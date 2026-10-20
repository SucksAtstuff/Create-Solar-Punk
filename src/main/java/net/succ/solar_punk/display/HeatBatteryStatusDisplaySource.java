package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;

public class HeatBatteryStatusDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof HeatBatteryBlockEntity heatBattery))
            return EMPTY_LINE;

        return switch (heatBattery.getHeatLevel()) {
            case 2 -> Component.literal("Superheated").withStyle(ChatFormatting.RED);
            case 1 -> Component.literal("Heated").withStyle(ChatFormatting.YELLOW);
            default -> Component.literal("None").withStyle(ChatFormatting.DARK_GRAY);
        };
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
