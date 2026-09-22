package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;

import java.util.Locale;

public class SolarPowerTowerGenerationRateDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SolarPowerTowerBlockEntity tower))
            return ZERO.copy();
        SolarPowerTowerBlockEntity ctrl = tower.getControllerBE();
        if (ctrl == null) return ZERO.copy();
        String text = String.format(Locale.ROOT, "%.1f mB/s", ctrl.getGenerationRate() * 20f);
        return Component.literal(text).withStyle(ctrl.steamMode ? ChatFormatting.WHITE : ChatFormatting.GOLD);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
