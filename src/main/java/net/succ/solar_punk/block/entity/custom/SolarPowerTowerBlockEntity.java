package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class SolarPowerTowerBlockEntity extends MultiBlockFluidBE<SolarPowerTowerBlockEntity>
        implements IHaveGoggleInformation {

    public static final int TANK_CAPACITY_PER_BLOCK = 8000;
    // Max height per footprint width: index 1→5, 2→10, 3→20
    private static final int[] MAX_HEIGHTS = {0, 5, 10, 20};

    private float saltAccumulator   = 0f;
    private int   cachedMirrorCount = 0;
    private int   mirrorScanCooldown = 0;

    public final FluidTank waterTank = new FluidTank(TANK_CAPACITY_PER_BLOCK) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final FluidTank saltTank = new FluidTank(TANK_CAPACITY_PER_BLOCK) {
        @Override public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.MOLTEN_SALT_SOURCE.get());
        }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        private SolarPowerTowerBlockEntity ctrl() { return getControllerBE(); }

        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return FluidStack.EMPTY;
            return tank == 0 ? c.waterTank.getFluid() : c.saltTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return 0;
            return tank == 0 ? c.waterTank.getCapacity() : c.saltTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(Fluids.WATER);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return 0;
            return resource.getFluid().isSame(Fluids.WATER) ? c.waterTank.fill(resource, action) : 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            return c != null ? c.saltTank.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            return c != null ? c.saltTank.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    };

    public SolarPowerTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SolarPowerTowerBlockEntity.class);
    }

    // -------------------------------------------------------------------------
    // IMultiBlockEntityContainer.Fluid
    // -------------------------------------------------------------------------

    @Override
    protected void updatePosition() {
        SolarPowerTowerBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return;
        int yOffset = worldPosition.getY() - ctrl.worldPosition.getY();
        boolean alone  = ctrl.height == 1;
        boolean isBot  = yOffset == 0;
        boolean isTop  = yOffset == ctrl.height - 1;
        SolarPowerTowerBlock.TowerPosition pos = alone  ? SolarPowerTowerBlock.TowerPosition.SINGLE
                                               : isBot  ? SolarPowerTowerBlock.TowerPosition.BOTTOM
                                               : isTop  ? SolarPowerTowerBlock.TowerPosition.TOP
                                                        : SolarPowerTowerBlock.TowerPosition.MIDDLE;
        BlockState state = getBlockState();
        if (state.getValue(SolarPowerTowerBlock.POSITION) != pos)
            level.setBlock(worldPosition, state.setValue(SolarPowerTowerBlock.POSITION, pos), 2);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis != Direction.Axis.Y) return MAX_WIDTH;
        return switch (width) {
            case 2 -> MAX_HEIGHTS[2];
            case 3 -> MAX_HEIGHTS[3];
            default -> MAX_HEIGHTS[1];
        };
    }

    @Override public int getTankSize(int tank) { return TANK_CAPACITY_PER_BLOCK; }

    @Override
    public void setTankSize(int tank, int blocks) {
        int newCap = TANK_CAPACITY_PER_BLOCK * Math.max(blocks, 1);
        waterTank.setCapacity(newCap);
        saltTank.setCapacity(newCap);
        if (waterTank.getFluidAmount() > newCap) waterTank.setFluid(new FluidStack(waterTank.getFluid().getFluid(), newCap));
        if (saltTank.getFluidAmount()  > newCap) saltTank.setFluid(new FluidStack(saltTank.getFluid().getFluid(), newCap));
    }

    @Override public IFluidTank getTank(int tank) { return waterTank; }
    @Override public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti(this);
        }

        if (!isController()) return;

        if (--mirrorScanCooldown <= 0) {
            mirrorScanCooldown = 40;
            cachedMirrorCount = isSunActive() ? scanMirrors() : 0;
        }

        if (width < 3 || height < 3) {
            saltAccumulator = 0f;
            setLit(false);
            return;
        }

        if (!isSunActive()) {
            saltAccumulator = 0f;
            setLit(false);
            return;
        }

        float efficiency = mirrorEfficiency();
        // Rate scales super-linearly with height (exponent 1.5) so taller towers are
        // always more block-efficient than multiple short ones.
        // At max size (3×3×20) and 100% mirrors: 9 mB/tick.
        int maxH = switch (width) { case 2 -> MAX_HEIGHTS[2]; case 3 -> MAX_HEIGHTS[3]; default -> MAX_HEIGHTS[1]; };
        float heightFraction = (float) height / maxH;
        float rate = (width * width) * (float) Math.pow(heightFraction, 1.5) * efficiency;
        saltAccumulator += rate;

        int saltToAdd = (int) saltAccumulator;
        if (saltToAdd >= 1) {
            int waterToDrain = saltToAdd * width * width;
            if (waterTank.getFluidAmount() >= waterToDrain &&
                saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), saltToAdd),
                        IFluidHandler.FluidAction.SIMULATE) == saltToAdd) {
                waterTank.drain(waterToDrain, IFluidHandler.FluidAction.EXECUTE);
                saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), saltToAdd),
                        IFluidHandler.FluidAction.EXECUTE);
                saltAccumulator -= saltToAdd;
            }
        }

        setLit(true);
        if (level.getGameTime() % 20 == 0) sync();
    }

    private boolean isSunActive() {
        if (level == null) return false;
        long time = level.getDayTime() % 24000;
        if (time >= 12000 || level.isThundering() || level.isRaining()) return false;
        return level.canSeeSky(worldPosition.above(height));
    }

    private int scanMirrors() {
        if (level == null) return 0;
        int count = 0;
        for (int dy = 0; dy < height; dy++) {
            for (int d = 0; d < width; d++) {
                if (isMirrorAt(worldPosition.offset(-1,    dy, d     ))) count++;
                if (isMirrorAt(worldPosition.offset(width, dy, d     ))) count++;
                if (isMirrorAt(worldPosition.offset(d,     dy, -1    ))) count++;
                if (isMirrorAt(worldPosition.offset(d,     dy, width ))) count++;
            }
        }
        return count;
    }

    private boolean isMirrorAt(BlockPos p) {
        return level.isLoaded(p) && level.getBlockState(p).is(ModBlocks.SOLAR_MIRROR.get());
    }

    // Triangle curve: ramps 0→100% up to the optimal mirror count, then falls back to 0% at 2× optimal.
    // Optimal = 2 × width × height (~half the directly-adjacent wall faces).
    // Filling every adjacent face tips into over-mirroring territory.
    private float mirrorEfficiency() {
        if (cachedMirrorCount == 0) return 0f;
        int optimal = 2 * width * height;
        float ratio = cachedMirrorCount / (float) optimal;
        return ratio <= 1f ? ratio : Math.max(0f, 2f - ratio);
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(SolarPowerTowerBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(SolarPowerTowerBlock.LIT, lit), 3);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveMultiblockNBT(tag);
        tag.putFloat("SaltAccumulator", saltAccumulator);
        tag.putInt("CachedMirrors", cachedMirrorCount);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.save(tag, "SaltTank",  saltTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadMultiblockNBT(tag);
        saltAccumulator   = tag.getFloat("SaltAccumulator");
        cachedMirrorCount = tag.getInt("CachedMirrors");
        if (isController()) {
            int totalBlocks = width * width * height;
            waterTank.setCapacity(TANK_CAPACITY_PER_BLOCK * totalBlocks);
            saltTank.setCapacity(TANK_CAPACITY_PER_BLOCK * totalBlocks);
        }
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.load(tag, "SaltTank",  saltTank);
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SolarPowerTowerBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return false;
        if (ctrl != this) return ctrl.addToGoggleTooltip(tooltip, isPlayerSneaking);

        int cap = waterTank.getCapacity();
        CreateLang.translate("solar_punk.tooltip.solar_power_tower_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + cap + " mB").style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.molten_salt")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(saltTank.getFluidAmount())
                        .text(" / " + cap + " mB").style(ChatFormatting.GOLD).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.mirrors")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(cachedMirrorCount).style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        int efficiencyPct = (int) (mirrorEfficiency() * 100);
        CreateLang.translate("solar_punk.tooltip.efficiency")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(efficiencyPct).text("%").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        return true;
    }
}
