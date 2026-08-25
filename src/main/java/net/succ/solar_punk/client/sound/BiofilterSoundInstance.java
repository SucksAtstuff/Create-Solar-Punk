package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BiofilterBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BiofilterSoundInstance extends AbstractTickableSoundInstance {

    private final BiofilterBlockEntity blockEntity;

    public BiofilterSoundInstance(BiofilterBlockEntity be) {
        super(ModSounds.BIOFILTER_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
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
        // No LIT blockstate on this block - isPowered() is just Math.abs(getSpeed()) > 0,
        // and kinetic speed is already synced client-side for shaft rendering, so it's
        // safe to read directly here without needing a dedicated blockstate property.
        if (blockEntity.isRemoved() || !blockEntity.isPowered() || AmbientSoundRange.isTooFar(x, y, z)) {
            stop();
            return;
        }

        // Pitch tracks rotation speed - a lazy 16 RPM feed should sound noticeably
        // different from a maxed-out 256 RPM network, same reference point Config's own
        // biofilter_absorption_per_rpm comment already uses ("16 RPM Biofilter at the old
        // flat-rate baseline... a 256 RPM one reaches 160/s"). Clamped well short of
        // chipmunk territory - this should read as "spinning faster", not a novelty
        // pitch-shift.
        float rpm = Math.abs(blockEntity.getSpeed());
        this.pitch = Mth.clamp(0.85f + rpm / 256f * 0.5f, 0.85f, 1.5f);
    }
}
