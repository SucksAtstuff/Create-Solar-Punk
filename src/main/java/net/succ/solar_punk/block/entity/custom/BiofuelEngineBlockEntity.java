package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.client.sound.MachineSoundInstance;
import net.succ.solar_punk.util.GoggleHelper;
import net.succ.solar_punk.util.GeneratorHelper;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
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
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.util.FluidTankNBTHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiofuelEngineBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    ScrollOptionBehaviour<RotationDirection> rotationDirection;

    public final FluidTank biofuelTank = new FluidTank(Config.biofuelEngineTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get());
        }
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide)
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    };

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
        if (biofuelTank.getFluidAmount() <= 0) return 0;
        return GeneratorHelper.directed(Config.biofuelEngineRpm, rotationDirection);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = biofuelTank.getFluidAmount() > 0 ? Config.biofuelEngineSu : 0;
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

        if (level.getGameTime() % Config.biofuelConsumePeriod == 0 && biofuelTank.getFluidAmount() >= Config.biofuelConsumeMb)
            biofuelTank.drain(Config.biofuelConsumeMb, IFluidHandler.FluidAction.EXECUTE);

        if (level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = biofuelTank.getFluidAmount() > 0;
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
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        FluidTankNBTHelper.load(tag, "BiofuelTank", biofuelTank);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.biofuel_engine_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        GoggleHelper.tank(tooltip, "biofuel", biofuelTank, ChatFormatting.GREEN);
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(Config.biofuelConsumeMb).text(" mB / " + Config.biofuelConsumePeriod + "t").style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);
        return true;
    }
}
