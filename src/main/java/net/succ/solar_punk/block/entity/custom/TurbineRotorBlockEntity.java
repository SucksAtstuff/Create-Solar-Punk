package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.AndesiteTurbineBladeBlock;
import net.succ.solar_punk.block.custom.BrassTurbineBladeBlock;
import net.succ.solar_punk.block.custom.TurbineRotorBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class TurbineRotorBlockEntity extends GeneratingKineticBlockEntity
        implements IHaveGoggleInformation {

    private static final int SCAN_INTERVAL = 40;
    private static final int MAX_HEIGHT = 20;
    private static final float BASE_EFFICIENCY = 0.1f;

    // The turbine can be built along any of the 3 axes, not just vertical. "Growth"
    // is the direction blade layers stack in; the master rotor is the layer closest
    // to the negative growth direction (the "floor" end), the cap is at the positive
    // end. The 4 "arms" a layer's blades sit on are always exactly the 4 Direction
    // values that aren't on the growth axis - Direction.getClockWise(axis) gives the
    // engine's own canonical cycling order, chosen here to reproduce the original
    // shipped arm order (East,South,West,North) exactly when axis=Y.
    private static Direction growthPositive(Direction.Axis axis) {
        return switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
    }

    private static Direction[] armDirections(Direction.Axis axis) {
        Direction base = switch (axis) {
            case X -> Direction.UP;
            case Y, Z -> Direction.EAST;
        };
        Direction[] arms = new Direction[4];
        arms[0] = base;
        for (int i = 1; i < 4; i++) arms[i] = arms[i - 1].getClockWise(axis);
        return arms;
    }

    public boolean structureValid = false;
    public boolean isMaster = false;
    public int turbineHeight = 0;
    public int andesiteBladeCount = 0;
    public int brassBladeCount = 0;
    public int[] layerBladeMask = new int[0];     // 4-bit mask per blade layer; bit n = arm n present
    public int[] layerBladeTypeMask = new int[0]; // 4-bit mask per blade layer; bit n = arm n is brass
    private float bladeEfficiency = BASE_EFFICIENCY;
    private int scanCooldown = 1;
    private boolean needsCapabilityRefresh = true; // not persisted - fires once after each load

    public final FluidTank steamTank = new FluidTank(Config.turbineSteamTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get());
        }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public final FluidTank condensateTank = new FluidTank(Config.turbineCondensateTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    private int condensateAccumulator = 0;

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? steamTank.getFluid() : condensateTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? steamTank.getCapacity() : condensateTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get());
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!resource.getFluid().isSame(ModFluids.STEAM_SOURCE.get())) return 0;
            return steamTank.fill(resource, action);
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(Fluids.WATER))
                return condensateTank.drain(resource, action);
            return FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return condensateTank.drain(maxDrain, action);
        }
    };

    public TurbineRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // -------------------------------------------------------------------------
    // GeneratingKineticBlockEntity
    // -------------------------------------------------------------------------

    private boolean condensateFull() {
        return condensateTank.getFluidAmount() >= condensateTank.getCapacity();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!structureValid || !isMaster || steamTank.isEmpty() || condensateFull()) return 0;
        return Math.min(Config.turbineMaxRpm, Config.turbineRpmPerLayer * turbineHeight);
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!structureValid || !isMaster || steamTank.isEmpty() || condensateFull()) {
            this.lastCapacityProvided = 0;
            return 0;
        }
        float su = Config.turbineSuPerLayer * turbineHeight * bladeEfficiency;
        this.lastCapacityProvided = su;
        return su;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        if (needsCapabilityRefresh && structureValid && isMaster) {
            needsCapabilityRefresh = false;
            invalidateStructureCapabilities();
        }

        if (--scanCooldown <= 0) {
            scanCooldown = SCAN_INTERVAL;
            boolean wasValid = structureValid;
            boolean wasMaster = isMaster;
            structureValid = doStructureScan();
            if (wasValid != structureValid || wasMaster != isMaster) {
                updateGeneratedRotation();
                setChanged();
                invalidateStructureCapabilities();
                if (structureValid && isMaster && !wasValid)
                    ModTriggers.fireNearby(level, worldPosition, ModTriggers.TURBINE_BUILT);
            }
        }

        if (!structureValid || !isMaster || steamTank.isEmpty()) {
            if (getBlockState().getBlock() instanceof TurbineRotorBlock b
                    && getBlockState().getValue(TurbineRotorBlock.ACTIVE)) {
                setActive(false);
                updateGeneratedRotation();
            }
            return;
        }

        if (condensateFull()) {
            if (getBlockState().getBlock() instanceof TurbineRotorBlock b
                    && getBlockState().getValue(TurbineRotorBlock.ACTIVE)) {
                setActive(false);
                updateGeneratedRotation();
                ModTriggers.fireNearby(level, worldPosition, ModTriggers.TURBINE_FLOODED);
            }
            return;
        }

        // Update speed BEFORE draining so getGeneratedSpeed() sees a non-empty tank.
        // Also fire immediately on the first active tick so the kinetic network wakes up.
        boolean wasActive = getBlockState().getBlock() instanceof TurbineRotorBlock
                && getBlockState().getValue(TurbineRotorBlock.ACTIVE);
        if (!wasActive || level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            setChanged();
        }

        int steamPerTick = Math.max(1,
                (int) Math.ceil(Config.turbineSteamPerLayerPerTick * turbineHeight / bladeEfficiency));
        FluidStack consumed = steamTank.drain(steamPerTick, IFluidHandler.FluidAction.EXECUTE);

        if (consumed.isEmpty()) {
            setActive(false);
            updateGeneratedRotation();
            return;
        }

        condensateAccumulator += consumed.getAmount();
        int waterProduced = condensateAccumulator / Config.turbineCondensateRatio;
        condensateAccumulator -= waterProduced * Config.turbineCondensateRatio;
        if (waterProduced > 0)
            condensateTank.fill(new FluidStack(Fluids.WATER, waterProduced), IFluidHandler.FluidAction.EXECUTE);

        setActive(true);
    }

    private void setActive(boolean active) {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof TurbineRotorBlock && state.getValue(TurbineRotorBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(TurbineRotorBlock.ACTIVE, active), 3);
            setBladeHidden(active);
        }
    }

    private void setBladeHidden(boolean hidden) {
        if (level == null || level.isClientSide || turbineHeight < 2) return;
        Direction.Axis axis = getBlockState().getValue(TurbineRotorBlock.AXIS);
        Direction growthDir = growthPositive(axis);
        Direction[] arms = armDirections(axis);
        for (int s = 0; s < turbineHeight - 1; s++) {
            BlockPos layerOrigin = worldPosition.relative(growthDir, s);
            for (Direction armDir : arms) {
                for (int dist = 1; dist <= 2; dist++) {
                    BlockPos pos = layerOrigin.relative(armDir, dist);
                    BlockState bs = level.getBlockState(pos);
                    if (bs.is(ModBlocks.ANDESITE_TURBINE_BLADE.get()))
                        level.setBlock(pos, bs.setValue(AndesiteTurbineBladeBlock.HIDDEN, hidden), 2);
                    else if (bs.is(ModBlocks.BRASS_TURBINE_BLADE.get()))
                        level.setBlock(pos, bs.setValue(BrassTurbineBladeBlock.HIDDEN, hidden), 2);
                }
            }
        }
    }

    public void invalidateStructure() {
        if (isMaster && structureValid) setBladeHidden(false);
        structureValid = false;
        isMaster = false;
        scanCooldown = 0;
        updateGeneratedRotation();
        setChanged();
        invalidateStructureCapabilities();
    }

    private void invalidateStructureCapabilities() {
        if (level == null || level.isClientSide) return;
        level.invalidateCapabilities(worldPosition);
        Direction.Axis axis = getBlockState().getValue(TurbineRotorBlock.AXIS);
        Direction growthDir = growthPositive(axis);
        Direction[] arms = armDirections(axis);
        Direction uDir = arms[0], vDir = arms[1];
        int height = Math.max(turbineHeight, 2);
        for (int s = -1; s <= height; s++) {
            BlockPos layerOrigin = worldPosition.relative(growthDir, s);
            for (int u = -3; u <= 3; u++) {
                for (int v = -3; v <= 3; v++) {
                    BlockPos p = layerOrigin.relative(uDir, u).relative(vDir, v);
                    level.invalidateCapabilities(p);
                    // Notify outer ring neighbours so adjacent pipes recheck and auto-connect
                    if (u == -3 || u == 3 || v == -3 || v == 3)
                        level.updateNeighborsAt(p, level.getBlockState(p).getBlock());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Structure scan
    // -------------------------------------------------------------------------

    // The rotor is always at the center of the 5x5 interior, so the 7x7 ring is
    // always exactly 3 blocks out in the plane perpendicular to the growth axis.
    // "s" (steps) below always means "how many blocks along growthDir from this
    // rotor" - negative is toward the floor end, positive toward the cap end.
    private boolean doStructureScan() {
        if (level == null) return false;
        if (!(getBlockState().getBlock() instanceof TurbineRotorBlock)) return false;

        Direction.Axis axis = getBlockState().getValue(TurbineRotorBlock.AXIS);
        Direction growthDir = growthPositive(axis);
        Direction[] arms = armDirections(axis);
        Direction uDir = arms[0], vDir = arms[1];

        if (!validateRing(growthDir, uDir, vDir, 0)) {
            isMaster = false;
            return false;
        }

        // If there's a rotor further toward the floor end inside a valid ring, it is master.
        for (int s = -1; s >= -MAX_HEIGHT; s--) {
            BlockPos checkPos = worldPosition.relative(growthDir, s);
            if (!level.isLoaded(checkPos)) break;
            if (!validateRing(growthDir, uDir, vDir, s)) break;
            if (level.getBlockState(checkPos).is(ModBlocks.TURBINE_ROTOR.get())) {
                isMaster = false;
                return true; // structure valid, not master
            }
        }

        // This is the master (the blade layer closest to the floor end). The layer
        // one further toward the floor end must be the solid floor.
        isMaster = true;
        if (!validateFloor(growthDir, uDir, vDir, -1)) return false;

        // Scan toward the cap end: find consecutive rotor+ring layers.
        int topS = 0;
        for (int s = 1; s <= MAX_HEIGHT; s++) {
            BlockPos checkPos = worldPosition.relative(growthDir, s);
            if (!level.isLoaded(checkPos)) break;
            if (!validateRing(growthDir, uDir, vDir, s)) break;
            if (!level.getBlockState(checkPos).is(ModBlocks.TURBINE_ROTOR.get())) break;
            topS = s;
        }

        // Top layer must have its interior fully sealed (the cap).
        if (topS == 0) return false; // no cap found beyond master
        if (!validateTopCap(growthDir, uDir, vDir, topS)) return false;

        int height = topS + 1; // blade layers + cap
        if (height < 2) return false; // minimum: 1 blade layer + cap

        // Count blades and build per-layer arm presence mask (4 bits, one per arm direction).
        int andesite = 0, brass = 0;
        int[] newMask = new int[height - 1];
        int[] newTypeMask = new int[height - 1];
        for (int s = 0; s < topS; s++) {
            BlockPos layerOrigin = worldPosition.relative(growthDir, s);
            int mask = 0, typeMask = 0;
            for (int arm = 0; arm < 4; arm++) {
                Direction armDir = arms[arm];
                for (int dist = 1; dist <= 2; dist++) {
                    BlockPos bladePos = layerOrigin.relative(armDir, dist);
                    BlockState bs = level.getBlockState(bladePos);
                    boolean isAndesite = bs.is(ModBlocks.ANDESITE_TURBINE_BLADE.get());
                    boolean isBrass = !isAndesite && bs.is(ModBlocks.BRASS_TURBINE_BLADE.get());
                    if (isAndesite) { andesite++; mask |= (1 << arm); }
                    else if (isBrass) { brass++; mask |= (1 << arm); typeMask |= (1 << arm); }
                    if (isAndesite || isBrass)
                        syncBladeOrientation(bladePos, bs, isAndesite, axis, armDir);
                }
            }
            newMask[s] = mask;
            newTypeMask[s] = typeMask;
        }

        int maxBlades = (height - 1) * 4; // 1 blade per arm, 4 arms, cap layer has no blades
        float weighted = andesite * 0.7f + brass;
        float bladeRatio = maxBlades > 0 ? Math.min(1f, weighted / maxBlades) : 0f;

        this.turbineHeight = height;
        this.andesiteBladeCount = andesite;
        this.brassBladeCount = brass;
        this.layerBladeMask = newMask;
        this.layerBladeTypeMask = newTypeMask;
        this.bladeEfficiency = BASE_EFFICIENCY + (1f - BASE_EFFICIENCY) * bladeRatio;
        return true;
    }

    // Keeps a discovered blade's cosmetic FACING/AXIS in sync with its actual position
    // in the structure. Purely visual (the scan above only ever checks block type),
    // but keeps the idle model looking right regardless of how the blade was placed.
    // Guarded by an equality check so a correctly-oriented blade isn't rewritten (and
    // its chunk/light state churned) on every scan interval.
    private void syncBladeOrientation(BlockPos pos, BlockState bs, boolean isAndesite,
                                       Direction.Axis wantAxis, Direction wantFacing) {
        Direction curFacing = isAndesite ? bs.getValue(AndesiteTurbineBladeBlock.FACING) : bs.getValue(BrassTurbineBladeBlock.FACING);
        Direction.Axis curAxis = isAndesite ? bs.getValue(AndesiteTurbineBladeBlock.AXIS) : bs.getValue(BrassTurbineBladeBlock.AXIS);
        if (curFacing == wantFacing && curAxis == wantAxis) return;
        BlockState updated = isAndesite
                ? bs.setValue(AndesiteTurbineBladeBlock.FACING, wantFacing).setValue(AndesiteTurbineBladeBlock.AXIS, wantAxis)
                : bs.setValue(BrassTurbineBladeBlock.FACING, wantFacing).setValue(BrassTurbineBladeBlock.AXIS, wantAxis);
        level.setBlock(pos, updated, 2);
    }

    // The perpendicular plane offset helper: growthDir/uDir/vDir form a 3-axis basis
    // (u,v span the ring/floor/cap plane), so this is a strict generalization of the
    // old literal x/y/z arithmetic - it reduces to exactly that when axis=Y (uDir=East,
    // vDir=South, matching the original x/z offsets one-for-one).
    private BlockPos offset(Direction growthDir, Direction uDir, Direction vDir, int alongGrowth, int u, int v) {
        return worldPosition.relative(growthDir, alongGrowth).relative(uDir, u).relative(vDir, v);
    }

    private boolean validateRing(Direction growthDir, Direction uDir, Direction vDir, int alongGrowth) {
        for (int u = -3; u <= 3; u++) {
            if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, u, -3))) return false;
            if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, u, 3))) return false;
        }
        for (int v = -2; v <= 2; v++) {
            if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, -3, v))) return false;
            if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, 3, v))) return false;
        }
        return true;
    }

    // Every position in the full 7×7 footprint must be casing — no rotor, no air.
    private boolean validateFloor(Direction growthDir, Direction uDir, Direction vDir, int alongGrowth) {
        for (int u = -3; u <= 3; u++)
            for (int v = -3; v <= 3; v++)
                if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, u, v))) return false;
        return true;
    }

    // Every position in the 5×5 interior (excluding outer ring and center rotor) must be casing.
    private boolean validateTopCap(Direction growthDir, Direction uDir, Direction vDir, int alongGrowth) {
        for (int u = -2; u <= 2; u++)
            for (int v = -2; v <= 2; v++) {
                if (u == 0 && v == 0) continue;
                if (!isCasing(offset(growthDir, uDir, vDir, alongGrowth, u, v))) return false;
            }
        return true;
    }

    private boolean isCasing(BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) return false;
        BlockState s = level.getBlockState(pos);
        return s.is(ModBlocks.TURBINE_CASING.get()) || s.is(ModBlocks.TURBINE_CASING_GLASS.get());
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    private TurbineRotorBlockEntity findMaster() {
        if (level == null) return null;
        if (!(getBlockState().getBlock() instanceof TurbineRotorBlock)) return null;
        Direction.Axis axis = getBlockState().getValue(TurbineRotorBlock.AXIS);
        Direction towardFloor = growthPositive(axis).getOpposite();
        for (int s = 1; s <= MAX_HEIGHT; s++) {
            BlockPos check = worldPosition.relative(towardFloor, s);
            if (!level.isLoaded(check)) break;
            if (!level.getBlockState(check).is(ModBlocks.TURBINE_ROTOR.get())) break;
            BlockEntity be = level.getBlockEntity(check);
            if (be instanceof TurbineRotorBlockEntity rotor && rotor.isMaster) return rotor;
        }
        return null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!isMaster && structureValid) {
            TurbineRotorBlockEntity master = findMaster();
            if (master != null) return master.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }

        CreateLang.translate("solar_punk.tooltip.steam_turbine_header").forGoggles(tooltip);

        if (!structureValid) {
            CreateLang.translate("solar_punk.tooltip.turbine_invalid")
                    .style(ChatFormatting.RED).forGoggles(tooltip, 1);
            return true;
        }
        if (!isMaster) {
            CreateLang.translate("solar_punk.tooltip.turbine_invalid")
                    .style(ChatFormatting.RED).forGoggles(tooltip, 1);
            return true;
        }

        CreateLang.translate("solar_punk.tooltip.turbine_height")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(turbineHeight).style(ChatFormatting.WHITE).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.turbine_blades")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(andesiteBladeCount + brassBladeCount)
                        .text(" (" + andesiteBladeCount + "A / " + brassBladeCount + "B)")
                        .style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        int effPct = (int) (bladeEfficiency * 100);
        CreateLang.translate("solar_punk.tooltip.efficiency")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(effPct).text("%").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.steam")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(steamTank.getFluidAmount())
                        .text(" / " + steamTank.getCapacity() + " mB")
                        .style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(condensateTank.getFluidAmount())
                        .text(" / " + condensateTank.getCapacity() + " mB")
                        .style(ChatFormatting.BLUE).component())
                .forGoggles(tooltip, 1);

        if (condensateFull())
            CreateLang.translate("solar_punk.tooltip.condensate_full")
                    .style(ChatFormatting.RED).forGoggles(tooltip, 1);

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("StructureValid", structureValid);
        tag.putBoolean("IsMaster", isMaster);
        tag.putInt("TurbineHeight", turbineHeight);
        tag.putInt("AndesiteBlades", andesiteBladeCount);
        tag.putInt("BrassBlades", brassBladeCount);
        tag.putFloat("BladeEfficiency", bladeEfficiency);
        tag.putIntArray("LayerBladeMask", layerBladeMask);
        tag.putIntArray("LayerBladeTypeMask", layerBladeTypeMask);
        FluidTankNBTHelper.save(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.save(tag, "CondensateTank", condensateTank);
        tag.putInt("CondensateAccumulator", condensateAccumulator);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        structureValid = tag.getBoolean("StructureValid");
        isMaster = tag.getBoolean("IsMaster");
        turbineHeight = tag.getInt("TurbineHeight");
        andesiteBladeCount = tag.getInt("AndesiteBlades");
        brassBladeCount = tag.getInt("BrassBlades");
        bladeEfficiency = tag.getFloat("BladeEfficiency");
        if (bladeEfficiency < BASE_EFFICIENCY) bladeEfficiency = BASE_EFFICIENCY;
        layerBladeMask = tag.getIntArray("LayerBladeMask");
        layerBladeTypeMask = tag.getIntArray("LayerBladeTypeMask");
        FluidTankNBTHelper.load(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.load(tag, "CondensateTank", condensateTank);
        condensateAccumulator = tag.getInt("CondensateAccumulator");
    }
}
