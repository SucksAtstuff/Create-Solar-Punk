package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModMixingRecipeGen extends MixingRecipeGen {

    GeneratedRecipe FERTILIZER = create("biochar_to_fertilizer", b -> b
        .require(ModItems.BIOCHAR.get())
        .require(Fluids.WATER, 250)
        .output(ModFluids.FERTILIZER_SOURCE.get(), 250));

    // Replaces the old passive DeuteriumExtractorBlockEntity (see plan_for_fusion.md's
    // Fuel chain section) - Water electrolyzed into Deuterium in a basin, Heated (not
    // Superheated) since Deuterium is meant to stay the cheap, non-bottleneck half of
    // the reactor's fuel pair. Copper Ingot stands in for the electrolysis electrode -
    // it conducts the current/heat but isn't consumed, so it's listed as both an
    // ingredient and a result and comes back out every cycle, same "reusable catalyst"
    // shape other tech mods use for electrolysis-style recipes.
    GeneratedRecipe WATER_TO_DEUTERIUM = create("water_to_deuterium", b -> b
        .require(Fluids.WATER, 1000)
        .require(Items.COPPER_INGOT)
        .output(ModFluids.DEUTERIUM_SOURCE.get(), 250)
        .output(Items.COPPER_INGOT, 1)
        .requiresHeat(HeatCondition.HEATED));

    // Replaces the old passive LithiumBrineExtractorBlockEntity (kinetic-powered,
    // single-purpose block) with a Basin recipe - same renewable-floor role (Water +
    // Salt -> Lithium Dust, no ore touched at all), just plugged into Create's own
    // Mixing infrastructure instead of a bespoke block, same move WATER_TO_DEUTERIUM
    // made above. Superheated (not Heated) keeps Lithium the harder of the two fuels
    // to produce - Deuterium is deliberately the cheap, non-bottleneck half. duration()
    // mirrors the old block's 600-tick (30s) cycle, since a Basin recipe would
    // otherwise process back-to-back as fast as the Mixer's RPM allows, undercutting
    // the "much slower than mining" framing a fixed cycle timer used to enforce. Chance
    // Beryllium Dust output is the same 5% bonus the block's own RNG used to roll,
    // now expressed as a chance-output the same way the Raw Lithium crushing recipe
    // does (see ModCrushingRecipeGen) - and gets JEI visibility for free via Create's
    // own Mixing category, same win Deuterium got.
    GeneratedRecipe SALT_BRINE_TO_LITHIUM = create("salt_brine_to_lithium", b -> b
        .require(Fluids.WATER, 250)
        .require(ModItems.SALT.get())
        .duration(600)
        .output(ModItems.LITHIUM_DUST.get(), 1)
        .output(0.05f, ModItems.BERYLLIUM_DUST.get(), 1)
        .requiresHeat(HeatCondition.SUPERHEATED));

    public ModMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
