package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.compat.cosmonautics.CosmonauticsSolar;
import net.succ.solar_punk.compat.sereneseasons.SeasonalSolar;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared sunlight checks for every solar block. All of them go through here so space
 * dimensions (see {@link ModTags.DimensionTypes} and {@link CosmonauticsSolar}) behave the
 * same everywhere.
 */
public final class SolarHelper {
    private SolarHelper() {}

    private static final long NOON_START    = 2000;
    private static final long EVENING_START = 10000;
    private static final long NIGHT_START   = 12000;

    // Phase boundaries in getTimeOfDay() space. That value is smoothed by vanilla, so the
    // tick boundaries are run through the same curve instead of inverting it.
    private static final float SUNRISE          = vanillaTimeOfDay(0);
    private static final float NOON_PROGRESS    = dayProgress(vanillaTimeOfDay(NOON_START));
    private static final float EVENING_PROGRESS = dayProgress(vanillaTimeOfDay(EVENING_START));
    private static final float NIGHT_PROGRESS   = dayProgress(vanillaTimeOfDay(NIGHT_START));

    // The same boundaries as sun height (y of the unit vector toward the sun) for when the
    // real sun direction is known, e.g. in Cosmonautics deep space. Vanilla's sun height is
    // cos(timeOfDay * 2pi), so a sun at a given height gives the same phase as it would in
    // the Overworld.
    private static final double NOON_SUN_HEIGHT = Math.cos(vanillaTimeOfDay(NOON_START) * Mth.TWO_PI);
    private static final double DAY_SUN_HEIGHT  = Math.cos(vanillaTimeOfDay(NIGHT_START) * Mth.TWO_PI);

    // How far the space sky check scans for blocking blocks. Station roofs are rarely
    // further than this from a panel, and it bounds the cost.
    private static final int SKY_SCAN_LIMIT = 64;

    // Space sky checks walk up to SKY_SCAN_LIMIT blocks and Brass Panels ask every tick,
    // so results are reused for a second.
    private static final int SKY_CACHE_TICKS = 20;
    private static final int SKY_CACHE_MAX = 4096;
    private record SkyResult(boolean clear, long stamp) {}
    private static final Map<GlobalPos, SkyResult> SKY_CACHE = new HashMap<>();

    public enum DayPhase { NIGHT, MORNING, NOON, EVENING }

    public static DayPhase getPhase(Level level, BlockPos pos) {
        if (level == null) return DayPhase.NIGHT;

        CosmonauticsSolar.Sun sun = CosmonauticsSolar.sunAt(level, pos);
        if (sun != null) {
            if (!sun.visible()) return DayPhase.NIGHT;
            double height = sun.direction().y;
            if (height >= NOON_SUN_HEIGHT) return DayPhase.NOON;
            if (height >= DAY_SUN_HEIGHT)  return DayPhase.MORNING;
            return DayPhase.NIGHT;
        }

        if (isAlwaysSunny(level)) return DayPhase.NOON;
        float progress = dayProgress(level.getTimeOfDay(1.0f));
        if (progress < NOON_PROGRESS)    return DayPhase.MORNING;
        if (progress < EVENING_PROGRESS) return DayPhase.NOON;
        if (progress < NIGHT_PROGRESS)   return DayPhase.EVENING;
        return DayPhase.NIGHT;
    }

    /** True during the daylight half of the cycle (always true in always-sunny dimensions). */
    public static boolean isDaytime(Level level, BlockPos pos) {
        return getPhase(level, pos) != DayPhase.NIGHT;
    }

    /** Sky access for a block at {@code pos}, checked from the cell directly above it. */
    public static boolean hasSkyAccess(Level level, BlockPos pos) {
        return level != null && canSeeSky(level, pos.above());
    }

    /**
     * {@link Level#canSeeSky}, extended for space. Where the real sun is known (Cosmonautics
     * deep space) a line toward it has to be clear, so a station's own hull can shade its
     * panels; while the sun is down it checks straight up, like the Overworld does at night.
     * Other always-sunny dimensions also accept a clear column straight up, since stations
     * live on Sable sub-levels where vanilla skylight isn't reliable.
     */
    public static boolean canSeeSky(Level level, BlockPos pos) {
        if (level == null) return false;

        CosmonauticsSolar.Sun sun = CosmonauticsSolar.sunAt(level, pos);
        if (sun != null) {
            Vec3 toward = sun.visible() && sun.direction().y > 0 ? sun.direction() : new Vec3(0, 1, 0);
            return cachedRayClear(level, pos, toward);
        }

        if (level.canSeeSky(pos)) return true;
        if (!isAlwaysSunny(level)) return false;
        return cachedRayClear(level, pos, new Vec3(0, 1, 0));
    }

