package net.succ.solar_punk.util;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiPredicate;

// Shared bits of the generators that let you pick their spin direction, like Create's
// Windmill Bearing.
public final class GeneratorHelper {

    private GeneratorHelper() {}

    /**
     * The clockwise / counter-clockwise scroll option, shown at {@code height} px up on
     * the faces {@code sideActive} allows. Changing it re-applies the generated speed.
     */
    public static ScrollOptionBehaviour<RotationDirection> rotationDirection(
            GeneratingKineticBlockEntity be, double height, BiPredicate<BlockState, Direction> sideActive) {
        ScrollOptionBehaviour<RotationDirection> behaviour = new ScrollOptionBehaviour<>(RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"),
                be,
                new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() {
                        return VecHelper.voxelSpace(8, height, 15.5);
                    }

                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        return sideActive.test(state, direction);
                    }
                });
        behaviour.withCallback($ -> be.updateGeneratedRotation());
        return behaviour;
    }

    /** {@code speed}, flipped when the option is set to counter-clockwise. */
    public static float directed(float speed, ScrollOptionBehaviour<RotationDirection> direction) {
        return direction.get() == RotationDirection.COUNTER_CLOCKWISE ? -speed : speed;
    }
}
