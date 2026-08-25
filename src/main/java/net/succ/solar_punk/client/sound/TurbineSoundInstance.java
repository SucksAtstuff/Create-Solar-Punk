package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.custom.TurbineRotorBlock;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

// Turbine Rotor is a multiblock, same shape as FermentationVatSoundInstance - but rather
// than checking structureValid/isMaster directly, this just reads the ACTIVE blockstate
// flag TurbineRotorBlockEntity#setActive already maintains on the master's own position
// (true only while structure is valid, master, and actually consuming steam) - same
// "gate on a synced blockstate property" convention every other machine's SoundInstance
// (LIT) already uses, rather than reaching into BE fields directly.
@OnlyIn(Dist.CLIENT)
public class TurbineSoundInstance extends AbstractTickableSoundInstance {

    private final TurbineRotorBlockEntity blockEntity;

    public TurbineSoundInstance(TurbineRotorBlockEntity be) {
        super(ModSounds.TURBINE_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        // turbine_loop.ogg is mastered ~19 dB hotter than electric_motor_buzz.ogg (mean
        // -15.4 dB vs -34.2 dB, measured via ffmpeg volumedetect) - a strict loudness
        // match against BrassPanelSoundInstance's 0.75f would land around 0.086f, same
        // ballpark as BiofuelEngineSoundInstance's 0.08f. Bumped a bit above that on
        // purpose: this is the biggest kinetic structure in the mod short of the Fusion
        // Reactor, it's allowed a bit more presence than the small single-block machines.
        this.volume = 0.12f;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.getBlockState().getValue(TurbineRotorBlock.ACTIVE)
                || AmbientSoundRange.isTooFar(x, y, z)) {
            stop();
        }
    }
}
