package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

// Fermentation Vat is a multiblock - only the controller cell ever has its own LIT
// toggled (see FermentationVatBlockEntity#setLit), so this only ever gets started from
// the controller's own tickAudio() and stops itself if that stops being true (fermenting
// stopped, or the multiblock split and this cell is no longer the controller).
@OnlyIn(Dist.CLIENT)
public class FermentationVatSoundInstance extends AbstractTickableSoundInstance {

    private final FermentationVatBlockEntity blockEntity;

    public FermentationVatSoundInstance(FermentationVatBlockEntity be) {
        super(ModSounds.FERMENTATION_VAT_BUBBLING.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        // fermentation_vat_bubbling.ogg is mastered ~9 dB hotter than electric_motor_buzz.ogg
        // (mean -25.2 dB vs -34.2 dB, measured via ffmpeg volumedetect), which
        // BrassPanelSoundInstance already plays at volume=0.75f without complaint. ~9 dB
        // is roughly a 2.8x loudness difference, so 0.75f / ~2.8 lands back in the same
        // perceived-loudness range instead of guessing at a number.
        this.volume = 0.27f;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.isController()
                || !blockEntity.getBlockState().getValue(FermentationVatBlock.LIT)
                || AmbientSoundRange.isTooFar(x, y, z)) {
            stop();
        }
    }
}
