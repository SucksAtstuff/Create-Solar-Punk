package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;

import java.util.List;
import net.succ.solar_punk.util.SolarHelper;

public class AndesiteSolarPanelBlockEntity extends GeneratingKineticBlockEntity {

    ScrollOptionBehaviour<RotationDirection> rotationDirection;

    public AndesiteSolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
                        return VecHelper.voxelSpace(8, 5, 15.5);
                    }
                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        return direction.getAxis().isHorizontal();
                    }
                }
        );
        rotationDirection.withCallback($ -> this.updateGeneratedRotation());
        behaviours.add(rotationDirection);
    }

    @Override
    public float getGeneratedSpeed() {
        if (!SolarHelper.hasSkyAccess(level, worldPosition)) return 0;
        float speed = switch (SolarHelper.getPhase(level, worldPosition)) {
            case MORNING, EVENING -> Config.andesiteMorningRpm;
            case NOON -> SolarHelper.isRaining(level) ? Config.andesiteMorningRpm : Config.andesiteNoonRpm;
            case NIGHT -> 0;
        };
        // Round so the network keeps clean integer RPM; the season only shifts every few
        // in-game days, so this doesn't churn the kinetic network.
        if (speed != 0) speed = Math.round(speed * SolarHelper.outputMultiplier(level, worldPosition));
        if (rotationDirection.get() == RotationDirection.COUNTER_CLOCKWISE) speed = -speed;
        return speed;
    }

    // Server-computed capacity, synced for the goggle tooltip. The client can't always work
    // it out itself: in Cosmonautics deep space the sun position is server-only data.
    private float syncedCapacity = 0;

    @Override
    public float calculateAddedStressCapacity() {
        if (level != null && level.isClientSide) return syncedCapacity;
        float capacity = computeCapacity();
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    private float computeCapacity() {
        float capacity = 0;
        if (SolarHelper.hasSkyAccess(level, worldPosition)) {
            capacity = switch (SolarHelper.getPhase(level, worldPosition)) {
                case MORNING, EVENING -> Config.andesiteMorningSu;
                case NOON -> SolarHelper.isRaining(level) ? Config.andesiteMorningSu : Config.andesiteNoonSu;
                case NIGHT -> 0;
            };
            if (capacity != 0) capacity *= SolarHelper.outputMultiplier(level, worldPosition);
        }
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            float capacity = computeCapacity();
            if (capacity != syncedCapacity) {
                syncedCapacity = capacity;
                sendData();
            }
            boolean active = getGeneratedSpeed() != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof AndesiteSolarPanelBlock)) return;
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) tag.putFloat("SolarCapacity", syncedCapacity);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) syncedCapacity = tag.getFloat("SolarCapacity");
    }
}
