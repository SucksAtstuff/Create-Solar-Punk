package net.succ.solar_punk.client.sound;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.BiofilterBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

@OnlyIn(Dist.CLIENT)
public class BiofilterSoundInstance extends MachineSoundInstance {

    private final BiofilterBlockEntity biofilter;

    public BiofilterSoundInstance(BiofilterBlockEntity be) {
        super(ModSounds.BIOFILTER_LOOP.get(), be, 0.75f);
        this.biofilter = be;
    }

    // No LIT state on this block; kinetic speed is already synced to the client.
    @Override
    protected boolean isRunning() {
        return biofilter.isPowered();
    }

    @Override
    public void tick() {
        super.tick();
        if (isStopped()) return;
        // Pitch rises with speed so a 16 RPM feed sounds different from a 256 RPM one,
        // clamped so it reads as "spinning faster" rather than a novelty pitch-shift.
        float rpm = Math.abs(biofilter.getSpeed());
        this.pitch = Mth.clamp(0.85f + rpm / 256f * 0.5f, 0.85f, 1.5f);
    }
}
