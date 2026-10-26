package net.succ.solar_punk.compat.northstar;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// Soft compat for Northstar Redux (github.com/Astronauts-of-Create/Northstar-Redux),
// with no Gradle dependency. The fluid registry is a DefaultedRegistry, so if Northstar
// isn't installed the lookup below just gives Fluids.EMPTY and nothing ever matches.
// This class must only be loaded after registries are frozen, which holds since it's
// only used from in-game block entity code.
public final class NorthstarCompat {

    // Northstar's high-tier liquid fuel, accepted by the Steam Turbine. Not
    // northstar:hydrogen, which is a gas for tanks and life support rather than a fuel.
    public static final Fluid LIQUID_HYDROGEN =
            BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("northstar", "liquid_hydrogen"));

    public static boolean isTurbineFuel(Fluid fluid) {
        return LIQUID_HYDROGEN != Fluids.EMPTY && fluid.isSame(LIQUID_HYDROGEN);
    }

    private NorthstarCompat() {}
}
