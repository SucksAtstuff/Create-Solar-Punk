package net.succ.solar_punk.util;

import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.offbyonelib.util.FluidTankNBTHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Oxy-fuel burning for the Biomass Gasifier and Biofuel Engine: Oxygen piped in raises the
 * generator's stress capacity while it runs. Oxygen boosts a fuel, it isn't one, so nothing
 * happens without fuel. Checked once a second, so the network is only re-solved when the
 * boost turns on or off.
 */
public final class OxyFuel {

    public final FluidTank tank;
    private final int perSecond;
    private final double boost;
    private boolean boosted;

    public OxyFuel(int capacity, int perSecond, double boost, Runnable onChanged) {
        this.perSecond = perSecond;
        this.boost = boost;
        this.tank = new FluidTank(capacity) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().isSame(ModFluids.OXYGEN_SOURCE.get());
            }
            @Override
            protected void onContentsChanged() { onChanged.run(); }
        };
    }

    /**
     * Call once a second. Uses a second's Oxygen while the generator runs and there's
     * enough of it. Returns true when the boost turned on or off, so the caller re-solves
     * the network for the new capacity.
     */
    public boolean tickSecond(boolean running) {
        boolean now = running && tank.getFluidAmount() >= perSecond;
        if (now) tank.drain(perSecond, IFluidHandler.FluidAction.EXECUTE);
        boolean changed = now != boosted;
        boosted = now;
        return changed;
    }

    public boolean boosted() {
        return boosted;
    }

    public float capacity(float base) {
        return boosted ? (float) (base * (1 + boost)) : base;
    }

    /**
     * The generator's fluid capability: Oxygen into {@link #tank}, and when {@code fuel} is
     * given, fuel into it too. Only fuel can be drained, so pumps can't pull Oxygen back out.
     */
    public IFluidHandler handler(@Nullable FluidTank fuel) {
        return new IFluidHandler() {
            private FluidTank at(int index) {
                return fuel != null && index == 0 ? fuel : tank;
            }

            @Override public int getTanks() { return fuel != null ? 2 : 1; }
            @Override public FluidStack getFluidInTank(int index) { return at(index).getFluid(); }
            @Override public int getTankCapacity(int index) { return at(index).getCapacity(); }
            @Override public boolean isFluidValid(int index, FluidStack stack) { return at(index).isFluidValid(stack); }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                if (tank.isFluidValid(resource)) return tank.fill(resource, action);
                return fuel != null ? fuel.fill(resource, action) : 0;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return fuel != null ? fuel.drain(resource, action) : FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return fuel != null ? fuel.drain(maxDrain, action) : FluidStack.EMPTY;
            }
        };
    }

    // Saved with the boost, so a reload keeps the same capacity (no phantom SU)
    public void write(CompoundTag tag) {
        FluidTankNBTHelper.save(tag, "OxygenTank", tank);
        tag.putBoolean("OxygenBoost", boosted);
    }

    public void read(CompoundTag tag) {
        FluidTankNBTHelper.load(tag, "OxygenTank", tank);
        boosted = tag.getBoolean("OxygenBoost");
    }

    public void addToGoggleTooltip(List<Component> tooltip) {
        SolarPunk.GOGGLES.tank(tooltip, "oxygen", tank, ChatFormatting.AQUA);
        if (boosted) {
            CreateLang.translate("solar_punk.tooltip.oxygen_boost")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.text("+" + Math.round(boost * 100) + "% SU").style(ChatFormatting.AQUA).component())
                    .forGoggles(tooltip, 1);
        }
    }
}
