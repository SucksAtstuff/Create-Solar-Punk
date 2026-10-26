package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

// A labelable percentage (0-1) read from a block entity. `source` finds the block entity
// to read (or null, which shows nothing).
public class PercentDisplaySource<T> extends PercentOrProgressBarDisplaySource {

    private final Function<DisplayLinkContext, T> source;
    private final Function<T, Float> progress;

    public PercentDisplaySource(Function<DisplayLinkContext, T> source, Function<T, Float> progress) {
        this.source = source;
        this.progress = progress;
    }

    @Override
    @Nullable
    protected Float getProgress(DisplayLinkContext context) {
        T be = source.apply(context);
        return be == null ? null : progress.apply(be);
    }

    @Override
    protected boolean progressBarActive(DisplayLinkContext context) {
        return false;
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
