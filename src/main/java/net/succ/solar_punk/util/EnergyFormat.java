package net.succ.solar_punk.util;

import java.util.Locale;

/**
 * Shortens large Forge Energy (FE) values for display, the way most tech mods do:
 * anything under 1000 prints in full, larger values collapse to a k / M / G / T / P
 * suffix with a couple of significant decimals ({@code 1250 -> "1.25k"},
 * {@code 3_400_000 -> "3.4M"}). Rates are unchanged in magnitude, just abbreviated.
 *
 * <p>Use {@link #fe(long)} / {@link #feRate(long)} for the full display string, or
 * {@link #abbreviate(long)} for the bare number when the unit is added elsewhere.
 */
public final class EnergyFormat {

    private EnergyFormat() {}

    private static final String[] SUFFIXES = {"", "k", "M", "G", "T", "P"};

    /** e.g. {@code "12.5k FE"}. */
    public static String fe(long value) {
        return abbreviate(value) + " FE";
    }

    /** e.g. {@code "80 FE/t"}. */
    public static String feRate(long value) {
        return abbreviate(value) + " FE/t";
    }

    /** Bare abbreviated number, no unit. Values under 1000 are returned as-is. */
    public static String abbreviate(long value) {
        if (value < 0) return "-" + abbreviate(-value);
        if (value < 1000) return Long.toString(value);

        int magnitude = 0;
        double scaled = value;
        while (scaled >= 1000.0 && magnitude < SUFFIXES.length - 1) {
            scaled /= 1000.0;
            magnitude++;
        }

        // More decimals for smaller mantissas so we keep ~3 significant figures.
        String num;
        if (scaled >= 100.0) {
            num = String.format(Locale.ROOT, "%.0f", scaled);
        } else if (scaled >= 10.0) {
            num = String.format(Locale.ROOT, "%.1f", scaled);
        } else {
            num = String.format(Locale.ROOT, "%.2f", scaled);
        }
        if (num.indexOf('.') >= 0) {
            num = num.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return num + SUFFIXES[magnitude];
    }
}