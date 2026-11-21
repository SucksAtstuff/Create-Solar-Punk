package net.succ.solar_punk.client.sound;

import net.succ.offbyonelib.client.sound.MachineSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BioreactorBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BioreactorSoundInstance extends MachineSoundInstance {

    private final BioreactorBlockEntity bioreactor;

    // A low background hum, well under the Brass Solar Panel's; the bubbling on top of it is a
    // one-shot played every so often from BioreactorBlockEntity#tickAudio.
    public BioreactorSoundInstance(BioreactorBlockEntity be) {
        super(ModSounds.BIOREACTOR_HUM.get(), be, 0.07f);
        this.bioreactor = be;
        // From the middle of the tank rather than the controller's corner.
        this.x = be.getBlockPos().getX() + be.getWidth() / 2.0;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + be.getWidth() / 2.0;
    }

    // Only the controller's LIT is toggled, so stop if the tank split and this block is no
    // longer the controller.
    @Override
    protected boolean isRunning() {
        return bioreactor.isController() && super.isRunning();
    }
}
