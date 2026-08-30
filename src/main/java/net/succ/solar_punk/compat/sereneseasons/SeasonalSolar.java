package net.succ.solar_punk.compat.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.succ.solar_punk.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Soft compat for Serene Seasons - no Gradle dependency, same reflection-only approach
 * as {@link net.succ.solar_punk.compat.sable.SableCompat}. Every solar generator
 * multiplies its output by {@link #outputMultiplier(Level, BlockPos)}: {@code 1.0} when
 * Serene Seasons is absent, disabled in the config, or anything goes wrong.
 *
 * <p>Output peaks at midsummer and bottoms out at midwinter, following a smooth cosine
 * over Serene Seasons' twelve sub-seasons. Hot/tropical biomes (desert, savanna,
 * badlands, jungle) barely swing at all, matching Serene Seasons' own treatment of them
 * as seasonless - so the mod's desert-based solar content is unaffected and the effect
 * only really bites in temperate and cold biomes.
 */
public final class SeasonalSolar {

    private SeasonalSolar() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/sereneseasons");

    private static final boolean LOADED = ModList.get().isLoaded("sereneseasons");

    // MID_SUMMER sits at index 4, MID_WINTER at index 10 - see subSeasonIndex().
    private static final int SUMMER_PEAK_INDEX = 4;

    private static boolean triedInit = false;
    private static Method mGetSeasonState; // sereneseasons.api.season.SeasonHelper#getSeasonState(Level)
    private static Method mGetSubSeason;   // resolved from the returned ISeasonState instance

    // The season only advances every few in-game days, so there's no point reflecting
    // it every time a kinetic block asks for its speed - recompute at most once a second.
    private static long cacheStampSeconds = Long.MIN_VALUE;
    private static Object cacheDimension = null;
    private static double cacheSeasonFactor = 1.0;

    /**
     * Output multiplier for a solar generator at {@code pos}. Returns {@code 1.0f} unless
     * Serene Seasons is installed, {@code generators.serene_seasons.enabled} is true, and
     * the generator is in the Overworld.
     */
    public static float outputMultiplier(Level level, BlockPos pos) {
        if (level == null || pos == null) return 1.0f;
        if (!LOADED || !Config.sereneSeasonsEnabled) return 1.0f;
        // Serene Seasons manages the Overworld by default; other dimensions are left flat.
        if (!level.dimension().equals(Level.OVERWORLD)) return 1.0f;

        double seasonFactor = seasonFactor(level);
        if (seasonFactor == 1.0) return 1.0f;

        // "Latitude" proxy: hot biomes are seasonless in Serene Seasons, so fade the swing
        // out toward 1.0 as biome base temperature climbs past temperate (0.8) to hot (1.2).
        float temperature = level.getBiome(pos).value().getBaseTemperature();
        double tropicalBlend = Mth.clamp((temperature - 0.8) / 0.4, 0.0, 1.0);

        double multiplier = 1.0 + (seasonFactor - 1.0) * (1.0 - tropicalBlend);
        return (float) Mth.clamp(multiplier, 0.3, 1.5);
    }

    private static double seasonFactor(Level level) {
        long nowSeconds = level.getGameTime() / 20L;
        Object dimension = level.dimension();
        if (nowSeconds == cacheStampSeconds && dimension.equals(cacheDimension)) {
            return cacheSeasonFactor;
        }
        cacheStampSeconds = nowSeconds;
        cacheDimension = dimension;
        cacheSeasonFactor = computeSeasonFactor(level);
        return cacheSeasonFactor;
    }

    private static synchronized void init() {
        if (triedInit) return;
        triedInit = true;
        try {
            Class<?> seasonHelper = Class.forName("sereneseasons.api.season.SeasonHelper");
            mGetSeasonState = seasonHelper.getMethod("getSeasonState", Level.class);
        } catch (Throwable t) {
            LOGGER.info("[SolarPunk] Serene Seasons is installed but its season API wasn't found ({}); "
                    + "seasonal solar scaling is off.", t.toString());
            mGetSeasonState = null;
        }
    }

    private static double computeSeasonFactor(Level level) {
        if (!triedInit) init();
        if (mGetSeasonState == null) return 1.0;
        try {
            Object state = mGetSeasonState.invoke(null, level);
            if (state == null) return 1.0;
            if (mGetSubSeason == null) mGetSubSeason = state.getClass().getMethod("getSubSeason");
            Object subSeason = mGetSubSeason.invoke(state);
            if (!(subSeason instanceof Enum<?> e)) return 1.0;

            int index = subSeasonIndex(e.name());
            if (index < 0) return 1.0;

            double summer = Config.sereneSeasonsSummerMultiplier;
            double winter = Config.sereneSeasonsWinterMultiplier;
            double mid = (summer + winter) / 2.0;
            double amplitude = (summer - winter) / 2.0;
            double phase = 2.0 * Math.PI * (index - SUMMER_PEAK_INDEX) / 12.0;
            return mid + amplitude * Math.cos(phase);
        } catch (Throwable t) {
            LOGGER.warn("[SolarPunk] Serene Seasons season lookup failed ({}); seasonal solar scaling is off.",
                    t.toString());
            mGetSeasonState = null;
            return 1.0;
        }
    }

    /** Serene Seasons' {@code Season.SubSeason} enum order, matched by name so a reorder can't break it. */
    private static int subSeasonIndex(String name) {
        return switch (name) {
            case "EARLY_SPRING"  -> 0;
            case "MID_SPRING"    -> 1;
            case "LATE_SPRING"   -> 2;
            case "EARLY_SUMMER"  -> 3;
            case "MID_SUMMER"    -> 4;
            case "LATE_SUMMER"   -> 5;
            case "EARLY_AUTUMN"  -> 6;
            case "MID_AUTUMN"    -> 7;
            case "LATE_AUTUMN"   -> 8;
            case "EARLY_WINTER"  -> 9;
            case "MID_WINTER"    -> 10;
            case "LATE_WINTER"   -> 11;
            default -> -1;
        };
    }
}