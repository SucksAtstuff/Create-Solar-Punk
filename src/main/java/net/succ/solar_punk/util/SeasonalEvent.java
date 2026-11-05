package net.succ.solar_punk.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Holidays the mod dresses up for, by the real-world date on the machine running it. Purely
 * cosmetic: see client/SeasonalModels for the model swaps.
 */
public enum SeasonalEvent {
    CHRISTMAS(Month.DECEMBER);

    private final Month month;

    SeasonalEvent(Month month) {
        this.month = month;
    }

    /** Folder the event's models and textures live in: block/events/<id>/. */
    public String id() {
        return name().toLowerCase();
    }

    // -Dsolarpunk.event=christmas forces an event on, for testing outside its dates.
    public boolean isActive() {
        return id().equals(System.getProperty("solarpunk.event")) || LocalDate.now().getMonth() == month;
    }
}
