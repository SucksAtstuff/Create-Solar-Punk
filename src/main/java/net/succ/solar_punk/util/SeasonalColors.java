package net.succ.solar_punk.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * Holiday colours for geyser steam, Trellis flowers and the Garden Light's glow. Each returns
 * -1 (leave it alone) unless an active event changes that thing; the first active event in
 * SeasonalEvent's order that has a colour for it wins. Colours that vary per block are picked
 * from the position, so a row of flowers or lights reads as a pattern, not noise. Colours
 * come back opaque (0xFF alpha), as item tints need since 1.21.
 */
public final class SeasonalColors {
    private SeasonalColors() {}

    private static final int[] RAINBOW = {0xE53935, 0xFB8C00, 0xFDD835, 0x43A047, 0x1E88E5, 0x8E24AA};
    private static final int[] PASTELS = {0xFFB3C6, 0xFFE29A, 0xB5EAD7, 0xC7CEEA, 0xE2C2FF, 0xFFD3B6};
    private static final int[] VALENTINE = {0xFF8FC8, 0xFF5C7A};
    private static final int[] HALLOWEEN_STEAM = {0x7BE356, 0xA25CE8};
    private static final int[] HALLOWEEN_FLOWERS = {0xFF8A1F, 0x9B4DE0};
    private static final int[] MARIGOLD = {0xFF9800, 0xFFB300};
    private static final int[] NEW_YEARS = {0xFFD54F, 0xE0E0E0};
    private static final int HALLOWEEN_LIGHT = 0xFF9933;
    private static final int SHAMROCK = 0x5FD068;

    /** Steam colour for one particle, or -1 for plain white steam. */
    public static int steam(RandomSource random) {
        for (SeasonalEvent event : SeasonalEvent.active()) {
            int[] colors = switch (event) {
                case HALLOWEEN -> HALLOWEEN_STEAM;
                case VALENTINES -> VALENTINE;
                case ST_PATRICKS -> new int[]{SHAMROCK};
                case EASTER -> PASTELS;
                case PRIDE -> RAINBOW;
                default -> null;
            };
            if (colors != null) return opaque(colors[random.nextInt(colors.length)]);
        }
        return -1;
    }

    /** New Year's sparklers in the geyser's steam. */
    public static boolean sparklers() {
        return SeasonalEvent.active().contains(SeasonalEvent.NEW_YEARS);
    }

    /** Colour for the flower on a flowering Trellis, or -1 to match the flower that was used. */
    public static int flower(@Nullable BlockPos pos) {
        for (SeasonalEvent event : SeasonalEvent.active()) {
            int color = switch (event) {
                case EASTER -> scatter(PASTELS, pos);
                case HALLOWEEN -> alternate(HALLOWEEN_FLOWERS, pos);
                case DAY_OF_THE_DEAD -> alternate(MARIGOLD, pos);
                case PRIDE -> alternate(RAINBOW, pos);
                default -> -1;
            };
            if (color != -1) return opaque(color);
        }
        return -1;
    }

    /** Colour for a lit Garden Light's glow (pos is null for the item), or -1 for its normal white. */
    public static int gardenLight(@Nullable BlockPos pos) {
        for (SeasonalEvent event : SeasonalEvent.active()) {
            int color = switch (event) {
                case NEW_YEARS -> alternate(NEW_YEARS, pos);
                case VALENTINES -> alternate(VALENTINE, pos);
                case ST_PATRICKS -> SHAMROCK;
                case EASTER -> scatter(PASTELS, pos);
                case HALLOWEEN -> HALLOWEEN_LIGHT;
                case PRIDE -> alternate(RAINBOW, pos);
                default -> -1;
            };
            if (color != -1) return opaque(color);
        }
        return -1;
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    // Steps through the colours one block at a time along every axis: a checkerboard for two
    // colours, diagonal bands for more.
    private static int alternate(int[] colors, @Nullable BlockPos pos) {
        if (pos == null) return colors[0];
        return colors[Math.floorMod(pos.getX() + pos.getY() + pos.getZ(), colors.length)];
    }

    // A fixed but random-looking colour per block.
    private static int scatter(int[] colors, @Nullable BlockPos pos) {
        if (pos == null) return colors[0];
        return colors[(int) Math.floorMod(Mth.getSeed(pos) >> 16, (long) colors.length)];
    }
}
