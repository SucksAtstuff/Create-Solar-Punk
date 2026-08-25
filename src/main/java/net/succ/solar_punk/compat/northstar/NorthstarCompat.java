package net.succ.solar_punk.compat.northstar;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// Soft compat for Northstar Redux (github.com/Astronauts-of-Create/Northstar-Redux) -
// no Gradle dependency needed, unlike a typed API compat. Fluids don't need the
// reflection SableCompat uses for Sable's own classes: BuiltInRegistries.FLUID is a
// DefaultedRegistry, so looking up an id that isn't registered (Northstar not
// installed) just resolves to Fluids.EMPTY - never null, never throws - and every
// check below then simply never matches a real FluidStack, a safe no-op. Only needs
// to run after all mods' registries are frozen, which every call site here already
// guarantees (block entity tick/capability code only ever runs in-game).
public final class NorthstarCompat {

    // Northstar's premium liquid fuel - already a Superheated Blaze Burner fuel over
    // there (burn value 3200, double Methane's 1600), signalling it's meant as a
    // late-game/high-tier fuel in Northstar's own economy. The natural fit for a
    // second pipeable Turbine fuel alongside Steam. Deliberately not northstar:hydrogen
    // - that one's a weightless GasFluid meant for gas tanks/life support, and isn't
    // registered as a fuel anywhere in Northstar itself.
    public static final Fluid LIQUID_HYDROGEN =
            BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath("northstar", "liquid_hydrogen"));

    public static boolean isTurbineFuel(Fluid fluid) {
        return LIQUID_HYDROGEN != Fluids.EMPTY && fluid.isSame(LIQUID_HYDROGEN);
    }

    private NorthstarCompat() {}
}
