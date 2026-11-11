package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.ElectrolyzerBlock;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.FluidTankNBTHelper;
import net.succ.solar_punk.util.GoggleHelper;
import net.succ.solar_punk.util.PonderHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Splits Water into Hydrogen and Oxygen with FE. Every tick it runs it uses
 * {@code fe_per_tick} FE and {@code water_per_tick} mB of Water, and makes the same amount
 * of Hydrogen and half as much Oxygen (2 H2O -> 2 H2 + O2). It stops when either gas tank
 * is full, so both gases have to be piped away.
 *
 * <p>Water goes in through any face but the two flanges; each flange gives out only its own
 * gas (see {@link #fluidHandler}). FE goes in through any face.
 */
public class ElectrolyzerBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public final FluidTank waterTank = tank(Config.electrolyzerTank, stack -> stack.getFluid().isSame(Fluids.WATER));
    public final FluidTank hydrogenTank = tank(Config.electrolyzerTank, stack -> stack.getFluid().isSame(ModFluids.HYDROGEN_SOURCE.get()));
    public final FluidTank oxygenTank = tank(Config.electrolyzerTank, stack -> stack.getFluid().isSame(ModFluids.OXYGEN_SOURCE.get()));

    // Water in only, for the top, front, back and bottom.
    private final IFluidHandler waterIn = new Handler(true, null);
    private final IFluidHandler hydrogenOut = new Handler(false, hydrogenTank);
    private final IFluidHandler oxygenOut = new Handler(false, oxygenTank);
    // Buckets and unsided lookups: Water in, Hydrogen then Oxygen out.
    public final IFluidHandler allFluids = new Handler(true, null);

    public final IEnergyStorage energyStorage = new Energy();
    private int energy = 0;
    // Water split so far that hasn't made a whole mB of Oxygen yet (0 or 1).
    private int oxygenRemainder = 0;
    private boolean dirty = false;

    // Client only: the drawn fill levels, eased towards the synced tank contents.
    public final LerpedFloat waterLevel = LerpedFloat.linear();
    public final LerpedFloat hydrogenLevel = LerpedFloat.linear();
    public final LerpedFloat oxygenLevel = LerpedFloat.linear();
    private boolean levelsStarted = false;

    public ElectrolyzerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private FluidTank tank(int capacity, java.util.function.Predicate<FluidStack> valid) {
        return new FluidTank(capacity, valid) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                dirty = true;
            }
        };
    }

    /** The fluid handler for a side; null for the unsided lookup. */
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        if (side == null) return allFluids;
        BlockState state = getBlockState();
        if (side == ElectrolyzerBlock.hydrogenSide(state)) return hydrogenOut;
        if (side == ElectrolyzerBlock.oxygenSide(state)) return oxygenOut;
        return waterIn;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            clientTick();
            return;
        }

        int water = Config.electrolyzerWaterPerTick;
        int oxygen = (oxygenRemainder + water) / 2;
        boolean canRun = energy >= Config.electrolyzerFePerTick
                && waterTank.getFluidAmount() >= water
                && hydrogenTank.getSpace() >= water
                && oxygenTank.getSpace() >= oxygen;

        if (canRun) {
            energy -= Config.electrolyzerFePerTick;
            waterTank.drain(water, IFluidHandler.FluidAction.EXECUTE);
            hydrogenTank.fill(new FluidStack(ModFluids.HYDROGEN_SOURCE.get(), water), IFluidHandler.FluidAction.EXECUTE);
            if (oxygen > 0)
                oxygenTank.fill(new FluidStack(ModFluids.OXYGEN_SOURCE.get(), oxygen), IFluidHandler.FluidAction.EXECUTE);
            oxygenRemainder = (oxygenRemainder + water) % 2;
            setChanged();
        }
        setLit(canRun);

        // Twice a second while anything moves, so the tubes fill smoothly; once a second
        // otherwise, for goggles.
        long time = level.getGameTime();
        if ((dirty && time % 10 == 0) || time % 20 == 0) {
            dirty = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof ElectrolyzerBlock && state.getValue(ElectrolyzerBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(ElectrolyzerBlock.LIT, lit), 3);
    }

    private void clientTick() {
        chase(waterLevel, waterTank);
        chase(hydrogenLevel, hydrogenTank);
        chase(oxygenLevel, oxygenTank);

        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        // Now and then vanilla's bubbling while it runs.
        if (getBlockState().getValue(ElectrolyzerBlock.LIT) && level.random.nextInt(80) == 0 && !PonderHelper.isPonder(level))
            level.playLocalSound(worldPosition, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                    0.15f, 1.4f + level.random.nextFloat() * 0.3f, false);
    }

    private static float fill(FluidTank tank) {
        return tank.getCapacity() > 0 ? Math.min(1f, tank.getFluidAmount() / (float) tank.getCapacity()) : 0f;
    }

    private static void chase(LerpedFloat level, FluidTank tank) {
        level.chase(fill(tank), 0.25, LerpedFloat.Chaser.EXP);
        level.tickChaser();
    }

    // -------------------------------------------------------------------------
    // Capabilities
    // -------------------------------------------------------------------------

    /**
     * Fills only the Water tank (if {@code takesWater}); drains only {@code gas}, or with no
     * gas given, Hydrogen and then Oxygen. Never gives Water back out.
     */
    private class Handler implements IFluidHandler {
        private final boolean takesWater;
        @Nullable private final FluidTank gas;

        Handler(boolean takesWater, @Nullable FluidTank gas) {
            this.takesWater = takesWater;
            this.gas = gas;
        }

        private FluidTank[] tanks() {
            return gas != null ? new FluidTank[]{gas} : new FluidTank[]{waterTank, hydrogenTank, oxygenTank};
        }

        @Override public int getTanks() { return tanks().length; }
        @Override public FluidStack getFluidInTank(int tank) { return tanks()[tank].getFluid(); }
        @Override public int getTankCapacity(int tank) { return tanks()[tank].getCapacity(); }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return takesWater && tanks()[tank] == waterTank && waterTank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return takesWater ? waterTank.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (gas != null) return gas.drain(resource, action);
            FluidStack drained = hydrogenTank.drain(resource, action);
            return drained.isEmpty() ? oxygenTank.drain(resource, action) : drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (gas != null) return gas.drain(maxDrain, action);
            FluidStack drained = hydrogenTank.drain(maxDrain, action);
            return drained.isEmpty() ? oxygenTank.drain(maxDrain, action) : drained;
        }
    }

    // Takes FE from any side; never gives any back.
    private class Energy implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = Math.max(0, Math.min(Math.min(toReceive, Config.electrolyzerMaxReceive),
                    Config.electrolyzerBuffer - energy));
            if (!simulate && received > 0) {
                energy += received;
                setChanged();
            }
            return received;
        }

        @Override public int extractEnergy(int toExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return energy; }
        @Override public int getMaxEnergyStored() { return Config.electrolyzerBuffer; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    // -------------------------------------------------------------------------
    // Goggles
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.electrolyzer_header").forGoggles(tooltip);

        boolean running = getBlockState().getValue(ElectrolyzerBlock.LIT);
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(running ? Config.electrolyzerFePerTick : 0))
                        .withStyle(running ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.fe(energy) + " / " + EnergyFormat.fe(Config.electrolyzerBuffer))
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        GoggleHelper.tank(tooltip, "water", waterTank, ChatFormatting.BLUE);
        GoggleHelper.tank(tooltip, "hydrogen", hydrogenTank, ChatFormatting.LIGHT_PURPLE);
        GoggleHelper.tank(tooltip, "oxygen", oxygenTank, ChatFormatting.AQUA);
        return true;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy);
        tag.putInt("OxygenRemainder", oxygenRemainder);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.save(tag, "HydrogenTank", hydrogenTank);
        FluidTankNBTHelper.save(tag, "OxygenTank", oxygenTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("Energy");
        oxygenRemainder = tag.getInt("OxygenRemainder");
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.load(tag, "HydrogenTank", hydrogenTank);
        FluidTankNBTHelper.load(tag, "OxygenTank", oxygenTank);
        // The first sync after the chunk loads shows the levels as they are, instead of
        // easing up from empty.
        if (!levelsStarted) {
            levelsStarted = true;
            waterLevel.startWithValue(fill(waterTank));
            hydrogenLevel.startWithValue(fill(hydrogenTank));
            oxygenLevel.startWithValue(fill(oxygenTank));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
