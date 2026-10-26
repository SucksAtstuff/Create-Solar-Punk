package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BiomassGasifierBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BiomassGasifierSoundInstance extends MachineSoundInstance {

    public BiomassGasifierSoundInstance(BiomassGasifierBlockEntity be) {
        super(ModSounds.BIOMASS_GASIFIER_LOOP.get(), be, 0.75f);
    }
}
