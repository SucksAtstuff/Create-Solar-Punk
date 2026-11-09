package net.succ.solar_punk.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Holidays the mod dresses up for, by the real-world date on the machine running it. Purely
 * cosmetic: see client/SeasonalModels for the model and texture swaps, and SeasonalColors for
 * the steam, flower and light colours.
 * <p>
 * Declared in priority order. Where two events share a day, each block or effect takes the
 * first active event that changes it, so New Year's wins over Christmas on Dec 31 for the
 * things New Year's changes, and the rest stay Christmassy.
 */
public enum SeasonalEvent {
    NEW_YEARS(date -> is(date, Month.DECEMBER, 31) || is(date, Month.JANUARY, 1)),
    VALENTINES(date -> is(date, Month.FEBRUARY, 14)),
    ST_PATRICKS(date -> is(date, Month.MARCH, 17)),
    // Good Friday to Easter Monday.
    EASTER(date -> {
        LocalDate sunday = easterSunday(date.getYear());
        return !date.isBefore(sunday.minusDays(2)) && !date.isAfter(sunday.plusDays(1));
    }),
    // The last week of October.
    HALLOWEEN(date -> date.getMonth() == Month.OCTOBER && date.getDayOfMonth() >= 25),
    DAY_OF_THE_DEAD(date -> is(date, Month.NOVEMBER, 1) || is(date, Month.NOVEMBER, 2)),
    CHRISTMAS(date -> date.getMonth() == Month.DECEMBER),
    PRIDE(date -> date.getMonth() == Month.JUNE);

    private static final SeasonalEvent[] VALUES = values();

    private final Predicate<LocalDate> dates;

    SeasonalEvent(Predicate<LocalDate> dates) {
        this.dates = dates;
    }

    /** Folder the event's models and textures live in: block/events/<id>/. */
    public String id() {
        return name().toLowerCase();
    }

    // -Dsolarpunk.event=christmas forces an event on, for testing outside its dates.
    public boolean isActive() {
        return id().equals(System.getProperty("solarpunk.event")) || dates.test(LocalDate.now());
    }

    /** Every event on today, in priority order. Asked for per block colour, so cached per day. */
    public static List<SeasonalEvent> active() {
        long today = LocalDate.now().toEpochDay();
        Cache cache = cached;
        if (cache == null || cache.day != today) {
            cache = new Cache(today, Arrays.stream(VALUES).filter(SeasonalEvent::isActive).toList());
            cached = cache;
        }
        return cache.events;
    }

    private record Cache(long day, List<SeasonalEvent> events) {}

    private static volatile Cache cached;

    private static boolean is(LocalDate date, Month month, int day) {
        return MonthDay.from(date).equals(MonthDay.of(month, day));
    }

    // Anonymous Gregorian algorithm (Meeus/Jones/Butcher).
    static LocalDate easterSunday(int year) {
        int a = year % 19, b = year / 100, c = year % 100;
        int d = b / 4, e = b % 4, f = (b + 8) / 25, g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }
}
