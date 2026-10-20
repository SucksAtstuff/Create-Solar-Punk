package net.succ.solar_punk.client.sound;

import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

// Shared by the reactor's three Core sounds: the startup and shutdown one-shots
// (looping=false, stopWhenUnformed=false, they just play out) and the running loop
// (looping=true, stopWhenUnformed=true). FusionReactorCoreBlockEntity#tickAudio decides
// which one is playing.
@OnlyIn(Dist.CLIENT)
public class FusionReactorSoundInstance extends MachineSoundInstance {

    private final FusionReactorCoreBlockEntity core;
    private final boolean stopWhenUnformed;
    private boolean stopRequested = false;

    public FusionReactorSoundInstance(SoundEvent sound, FusionReactorCoreBlockEntity be, boolean looping,
                                      boolean stopWhenUnformed, float volume) {
        super(sound, be, volume);
        this.core = be;
        this.looping = looping;
        this.stopWhenUnformed = stopWhenUnformed;
    }

    // The only safe way to stop this from outside. SoundManager#stop(SoundInstance) kills
    // the channel without marking the instance stopped, so a looping sound can keep
    // re-queuing itself. Going through tick() uses the self-stop() the engine respects.
    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void tick() {
        if (stopRequested
                || (stopWhenUnformed && (core.isRemoved() || !core.formed))
                || isTooFar())
            stop();
    }
}
