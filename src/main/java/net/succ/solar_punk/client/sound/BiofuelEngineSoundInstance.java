package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.custom.BiofuelEngineBlock;
import net.succ.solar_punk.block.entity.custom.BiofuelEngineBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BiofuelEngineSoundInstance extends AbstractTickableSoundInstance {

    private final BiofuelEngineBlockEntity blockEntity;

    public BiofuelEngineSoundInstance(BiofuelEngineBlockEntity be) {
        super(ModSounds.BIOFUEL_ENGINE_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        // biofuel_engine_loop.ogg is mastered ~20 dB hotter than electric_motor_buzz.ogg
        // (mean -14.5 dB vs -34.2 dB, peak -0.8 dB vs -18.7 dB - measured via ffmpeg
        // volumedetect, not eyeballed), which BrassPanelSoundInstance already plays at
        // volume=0.75f without complaint. ~20 dB is roughly a 10x loudness difference,
        // so 0.75f / 10 lands back in the same perceived-loudness ballpark instead of
        // guessing at a number.
        this.volume = 0.08f;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.getBlockState().getValue(BiofuelEngineBlock.LIT)
                || AmbientSoundRange.isTooFar(x, y, z)) {
            stop();
        }
    }
}
