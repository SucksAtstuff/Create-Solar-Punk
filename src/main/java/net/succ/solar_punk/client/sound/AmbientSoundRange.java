package net.succ.solar_punk.client.sound;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// Every looping machine-ambience SoundInstance in this package sets Attenuation.LINEAR,
// which by Minecraft's own SoundEngine#play() math (f1 = Math.max(volume, 1.0F) *
// sound.getAttenuationDistance()) should hard-cap audible range at ~16 blocks by
// default (attenuation_distance defaults to 16 when unset in sounds.json, same as every
// entry in this mod's own sounds.json) - that part of the setup is correct and matches
// BrassPanelSoundInstance's already-working pattern exactly. Reported anyway: these
// sounds audible from 200+ blocks away, well past where native OpenAL falloff should
// have silenced them, on a setup already known to run Sable (which mixes into camera/
// listener-position handling for its sublevel rendering - see SableCompat). Rather than
// keep guessing at what's overriding native attenuation on that specific setup, this is
// a hard client-side guarantee on top of it: every ambience SoundInstance checks this
// before staying alive each tick, so however the native falloff gets bypassed, the
// sound still can't be heard from far away.
//
// Originally 48 (3x attenuation_distance) on the assumption native falloff was still
// doing the real work and this was just a generous backstop. Confirmed it isn't - still
// clearly audible at 30 blocks, well inside that 48-block budget - so this is now the
// only thing actually controlling range, not a backstop. Tightened to 20 (a little
// headroom over the intended ~16 rather than a hard edge exactly on it).
public final class AmbientSoundRange {

    private static final double MAX_DISTANCE_SQ = 20.0 * 20.0;

    @OnlyIn(Dist.CLIENT)
    public static boolean isTooFar(double x, double y, double z) {
        var player = Minecraft.getInstance().player;
        return player == null || player.distanceToSqr(x, y, z) > MAX_DISTANCE_SQ;
    }

    private AmbientSoundRange() {}
}
