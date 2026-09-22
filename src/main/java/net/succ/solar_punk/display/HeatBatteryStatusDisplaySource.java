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

        int level = heatBattery.getHeatLevel();
        if (level == 2) return Component.literal("Superheated").withStyle(ChatFormatting.RED);
        if (level == 1) return Component.literal("Heated").withStyle(ChatFormatting.YELLOW);
        return Component.literal("None").withStyle(ChatFormatting.DARK_GRAY);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
