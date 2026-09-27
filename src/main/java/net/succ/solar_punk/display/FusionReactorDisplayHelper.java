package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;
import org.jetbrains.annotations.Nullable;

// The Core is buried at dead center once the reactor is built, so every Fusion Reactor
// display source is registered on the Casing shell (Casing and Casing Glass share one
// block entity type) and reaches the Core through the Casing's own proxy.
final class FusionReactorDisplayHelper {

    private FusionReactorDisplayHelper() {}

    @Nullable
    static FusionReactorCoreBlockEntity core(DisplayLinkContext context) {
        if (context.getSourceBlockEntity() instanceof FusionReactorCasingBlockEntity casing)
            return casing.core();
        if (context.getSourceBlockEntity() instanceof FusionReactorCoreBlockEntity core)
            return core;
        return null;
    }
}
