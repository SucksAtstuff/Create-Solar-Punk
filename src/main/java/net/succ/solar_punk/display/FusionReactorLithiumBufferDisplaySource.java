package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

public class FusionReactorLithiumBufferDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Float bufferMb = null;
        if (context.getSourceBlockEntity() instanceof FusionReactorCoreBlockEntity core)
            bufferMb = core.getLithiumBufferMb();
        else if (context.getSourceBlockEntity() instanceof FusionReactorCasingBlockEntity casing)
            bufferMb = casing.getLithiumBufferMb();

        if (bufferMb == null) return ZERO.copy();
        return Component.literal((int) (float) bufferMb + " mB").withStyle(ChatFormatting.AQUA);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
