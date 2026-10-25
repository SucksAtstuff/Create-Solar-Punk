package net.succ.solar_punk.util;

import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.world.level.Level;

// Ponder scenes run machines in their own client-side world. Machines check this before
// playing any sound, so scenes stay silent.
public final class PonderHelper {

    private PonderHelper() {}

    public static boolean isPonder(Level level) {
        return level instanceof PonderLevel;
    }
}
