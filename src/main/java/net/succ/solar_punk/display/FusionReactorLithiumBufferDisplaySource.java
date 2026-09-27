package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

// Registered as "fusion_reactor_lithium_buffer" (kept so existing Display Links stay
// linked) but shows the whole Lithium supply: the buffer alone is at most a few hundred
// mB and reads as empty even with a full stack of Lithium Dust waiting in the slot.
public class FusionReactorLithiumBufferDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        FusionReactorCoreBlockEntity core = FusionReactorDisplayHelper.core(context);
        if (core == null) return ZERO.copy();
        return Component.literal((int) core.getLithiumSupplyMb() + " mB").withStyle(ChatFormatting.AQUA);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
