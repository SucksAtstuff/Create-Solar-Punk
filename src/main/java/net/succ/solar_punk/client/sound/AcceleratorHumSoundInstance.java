package net.succ.solar_punk.client.sound;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.ParticleAcceleratorInputBlockEntity;
import net.succ.solar_punk.sound.ModSounds;

// The Particle Accelerator's hum. Its pitch follows the beams: low as a craft starts,
// rising as they speed up, and dropping back after the collision for the next craft.
// When the ring stops it slides down and fades out instead of cutting off.
@OnlyIn(Dist.CLIENT)
public class AcceleratorHumSoundInstance extends MachineSoundInstance {

    // The clip is mastered about as hot as the turbine loop, so it uses the same volume
    private static final float VOLUME = 0.12f;
    private static final float MIN_PITCH = 0.6f, MAX_PITCH = 1.8f;
    private static final int FADE_TICKS = 20;

    private final ParticleAcceleratorInputBlockEntity accelerator;
    private float fade;  // 0 silent, 1 full volume

    public AcceleratorHumSoundInstance(ParticleAcceleratorInputBlockEntity be) {
        super(ModSounds.PARTICLE_ACCELERATOR_HUM.get(), be, 0);
        this.accelerator = be;
        this.pitch = MIN_PITCH;
    }

    // It fades in from silence, and Minecraft skips sounds that start at volume 0
    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    protected boolean isRunning() {
        return accelerator.isRunning();
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || isTooFar()) {
            stop();
            return;
        }
        boolean running = isRunning();
        // The beams speed up in step with progress, so the pitch does too
        float target = running ? MIN_PITCH + (MAX_PITCH - MIN_PITCH) * accelerator.getBeamProgress(1) : MIN_PITCH;
        // ease toward it: quick enough to follow the beams, smooth enough to sweep
        // down after a collision rather than jump
        pitch = Mth.lerp(running ? 0.3f : 0.08f, pitch, target);

        fade = Mth.clamp(fade + (running ? 1f : -1f) / FADE_TICKS, 0, 1);
        volume = VOLUME * fade;
        if (!running && fade <= 0) stop();
    }
}
