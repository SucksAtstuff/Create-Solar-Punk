package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BiofuelEngineBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BiofuelEngineSoundInstance extends MachineSoundInstance {

    // The clip is mastered ~20 dB (about 10x) hotter than the reference buzz.
    public BiofuelEngineSoundInstance(BiofuelEngineBlockEntity be) {
        super(ModSounds.BIOFUEL_ENGINE_LOOP.get(), be, 0.08f);
    }
}
