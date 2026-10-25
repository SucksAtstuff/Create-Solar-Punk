package net.succ.solar_punk.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// Looping machine ambience, positioned at the block and kept alive while the block's
// LIT state is true. Subclasses only pick the sound and volume, and can override
// isRunning() for machines that don't use LIT.
//
// Volumes are tuned against the Brass Solar Panel's electric_motor_buzz.ogg at 0.75,
// measured with ffmpeg volumedetect, so every machine sounds about equally loud.
@OnlyIn(Dist.CLIENT)
public class MachineSoundInstance extends AbstractTickableSoundInstance {

    // LINEAR attenuation fades these out at ~16 blocks, but only for mono files: stereo
    // sounds play non-positionally, which is why they were once audible from 200+ blocks.
    // Every sound file must stay mono. The range is still enforced here as a backstop;
    // 20 gives a little headroom over the intended 16.
    private static final double MAX_DISTANCE_SQ = 20.0 * 20.0;

    protected final BlockEntity blockEntity;

    public MachineSoundInstance(SoundEvent sound, BlockEntity be, float volume) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    protected boolean isRunning() {
        BlockState state = blockEntity.getBlockState();
        return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
    }

    protected boolean isTooFar() {
        var player = Minecraft.getInstance().player;
        return player == null || player.distanceToSqr(x, y, z) > MAX_DISTANCE_SQ;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !isRunning() || isTooFar())
            stop();
    }
}
