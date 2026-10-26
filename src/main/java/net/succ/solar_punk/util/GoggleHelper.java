package net.succ.solar_punk.util;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

// Goggle tooltip lines the machines share.
public final class GoggleHelper {

    private GoggleHelper() {}

    /** "Label: 1000 / 8000 mB" under {@code solar_punk.tooltip.<langKey>}. */
    public static void tank(List<Component> tooltip, String langKey, int amount, int capacity, ChatFormatting color) {
        tankLine(langKey, amount, capacity, color).forGoggles(tooltip, 1);
    }

    public static void tank(List<Component> tooltip, String langKey, FluidTank tank, ChatFormatting color) {
        tank(tooltip, langKey, tank.getFluidAmount(), tank.getCapacity(), color);
    }

    /** The same line, unsent, for callers that add to it before showing it. */
    public static LangBuilder tankLine(String langKey, int amount, int capacity, ChatFormatting color) {
        return CreateLang.translate("solar_punk.tooltip." + langKey)
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(amount).text(" / " + capacity + " mB").style(color).component());
    }

    /**
     * Two lines for a tank that can hold different fluids: "Label: Fluid Name", then its
     * amount indented below. Shows nothing while the tank is empty.
     */
    public static void namedTank(List<Component> tooltip, String langKey, FluidTank tank, ChatFormatting nameColor) {
        if (tank.isEmpty()) return;
        CreateLang.translate("solar_punk.tooltip." + langKey)
                .style(ChatFormatting.GRAY)
                .add(tank.getFluid().getHoverName().copy().withStyle(nameColor))
                .forGoggles(tooltip, 1);
        CreateLang.number(tank.getFluidAmount())
                .text(" / " + tank.getCapacity() + " mB")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 2);
    }
}
