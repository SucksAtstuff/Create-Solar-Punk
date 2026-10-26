package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

// Gated on the master rotor's ACTIVE state (which is the LIT property), set only while
// the structure is valid and actually consuming steam.
@OnlyIn(Dist.CLIENT)
public class TurbineSoundInstance extends MachineSoundInstance {

    // The clip is mastered ~19 dB hotter than the reference buzz, so a strict match
    // would be ~0.086. It's a bit louder on purpose since the turbine is a big machine.
    public TurbineSoundInstance(TurbineRotorBlockEntity be) {
        super(ModSounds.TURBINE_LOOP.get(), be, 0.12f);
    }
}
