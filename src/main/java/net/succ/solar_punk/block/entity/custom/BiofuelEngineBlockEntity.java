package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.pollution.PollutionSavedData;

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

    public BiofuelEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        rotationDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"),
                this,
                new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() {
                        return VecHelper.voxelSpace(8, 8, 15.5);
                    }
                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        // Everywhere except the front (matches FACING - reserved for the
                        // lit/output face) and the bottom (reserved for the input shaft,
                        // see hasShaftTowards) - leaves back, top, and both sides.
                        return direction != state.getValue(BiofuelEngineBlock.FACING)
                                && direction != Direction.DOWN;
                    }
                }
        );
        rotationDirection.withCallback($ -> this.updateGeneratedRotation());
        behaviours.add(rotationDirection);
    }

    @Override
    public float getGeneratedSpeed() {
        if (biofuelTank.getFluidAmount() <= 0) return 0;
        float speed = Config.biofuelEngineRpm;
        if (rotationDirection.get() == RotationDirection.COUNTER_CLOCKWISE) speed = -speed;
        return speed;
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
        if (level == null || level.isClientSide) return;

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

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (!biofuelTank.isEmpty()) {
            CompoundTag t = new CompoundTag();
            t.putString("Fluid", BuiltInRegistries.FLUID.getKey(biofuelTank.getFluid().getFluid()).toString());
            t.putInt("Amount", biofuelTank.getFluidAmount());
            tag.put("BiofuelTank", t);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("BiofuelTank")) {
            CompoundTag t = tag.getCompound("BiofuelTank");
            Fluid f = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(t.getString("Fluid"))).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY) biofuelTank.setFluid(new FluidStack(f, t.getInt("Amount")));
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.biofuel_engine_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        CreateLang.translate("solar_punk.tooltip.biofuel")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(biofuelTank.getFluidAmount())
                        .text(" / " + Config.biofuelEngineTank + " mB").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(Config.biofuelConsumeMb).text(" mB / " + Config.biofuelConsumePeriod + "t").style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);
        return true;
    }
}
