package net.succ.solar_punk.display;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

public class FusionReactorSteamTankDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        FusionReactorCoreBlockEntity core = FusionReactorDisplayHelper.core(context);
        if (core == null) return ZERO.copy();
        int percent = Math.round(core.steamTank.getFluidAmount() * 100f / core.steamTank.getCapacity());
        return Component.literal(percent + "%").withStyle(ChatFormatting.WHITE);
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
