package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GeyserCapBlockEntity extends GeneratingKineticBlockEntity implements GeoBlockEntity {

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
        return hasVent() ? Config.geyserCapRpm : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = hasVent() ? Config.geyserCapSu : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        long time = level.getGameTime();

        if (level.isClientSide) {
            if (time % 40 == 0 && getBlockState().getValue(GeyserCapBlock.LIT)) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS,
                        1.0f, 0.9f + level.random.nextFloat() * 0.2f, false);
            }
            return;
        }

        if (time % 20 == 0) {
            updateGeneratedRotation();
            boolean active = hasVent();
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(GeyserCapBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(GeyserCapBlock.LIT, active), 3);
        }

        if (time % 40 == 0 && hasVent()) {
            ServerLevel serverLevel = (ServerLevel) level;
            double cx = worldPosition.getX() + 0.5, cy = worldPosition.getY() + 1.0, cz = worldPosition.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.CLOUD,  cx, cy, cz, 6, 0.25, 0.05, 0.25, 0.04);
            serverLevel.sendParticles(ParticleTypes.SPLASH, cx, cy, cz, 8, 0.2,  0.1,  0.2,  0.1);
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
