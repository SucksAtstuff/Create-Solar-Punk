package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class SolarHeaterSoundInstance extends MachineSoundInstance {

    // The clip is slightly quieter than the reference buzz, but it's kept quieter still
    // on purpose: the heater has no moving parts, so this should be a faint shimmer
    // rather than a machine hum.
    public SolarHeaterSoundInstance(SolarHeaterBlockEntity be) {
        super(ModSounds.SOLAR_HEATER_SHIMMER.get(), be, 0.5f);
    }
}
