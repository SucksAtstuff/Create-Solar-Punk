package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.custom.SolarHeaterBlock;
import net.succ.solar_punk.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class SolarHeaterSoundInstance extends AbstractTickableSoundInstance {

    private final SolarHeaterBlockEntity blockEntity;

    public SolarHeaterSoundInstance(SolarHeaterBlockEntity be) {
        super(ModSounds.SOLAR_HEATER_SHIMMER.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        // solar_heater_shimmer.ogg is mastered ~2.7 dB quieter than electric_motor_buzz.ogg
        // (mean -36.9 dB vs -34.2 dB, measured via ffmpeg volumedetect), which would
        // actually call for a *higher* playback volume than BrassPanelSoundInstance's
        // 0.75f to loudness-match it. Deliberately going well under that match instead -
        // the Solar Heater has no moving parts, this is meant to read as a faint ambient
        // shimmer off the mirrors/salt, not a machine hum on par with an alternator.
        this.volume = 0.5f;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.getBlockState().getValue(SolarHeaterBlock.LIT)
                || AmbientSoundRange.isTooFar(x, y, z)) {
            stop();
        }
    }
}
