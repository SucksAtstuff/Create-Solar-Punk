package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.PercentOrProgressBarDisplaySource;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class SolarPowerTowerEfficiencyDisplaySource extends PercentOrProgressBarDisplaySource {

    @Override
    @Nullable
    protected Float getProgress(DisplayLinkContext context) {
        if (!(context.getSourceBlockEntity() instanceof SolarPowerTowerBlockEntity tower))
            return null;
        SolarPowerTowerBlockEntity ctrl = tower.getControllerBE();
        if (ctrl == null) return null;
        return ctrl.getEfficiency();
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
