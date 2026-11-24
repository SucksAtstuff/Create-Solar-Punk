package net.succ.solar_punk.block.entity.custom;

import net.succ.offbyonelib.client.sound.MachineSoundInstance;
import net.succ.solar_punk.SolarPunk;
import net.succ.offbyonelib.util.GeneratorHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.client.sound.BiofuelEngineSoundInstance;
import net.succ.solar_punk.datamap.ModDataMaps;
import net.succ.offbyonelib.util.FluidTankNBTHelper;
import net.succ.solar_punk.util.OxyFuel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiofuelEngineBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    ScrollOptionBehaviour<RotationDirection> rotationDirection;

    public final FluidTank biofuelTank = new FluidTank(Config.biofuelEngineTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            // solarpunk:engine_fuels: Biofuel by default
            return ModDataMaps.engineFuel(stack) != null;
        }
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide)
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };

    // Oxygen piped in alongside the Biofuel raises the stress capacity while running
    public final OxyFuel oxyFuel = new OxyFuel(Config.biofuelEngineOxygenTank, Config.biofuelEngineOxygenPerSecond,
            Config.biofuelEngineOxygenBoost, this::setChanged);
    // Biofuel and Oxygen in from any face; only Biofuel comes back out
    public final IFluidHandler fluidHandler = oxyFuel.handler(biofuelTank);

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private BiofuelEngineSoundInstance soundInstance;

    public BiofuelEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // Every side except the front (FACING) and the bottom (the shaft)
        rotationDirection = GeneratorHelper.rotationDirection(this, 8, (state, direction) ->
                direction != state.getValue(BiofuelEngineBlock.FACING) && direction != Direction.DOWN);
        behaviours.add(rotationDirection);
    }

    @Override
    public float getGeneratedSpeed() {
        if (!hasFuel()) return 0;
        return GeneratorHelper.directed(Config.biofuelEngineRpm, rotationDirection);
    }

    // mB burned each second: a bucket lasts the fuel's ticks_per_bucket
    public int fuelPerSecond() {
        ModDataMaps.EngineFuel fuel = ModDataMaps.engineFuel(biofuelTank.getFluid());
        return fuel == null ? 0 : Math.max(1, Math.round(20_000f / fuel.ticksPerBucket()));
    }

    // Runs only while the tank can pay for the next second, so a leftover below
    // one second's worth can't keep it spinning without ever being drained
    private boolean hasFuel() {
        int perSecond = fuelPerSecond();
        return perSecond > 0 && biofuelTank.getFluidAmount() >= perSecond;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = hasFuel() ? oxyFuel.capacity(Config.biofuelEngineSu) : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        if (level.getGameTime() % 20 == 0 && hasFuel())
            biofuelTank.drain(fuelPerSecond(), IFluidHandler.FluidAction.EXECUTE);

        if (level.getGameTime() % 20 == 0) {
            oxyFuel.tickSecond(hasFuel());
            // Also re-solves the network for the Oxygen boost, and syncs the tanks to Goggles
            updateGeneratedRotation();
            boolean active = hasFuel();
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof BiofuelEngineBlock)) return;
            if (state.getValue(BiofuelEngineBlock.LIT) != active) {
                level.setBlock(worldPosition, state.setValue(BiofuelEngineBlock.LIT, active), 3);
                if (active)
                    ModTriggers.fireNearby(level, worldPosition, ModTriggers.BIOFUEL_ENGINE_ON);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        // Create's KineticBlockEntity.tick() calls this too, so Ponder's silence is
        // handled in keepPlaying() rather than at the call site
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance,
                getBlockState().getValue(BiofuelEngineBlock.LIT), () -> new BiofuelEngineSoundInstance(this));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        FluidTankNBTHelper.save(tag, "BiofuelTank", biofuelTank);
        oxyFuel.write(tag);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // Before super.read(), which reads the capacity back
        FluidTankNBTHelper.load(tag, "BiofuelTank", biofuelTank);
        oxyFuel.read(tag);
        super.read(tag, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.biofuel_engine_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        // "Fuel:" with the fluid's name, since datapacks can add other fuels
        LangBuilder fuelLine = SolarPunk.GOGGLES.tankLine("fuel",
                biofuelTank.getFluidAmount(), biofuelTank.getCapacity(), ChatFormatting.GREEN);
        if (!biofuelTank.isEmpty())
            fuelLine.add(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY)
                    .append(biofuelTank.getFluid().getHoverName().copy().withStyle(ChatFormatting.DARK_GREEN))
                    .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)));
        fuelLine.forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(fuelPerSecond()).text(" mB/s").style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);
        oxyFuel.addToGoggleTooltip(tooltip);
        return true;
    }
}
