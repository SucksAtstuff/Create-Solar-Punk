package net.succ.solar_punk.client.sound;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class FermentationVatSoundInstance extends MachineSoundInstance {

    private final FermentationVatBlockEntity vat;

    // The clip is mastered ~9 dB (about 2.8x) hotter than the reference buzz.
    public FermentationVatSoundInstance(FermentationVatBlockEntity be) {
        super(ModSounds.FERMENTATION_VAT_BUBBLING.get(), be, 0.27f);
        this.vat = be;
    }

    // Only the controller's LIT is ever toggled, so also stop if the multiblock split
    // and this block is no longer the controller.
    @Override
    protected boolean isRunning() {
        return vat.isController() && super.isRunning();
    }
}
