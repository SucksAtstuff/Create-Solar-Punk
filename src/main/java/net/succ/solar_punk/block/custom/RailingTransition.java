package net.succ.solar_punk.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

/**
 * What a flat {@link FiligreeRailingBlock} shows where it meets a gentle slope: nothing, or the
 * START or END transition piece, half flat and half slope. START sits behind a run's first UP
 * piece (the slope's low end), END in front of and one block above its last UP piece (the high
 * end). The direction is the way the slope climbs, which is how the models are turned.
 */
public enum RailingTransition implements StringRepresentable {
    NONE(null, null),
    START_NORTH(Kind.START, Direction.NORTH), START_EAST(Kind.START, Direction.EAST),
    START_SOUTH(Kind.START, Direction.SOUTH), START_WEST(Kind.START, Direction.WEST),
    END_NORTH(Kind.END, Direction.NORTH), END_EAST(Kind.END, Direction.EAST),
    END_SOUTH(Kind.END, Direction.SOUTH), END_WEST(Kind.END, Direction.WEST);

    public enum Kind { START, END }

    @Nullable public final Kind kind;
    @Nullable public final Direction climb;

    RailingTransition(@Nullable Kind kind, @Nullable Direction climb) {
        this.kind = kind;
        this.climb = climb;
    }

    public static RailingTransition of(Kind kind, Direction climb) {
        for (RailingTransition t : values()) {
            if (t.kind == kind && t.climb == climb) return t;
        }
        return NONE;
    }

    public RailingTransition rotated(Direction newClimb) {
        return kind == null ? NONE : of(kind, newClimb);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
