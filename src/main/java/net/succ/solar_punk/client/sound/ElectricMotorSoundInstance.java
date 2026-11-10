package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.ElectricMotorBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class ElectricMotorSoundInstance extends MachineSoundInstance {

    // Borrows the Brass Solar Panel's electric hum until the motor has its own sound.
    public ElectricMotorSoundInstance(ElectricMotorBlockEntity be) {
        super(ModSounds.BRASS_SOLAR_PANEL_HUM.get(), be, 0.5f);
    }
}
