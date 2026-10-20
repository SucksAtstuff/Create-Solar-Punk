package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

// A labelable single-line number read from a block entity. `source` finds the block
// entity to read (or null, which shows 0) and `line` formats the value.
public class NumericLineDisplaySource<T> extends NumericSingleLineDisplaySource {

    private final Function<DisplayLinkContext, T> source;
    private final Function<T, MutableComponent> line;

    public NumericLineDisplaySource(Function<DisplayLinkContext, T> source, Function<T, MutableComponent> line) {
        this.source = source;
        this.line = line;
    }

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        T be = source.apply(context);
        return be == null ? ZERO.copy() : line.apply(be);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
