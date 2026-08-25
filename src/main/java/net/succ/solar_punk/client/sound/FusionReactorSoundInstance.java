package net.succ.solar_punk.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

// Positional sound instance shared by all three of the reactor's Core sounds - the
// startup and shutdown one-shots (looping=false, stopWhenUnformed=false - they just
// play out on their own) and the running loop (looping=true, stopWhenUnformed=true, the
// same "stop once the condition driving it goes away" shape as BrassPanelSoundInstance
// uses for the Brass Solar Panel's LIT state). See
// FusionReactorCoreBlockEntity#tickAudio for the startup -> loop -> shutdown sequencing
// that decides which of the three is currently playing.
@OnlyIn(Dist.CLIENT)
public class FusionReactorSoundInstance extends AbstractTickableSoundInstance {

    private final FusionReactorCoreBlockEntity blockEntity;
    private final boolean stopWhenUnformed;

    // Set by FusionReactorCoreBlockEntity#tickAudio to cut this instance off early (e.g.
    // a startup clip getting interrupted by a shutdown). Deliberately NOT acted on
    // immediately by the caller - see requestStop().
    private boolean stopRequested = false;

    public FusionReactorSoundInstance(SoundEvent sound, FusionReactorCoreBlockEntity be, boolean looping, boolean stopWhenUnformed, float volume) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = be;
        this.stopWhenUnformed = stopWhenUnformed;
        this.looping = looping;
        this.delay = 0;
        // Well below BrassPanelSoundInstance's 0.75 - the raw clips are mastered much
        // hotter than that ambient buzz, so the same multiplier came out deafening. Now
        // per-sound rather than one flat 0.25 for all three - measured via ffmpeg
        // volumedetect against electric_motor_buzz.ogg the same way every other
        // ambience's volume was tuned (see FusionReactorCoreBlockEntity#tickAudio for the
        // actual numbers), since the loop clip runs ~9 dB hotter than the two one-shots
        // and was still noticeably louder than the rest of the mod's ambience at the old
        // shared value.
        this.volume = volume;
        this.pitch = 1.0f;
        this.x = be.getBlockPos().getX() + 0.5;
        this.y = be.getBlockPos().getY() + 0.5;
        this.z = be.getBlockPos().getZ() + 0.5;
        this.attenuation = Attenuation.LINEAR;
    }

    // The only supported way to cut this instance off from outside the class - stop()
    // itself is protected, and more importantly, calling it indirectly via
    // SoundManager#stop(SoundInstance) only kills the audio channel without marking this
    // instance's own `stopped` state. For a looping TickableSoundInstance in particular,
    // the sound engine's per-tick bookkeeping decides whether to keep it alive based on
    // isStopped() - not on whether some channel got killed out from under it - so an
    // external stop() call can leave a looping sound re-queuing itself forever. Routing
    // every stop through this flag, checked from within tick() below, means it always
    // goes through the same self-stop() call the engine actually respects.
    public void requestStop() {
        stopRequested = true;
    }

    @Override
    public void tick() {
        if (stopRequested
                || (stopWhenUnformed && (blockEntity.isRemoved() || !blockEntity.formed))
                || AmbientSoundRange.isTooFar(x, y, z))
            stop();
    }
}
