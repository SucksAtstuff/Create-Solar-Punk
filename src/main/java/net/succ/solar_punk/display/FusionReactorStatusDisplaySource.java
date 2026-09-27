package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

public class FusionReactorStatusDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        FusionReactorCoreBlockEntity core = FusionReactorDisplayHelper.core(context);
        if (core == null) return EMPTY_LINE;

        return switch (core.getStatus()) {
            case RUNNING      -> Component.literal("Running").withStyle(ChatFormatting.GREEN);
            case INCOMPLETE   -> Component.literal("Incomplete").withStyle(ChatFormatting.RED);
            case NO_BLANKET   -> Component.literal("No blanket modules").withStyle(ChatFormatting.RED);
            case NO_SIGNAL    -> Component.literal("Idle: no redstone signal").withStyle(ChatFormatting.GRAY);
            case LOW_SIGNAL   -> Component.literal("Idle: redstone signal too low").withStyle(ChatFormatting.GRAY);
            case STEAM_FULL   -> Component.literal("Stalled: steam tank full").withStyle(ChatFormatting.YELLOW);
            case NO_DEUTERIUM -> Component.literal("Out of Deuterium").withStyle(ChatFormatting.RED);
            case NO_LITHIUM   -> Component.literal("Out of Lithium").withStyle(ChatFormatting.RED);
        };
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
