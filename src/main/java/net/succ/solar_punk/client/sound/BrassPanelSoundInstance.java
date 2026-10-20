package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BrassSolarPanelBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BrassPanelSoundInstance extends MachineSoundInstance {

    // The reference volume every other machine sound is matched against.
    public BrassPanelSoundInstance(BrassSolarPanelBlockEntity be) {
        super(ModSounds.ALTERNATOR.get(), be, 0.75f);
    }
}
