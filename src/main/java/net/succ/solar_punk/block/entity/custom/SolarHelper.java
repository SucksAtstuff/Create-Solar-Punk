package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class SolarHelper {
    private SolarHelper() {}

    private static final long NOON_START    = 2000;
    private static final long EVENING_START = 10000;
    private static final long NIGHT_START   = 12000;

    public enum DayPhase { NIGHT, MORNING, NOON, EVENING }

    public static DayPhase getPhase(Level level) {
        if (level == null) return DayPhase.NIGHT;
        long time = level.getDayTime() % 24000;
        if (time < NOON_START)    return DayPhase.MORNING;
        if (time < EVENING_START) return DayPhase.NOON;
        if (time < NIGHT_START)   return DayPhase.EVENING;
        return DayPhase.NIGHT;
    }

    public static boolean hasSkyAccess(Level level, BlockPos pos) {
        return level != null && level.canSeeSky(pos.above());
    }
}
