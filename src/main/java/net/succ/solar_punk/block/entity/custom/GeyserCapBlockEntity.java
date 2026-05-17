package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.GeyserCapBlock;

public class GeyserCapBlockEntity extends GeneratingKineticBlockEntity implements GeoBlockEntity {

    private static final float RPM      = 32f;
    private static final float CAPACITY = 512f;

    private static final RawAnimation ACTIVE = RawAnimation.begin().thenLoop("animation.geyser_cap.active");
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("animation.geyser_cap.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GeyserCapBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private boolean hasVent() {
        return level != null && level.getBlockState(worldPosition.below()).is(ModBlocks.GEYSER_VENT.get());
    }

    @Override
    public float getGeneratedSpeed() {
        return hasVent() ? RPM : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = hasVent() ? CAPACITY : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && level.getGameTime() % 20 == 0) {
            updateGeneratedRotation();
            boolean active = hasVent();
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(GeyserCapBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(GeyserCapBlock.LIT, active), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            state.getController().setAnimation(hasVent() ? ACTIVE : IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
