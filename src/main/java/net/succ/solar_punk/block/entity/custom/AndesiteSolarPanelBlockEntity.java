package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.AndesiteSolarPanelBlock;

import java.util.List;

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
        float speed = switch (SolarHelper.getPhase(level)) {
            case MORNING, EVENING -> Config.andesiteMorningRpm;
            case NOON -> level.isRaining() ? Config.andesiteMorningRpm : Config.andesiteNoonRpm;
            case NIGHT -> 0;
        };
        if (rotationDirection.get() == RotationDirection.COUNTER_CLOCKWISE) speed = -speed;
        return speed;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 0;
        if (SolarHelper.hasSkyAccess(level, worldPosition)) {
            capacity = switch (SolarHelper.getPhase(level)) {
                case MORNING, EVENING -> Config.andesiteMorningSu;
                case NOON -> level.isRaining() ? Config.andesiteMorningSu : Config.andesiteNoonSu;
                case NIGHT -> 0;
            };
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = getGeneratedSpeed() != 0;
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof AndesiteSolarPanelBlock)) return;
            if (state.getValue(AndesiteSolarPanelBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(AndesiteSolarPanelBlock.LIT, active), 3);
        }
    }
}
