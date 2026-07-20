package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

    // Arm order matches TurbineRotorRenderer: 0=East, 1=South, 2=West, 3=North.
    // Each entry lists the two blade positions (dx,dz) for that arm.
    public static final int[][][] ARM_OFFSETS = {
        {{ 1, 0}, { 2, 0}},  // East
        {{ 0, 1}, { 0, 2}},  // South
        {{-1, 0}, {-2, 0}},  // West
        {{ 0,-1}, { 0,-2}},  // North
    };

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
        public boolean isFluidValid(FluidStack stack) { return false; }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

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

    @Override
    public float getGeneratedSpeed() {
        if (!structureValid || !isMaster || steamTank.isEmpty()) return 0;
        return Math.min(Config.turbineMaxRpm, Config.turbineRpmPerLayer * turbineHeight);
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!structureValid || !isMaster || steamTank.isEmpty()) {
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

        int waterProduced = consumed.getAmount() / 10;
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

    private static final int[][] BLADE_OFFSETS = {{0,-2},{0,-1},{0,1},{0,2},{-2,0},{-1,0},{1,0},{2,0}};

    private void setBladeHidden(boolean hidden) {
        if (level == null || level.isClientSide || turbineHeight < 2) return;
        int rx = worldPosition.getX(), ry = worldPosition.getY(), rz = worldPosition.getZ();
        for (int dy = 0; dy < turbineHeight - 1; dy++) {
            for (int[] off : BLADE_OFFSETS) {
                BlockPos pos = new BlockPos(rx + off[0], ry + dy, rz + off[1]);
                BlockState bs = level.getBlockState(pos);
                if (bs.is(ModBlocks.ANDESITE_TURBINE_BLADE.get()))
                    level.setBlock(pos, bs.setValue(AndesiteTurbineBladeBlock.HIDDEN, hidden), 2);
                else if (bs.is(ModBlocks.BRASS_TURBINE_BLADE.get()))
                    level.setBlock(pos, bs.setValue(BrassTurbineBladeBlock.HIDDEN, hidden), 2);
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
        int rx = worldPosition.getX(), ry = worldPosition.getY(), rz = worldPosition.getZ();
        int height = Math.max(turbineHeight, 2);
        for (int y = ry - 1; y <= ry + height; y++) {
            for (int x = rx - 3; x <= rx + 3; x++) {
                for (int z = rz - 3; z <= rz + 3; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    level.invalidateCapabilities(p);
                    // Notify outer ring neighbours so adjacent pipes recheck and auto-connect
                    if (x == rx - 3 || x == rx + 3 || z == rz - 3 || z == rz + 3)
                        level.updateNeighborsAt(p, level.getBlockState(p).getBlock());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Structure scan
    // -------------------------------------------------------------------------

    // The rotor is always at the center of the 5x5 interior, so the 7x7 ring
    // is always exactly 3 blocks out in each horizontal direction.
    private boolean doStructureScan() {
        if (level == null) return false;

        int rx = worldPosition.getX();
        int ry = worldPosition.getY();
        int rz = worldPosition.getZ();

        int rMinX = rx - 3, rMaxX = rx + 3;
        int rMinZ = rz - 3, rMaxZ = rz + 3;

        if (!validateRing(rMinX, rMaxX, rMinZ, rMaxZ, ry)) {
            isMaster = false;
            return false;
        }

        // If there's a lower rotor at the same (x,z) inside a valid ring, it is master.
        for (int y = ry - 1; y >= ry - MAX_HEIGHT; y--) {
            if (!level.isLoaded(new BlockPos(rx, y, rz))) break;
            if (!validateRing(rMinX, rMaxX, rMinZ, rMaxZ, y)) break;
            if (level.getBlockState(new BlockPos(rx, y, rz)).is(ModBlocks.TURBINE_ROTOR.get())) {
                isMaster = false;
                return true; // structure valid, not master
            }
        }

        // This is the master (lowest blade layer). The layer directly below must be the solid floor.
        isMaster = true;
        if (!validateFloor(rx, ry - 1, rz)) return false;

        // Scan upward: find consecutive rotor+ring layers.
        int topY = ry;
        for (int y = ry + 1; y <= ry + MAX_HEIGHT; y++) {
            BlockPos checkPos = new BlockPos(rx, y, rz);
            if (!level.isLoaded(checkPos)) break;
            if (!validateRing(rMinX, rMaxX, rMinZ, rMaxZ, y)) break;
            if (!level.getBlockState(checkPos).is(ModBlocks.TURBINE_ROTOR.get())) break;
            topY = y;
        }

        // Top layer must have its interior fully sealed (the cap).
        if (topY == ry) return false; // no cap found above master
        if (!validateTopCap(rx, topY, rz)) return false;

        int height = topY - ry + 1; // blade layers + cap
        if (height < 2) return false; // minimum: 1 blade layer + cap

        // Count blades and build per-layer arm presence mask (4 bits: East/South/West/North).
        int andesite = 0, brass = 0;
        int[] newMask = new int[height - 1];
        int[] newTypeMask = new int[height - 1];
        for (int y = ry; y < topY; y++) {
            int dy = y - ry;
            int mask = 0, typeMask = 0;
            for (int arm = 0; arm < 4; arm++) {
                for (int[] off : ARM_OFFSETS[arm]) {
                    BlockState bs = level.getBlockState(new BlockPos(rx + off[0], y, rz + off[1]));
                    if (bs.is(ModBlocks.ANDESITE_TURBINE_BLADE.get())) { andesite++; mask |= (1 << arm); }
                    else if (bs.is(ModBlocks.BRASS_TURBINE_BLADE.get())) { brass++; mask |= (1 << arm); typeMask |= (1 << arm); }
                }
            }
            newMask[dy] = mask;
            newTypeMask[dy] = typeMask;
        }

        int maxBlades = (height - 1) * 8; // only cap has no blades
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

    private boolean validateRing(int minX, int maxX, int minZ, int maxZ, int y) {
        for (int x = minX; x <= maxX; x++) {
            if (!isCasing(new BlockPos(x, y, minZ))) return false;
            if (!isCasing(new BlockPos(x, y, maxZ))) return false;
        }
        for (int z = minZ + 1; z < maxZ; z++) {
            if (!isCasing(new BlockPos(minX, y, z))) return false;
            if (!isCasing(new BlockPos(maxX, y, z))) return false;
        }
        return true;
    }

    // Every position in the full 7×7 footprint must be casing — no rotor, no air.
    private boolean validateFloor(int rx, int y, int rz) {
        for (int x = rx - 3; x <= rx + 3; x++)
            for (int z = rz - 3; z <= rz + 3; z++)
                if (!isCasing(new BlockPos(x, y, z))) return false;
        return true;
    }

    // Every position in the 5×5 interior (excluding outer ring and center rotor) must be casing.
    private boolean validateTopCap(int rx, int y, int rz) {
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!isCasing(new BlockPos(rx + dx, y, rz + dz))) return false;
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
        for (int dy = 1; dy <= MAX_HEIGHT; dy++) {
            BlockPos check = worldPosition.below(dy);
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
    }
}
