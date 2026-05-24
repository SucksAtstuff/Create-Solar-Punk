package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class FluidTankNBTHelper {
    private FluidTankNBTHelper() {}

    public static void save(CompoundTag tag, String key, FluidTank tank) {
        if (tank.isEmpty()) return;
        CompoundTag t = new CompoundTag();
        t.putString("Fluid", BuiltInRegistries.FLUID.getKey(tank.getFluid().getFluid()).toString());
        t.putInt("Amount", tank.getFluidAmount());
        tag.put(key, t);
    }

    public static void load(CompoundTag tag, String key, FluidTank tank) {
        if (!tag.contains(key)) return;
        CompoundTag t = tag.getCompound(key);
        BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(t.getString("Fluid")))
                .filter(fluid -> fluid != Fluids.EMPTY)
                .ifPresent(fluid -> tank.setFluid(new FluidStack(fluid, t.getInt("Amount"))));
    }
}
