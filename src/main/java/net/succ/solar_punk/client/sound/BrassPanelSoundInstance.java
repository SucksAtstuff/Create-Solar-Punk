package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.custom.BrassSolarPanelBlock;
import net.succ.solar_punk.block.entity.custom.BrassSolarPanelBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BrassPanelSoundInstance extends AbstractTickableSoundInstance {

    private final BrassSolarPanelBlockEntity blockEntity;

    public BrassPanelSoundInstance(BrassSolarPanelBlockEntity be) {
        super(ModSounds.ALTERNATOR.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.75f;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || !blockEntity.getBlockState().getValue(BrassSolarPanelBlock.LIT)) {
            stop();
        }
    }
}
