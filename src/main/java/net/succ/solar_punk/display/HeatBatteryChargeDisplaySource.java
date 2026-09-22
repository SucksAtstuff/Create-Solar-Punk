package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import org.jetbrains.annotations.Nullable;

public class HeatBatteryChargeDisplaySource extends PercentOrProgressBarDisplaySource {

    @Override
    @Nullable
    protected Float getProgress(DisplayLinkContext context) {
        if (!(context.getSourceBlockEntity() instanceof HeatBatteryBlockEntity heatBattery))
            return null;
        return heatBattery.getHeatStored() / (float) Config.heatBatteryMaxHeat;
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
