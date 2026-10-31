package net.succ.solar_punk.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.compat.cosmonautics.CosmonauticsSolar;
import net.succ.solar_punk.compat.sable.SableCompat;
import net.succ.solar_punk.compat.sereneseasons.SeasonalSolar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/**
 * Shared sunlight checks for every solar block. All of them go through here so space
 * dimensions (see {@link ModTags.DimensionTypes} and {@link CosmonauticsSolar}) and blocks on
 * Sable sub-levels (see {@link SableCompat#placement}) behave the same everywhere.
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

    // Space and sub-level sky checks walk up to SKY_SCAN_LIMIT blocks and Brass Panels ask every tick,
    // so results are reused for a second. Concurrent since in singleplayer the client
    // thread (goggles) and the server thread both check.
    private static final int SKY_CACHE_TICKS = 20;
    private static final int SKY_CACHE_MAX = 4096;
    private record SkyResult(boolean clear, long stamp) {}
    private static final Map<GlobalPos, SkyResult> SKY_CACHE = new ConcurrentHashMap<>();

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

        // On a contraption the sun's height is measured from the contraption's own "up", so
        // tilting it toward the sun gives more output. Still night once the sun has set.
        SableCompat.Placement placement = SableCompat.placement(level, pos);
        if (placement != null) {
            Vec3 world = vanillaSunDirection(level);
            if (world.y < DAY_SUN_HEIGHT) return DayPhase.NIGHT;
            double height = placement.toLocal(world).y;
            if (height >= NOON_SUN_HEIGHT) return DayPhase.NOON;
            // The sun rises in the east (+x).
            if (height >= DAY_SUN_HEIGHT)  return world.x > 0 ? DayPhase.MORNING : DayPhase.EVENING;
            return DayPhase.NIGHT;
        }

        float progress = dayProgress(level.getTimeOfDay(1.0f));
        if (progress < NOON_PROGRESS)    return DayPhase.MORNING;
        if (progress < EVENING_PROGRESS) return DayPhase.NOON;
        if (progress < NIGHT_PROGRESS)   return DayPhase.EVENING;
        return DayPhase.NIGHT;
    }

    /**
     * A solar block's base output for the current sunlight: {@code dawn} in the morning
     * and evening (and at noon in the rain), {@code noon} at a clear noon, and 0 at night
     * or with the sky blocked. Seasons and space aren't applied, see {@link #outputMultiplier}.
     */
    public static float byPhase(Level level, BlockPos pos, float dawn, float noon) {
        if (!hasSkyAccess(level, pos)) return 0;
        return switch (getPhase(level, pos)) {
            case MORNING, EVENING -> dawn;
            case NOON -> isRaining(level) ? dawn : noon;
            case NIGHT -> 0;
        };
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
     *
     * <p>Elsewhere a block on a sub-level (e.g. an Aeronautics contraption) needs a clear line
     * toward the sun through its own contraption, and the sky has to be open above where the
     * contraption really is in the world, so flying it indoors or underground shades it.
     * Any block, on a contraption or not, is also shaded by another contraption above it.
     */
    public static boolean canSeeSky(Level level, BlockPos pos) {
        if (level == null) return false;

        CosmonauticsSolar.Sun sun = CosmonauticsSolar.sunAt(level, pos);
        if (sun != null) {
            Vec3 toward = sun.visible() && sun.direction().y > 0 ? sun.direction() : new Vec3(0, 1, 0);
            return cached(level, pos, () -> rayClear(level, pos, toward));
        }

        if (!isAlwaysSunny(level)) {
            SableCompat.Placement placement = SableCompat.placement(level, pos);
            if (placement != null) return cached(level, pos, () -> subLevelSkyClear(level, pos, placement));
        }

        if (!isAlwaysSunny(level)) {
            return level.canSeeSky(pos)
                    && cached(level, pos, () -> !shadedBySubLevels(level, Vec3.atCenterOf(pos), null));
        }

        if (level.canSeeSky(pos)) return true;
        return cached(level, pos, () -> rayClear(level, pos, new Vec3(0, 1, 0)));
    }

    private static boolean subLevelSkyClear(Level level, BlockPos pos, SableCompat.Placement placement) {
        // Through the contraption itself, in its own frame: toward the sun while it's up,
        // otherwise along the contraption's "up".
        Vec3 sun = placement.toLocal(vanillaSunDirection(level));
        Vec3 toward = sun.y > 0 ? sun : new Vec3(0, 1, 0);
        if (!rayClear(level, pos, toward)) return false;

        // Then the real world above it. An unloaded chunk can't shade it.
        BlockPos worldPos = placement.worldPos();
        if (level.isLoaded(worldPos) && !level.canSeeSky(worldPos)) return false;
        return !shadedBySubLevels(level, placement.worldCenter(), placement.subLevel());
    }

    /**
     * True if a sub-level other than {@code exclude} has a skylight-blocking block in the
     * column straight up from {@code start} (world space). Straight up, like the vanilla sky
     * check, so parking under a ship shades a block whatever the time of day.
     */
    private static boolean shadedBySubLevels(Level level, Vec3 start, Object exclude) {
        Vec3 end = start.add(0, SKY_SCAN_LIMIT, 0);
        for (SableCompat.PlotRay ray : SableCompat.raysThroughSubLevels(level, start, end, exclude)) {
            Vec3 along = ray.end().subtract(ray.start());
            if (!rayClear(level, ray.start(), along.normalize(), along.length())) return true;
        }
        return false;
    }

    public static boolean isRaining(Level level) {
        return level != null && !hasNoWeather(level) && level.isRaining();
    }

    public static boolean isThundering(Level level) {
        return level != null && !hasNoWeather(level) && level.isThundering();
    }

    /** Seasonal and space output multipliers combined. */
    public static float outputMultiplier(Level level, BlockPos pos) {
        // Seasons depend on the biome, so use where a contraption really is.
        SableCompat.Placement placement = SableCompat.placement(level, pos);
        float multiplier = SeasonalSolar.outputMultiplier(level, placement != null ? placement.worldPos() : pos);
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

    private static boolean cached(Level level, BlockPos pos, BooleanSupplier check) {
        long now = level.getGameTime();
        GlobalPos key = GlobalPos.of(level.dimension(), pos.immutable());
        SkyResult cached = SKY_CACHE.get(key);
        // now < stamp: a different world was loaded since (singleplayer), so it's stale.
        if (cached != null && now >= cached.stamp() && now - cached.stamp() < SKY_CACHE_TICKS) return cached.clear();

        if (SKY_CACHE.size() >= SKY_CACHE_MAX) SKY_CACHE.clear();
        boolean clear = check.getAsBoolean();
        SKY_CACHE.put(key, new SkyResult(clear, now));
        return clear;
    }

    private static boolean rayClear(Level level, BlockPos start, Vec3 dir) {
        return rayClear(level, Vec3.atCenterOf(start), dir.normalize(), SKY_SCAN_LIMIT);
    }

    /**
     * Walks every cell a ray from {@code origin} passes through (its own cell included) for
     * {@code length} blocks along the unit vector {@code dir}, and fails on the first one
     * skylight can't pass straight down through - the same rule vanilla's sky check uses,
     * so glass is fine but solid blocks aren't.
     */
    private static boolean rayClear(Level level, Vec3 origin, Vec3 dir, double length) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(origin).mutable();
        if (!level.getBlockState(cursor).propagatesSkylightDown(level, cursor)) return false;

        int stepX = (int) Math.signum(dir.x), stepY = (int) Math.signum(dir.y), stepZ = (int) Math.signum(dir.z);
        double deltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.x);
        double deltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.y);
        double deltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(dir.z);
        // Distance along the ray to the first boundary on each axis.
        double maxX = boundary(origin.x, stepX, deltaX);
        double maxY = boundary(origin.y, stepY, deltaY);
        double maxZ = boundary(origin.z, stepZ, deltaZ);

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
            if (t > length || level.isOutsideBuildHeight(cursor.getY())) return true;
            if (!level.getBlockState(cursor).propagatesSkylightDown(level, cursor)) return false;
        }
    }

    /** Distance along the ray until it crosses a cell boundary on one axis. */
    private static double boundary(double coord, int step, double delta) {
        if (step == 0) return Double.POSITIVE_INFINITY;
        double frac = coord - Math.floor(coord);
        return (step > 0 ? 1 - frac : frac) * delta;
    }

    /**
     * Unit vector toward the vanilla sun in world space. It rises in the east (+x), passes
     * overhead at noon and sets in the west, the same path the sky renderer draws it on.
     */
    private static Vec3 vanillaSunDirection(Level level) {
        double angle = level.getTimeOfDay(1.0f) * Mth.TWO_PI;
        return new Vec3(-Math.sin(angle), Math.cos(angle), 0);
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
