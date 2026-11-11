package net.succ.solar_punk.compat.crowns;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Soft compat for Create: CROWNS (github.com/RealAntEngineer/CROWNS), with no Gradle
 * dependency. CROWNS has no steam fluid: its steam is plain {@code minecraft:water}
 * carrying a {@code crowns:real_gas_state} data component (FormicAPI's
 * {@code SpecificRealGasState}: pressure, enthalpy, and a vapor quality from 0 = liquid
 * to 1 = dry or superheated steam). The Steam Turbine accepts such water once its vapor
 * quality reaches {@code generators.steam_turbine.crowns_min_vapor_quality} and stores
 * it as Solarpunk Steam, 1:1 by mB like the Firebox Boiler.
 *
 * <p>Only this direction works: CROWNS machines only take fluids in the water tag, so
 * Solarpunk Steam can't go into them. Any reflection failure disables the compat and
 * logs once. Must only be used after registries are frozen (in-game block entity code).
 */
public final class CrownsCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/crowns");

    private static final boolean LOADED = ModList.get().isLoaded("crowns");

    private static boolean triedInit = false;
    private static boolean failed = false;
    private static DataComponentType<?> gasState;
    private static Method mVaporQuality; // SpecificRealGasState#vaporQuality(), returns Float

    public static boolean isTurbineSteam(FluidStack stack) {
        if (!LOADED || failed || !Config.turbineCrownsSteam) return false;
        if (stack.isEmpty() || !stack.getFluid().isSame(Fluids.WATER)) return false;
        if (!init()) return false;

        // Water without the component is CROWNS' default state: liquid at room temperature
        Object state = stack.get(gasState);
        if (state == null) return false;
        try {
            if (mVaporQuality == null)
                mVaporQuality = state.getClass().getMethod("vaporQuality");
            Object quality = mVaporQuality.invoke(state);
            return quality instanceof Float q && q >= Config.turbineCrownsMinVaporQuality;
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    private static boolean init() {
        if (triedInit) return gasState != null;
        triedInit = true;
        gasState = BuiltInRegistries.DATA_COMPONENT_TYPE.get(
                ResourceLocation.fromNamespaceAndPath("crowns", "real_gas_state"));
        if (gasState == null) disable(null);
        return gasState != null;
    }

    private static void disable(Throwable t) {
        failed = true;
        LOGGER.warn("Create: CROWNS steam compat disabled, its steam format has changed", t);
    }

    private CrownsCompat() {}
}