    public static boolean isRaining(Level level) {
        return level != null && !hasNoWeather(level) && level.isRaining();
    }

    public static boolean isThundering(Level level) {
        return level != null && !hasNoWeather(level) && level.isThundering();
    }

    /** Seasonal and space output multipliers combined. */
    public static float outputMultiplier(Level level, BlockPos pos) {
        float multiplier = SeasonalSolar.outputMultiplier(level, pos);
        if (isAlwaysSunny(level)) multiplier *= (float) Config.spaceSolarMultiplier;
        return multiplier;
    }

    public static boolean isAlwaysSunny(Level level) {
        return level != null && level.dimensionTypeRegistration().is(ModTags.DimensionTypes.ALWAYS_SUNNY);
    }

    public static boolean hasNoWeather(Level level) {
        return level != null && (isAlwaysSunny(level)
                || level.dimensionTypeRegistration().is(ModTags.DimensionTypes.NO_WEATHER));
    }

    private static boolean cachedRayClear(Level level, BlockPos start, Vec3 dir) {
        long now = level.getGameTime();
        GlobalPos key = GlobalPos.of(level.dimension(), start.immutable());
        SkyResult cached = SKY_CACHE.get(key);
        // now < stamp: a different world was loaded since (singleplayer), so it's stale.
        if (cached != null && now >= cached.stamp() && now - cached.stamp() < SKY_CACHE_TICKS) return cached.clear();

        if (SKY_CACHE.size() >= SKY_CACHE_MAX) SKY_CACHE.clear();
        boolean clear = rayClear(level, start, dir);
        SKY_CACHE.put(key, new SkyResult(clear, now));
        return clear;
    }

    /**
     * Walks every cell a ray from the centre of {@code start} passes through (start
     * included) and fails on the first one skylight can't pass straight down through -
     * the same rule vanilla's sky check uses, so glass is fine but solid blocks aren't.
     */
    private static boolean rayClear(Level level, BlockPos start, Vec3 dir) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        if (!level.getBlockState(cursor).propagatesSkylightDown(level, cursor)) return false;

        int stepX = (int) Math.signum(dir.x), stepY = (int) Math.signum(dir.y), stepZ = (int) Math.signum(dir.z);
        double deltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.x);
        double deltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.y);
        double deltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.z);
        // Starting from the cell centre, every boundary is half a cell away.
        double maxX = deltaX / 2, maxY = deltaY / 2, maxZ = deltaZ / 2;

        while (true) {
            double t;
            if (maxX <= maxY && maxX <= maxZ) {
                t = maxX;
                maxX += deltaX;
                cursor.move(stepX, 0, 0);
            } else if (maxY <= maxZ) {
                t = maxY;
                maxY += deltaY;
                cursor.move(0, stepY, 0);
            } else {
                t = maxZ;
                maxZ += deltaZ;
                cursor.move(0, 0, stepZ);
            }
            if (t > SKY_SCAN_LIMIT || level.isOutsideBuildHeight(cursor.getY())) return true;
            if (!level.getBlockState(cursor).propagatesSkylightDown(level, cursor)) return false;
        }
    }

    /**
     * Shifts a {@link Level#getTimeOfDay} value so the cycle starts at sunrise (day tick 0)
     * and increases monotonically. Phases use getTimeOfDay rather than getDayTime so mods
     * that override the sun position (e.g. Create: Cosmonautics on its Moon) are respected.
     */
    private static float dayProgress(float timeOfDay) {
        return Mth.frac(timeOfDay - SUNRISE);
    }

    /** Same curve as {@code DimensionType#timeOfDay} without a fixed time. */
    private static float vanillaTimeOfDay(long ticks) {
        double d = Mth.frac(ticks / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) (d * 2.0 + e) / 3.0f;
    }
}
