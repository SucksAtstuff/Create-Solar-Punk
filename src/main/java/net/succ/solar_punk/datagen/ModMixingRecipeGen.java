package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.anySalt;

public class ModMixingRecipeGen extends MixingRecipeGen {

    GeneratedRecipe FERTILIZER = create("biochar_to_fertilizer", b -> b
        .require(ModItems.BIOCHAR.get())
        .require(Fluids.WATER, 250)
        .output(ModFluids.FERTILIZER_SOURCE.get(), 250));

    // Water electrolyzed into Deuterium. Only Heated (not Superheated) since Deuterium
    // is meant to be the cheap half of the Fusion Reactor's fuel. The Copper Ingot is the
    // electrode: it's both an ingredient and a result, so it comes back every cycle.
    GeneratedRecipe WATER_TO_DEUTERIUM = create("water_to_deuterium", b -> b
        .require(Fluids.WATER, 1000)
        .require(Tags.Items.INGOTS_COPPER)
        .output(ModFluids.DEUTERIUM_SOURCE.get(), 250)
        .output(Items.COPPER_INGOT, 1)
        .requiresHeat(HeatCondition.HEATED));

    // Renewable Lithium from salt brine, with a small Beryllium Dust bonus. Superheated
    // keeps Lithium the harder fuel to make, and the long 600-tick (30s) duration keeps
    // it much slower than mining Lithium Ore.
    GeneratedRecipe SALT_BRINE_TO_LITHIUM = create("salt_brine_to_lithium", b -> b
        .require(Fluids.WATER, 250)
        .require(anySalt())
        .duration(600)
        .output(ModItems.LITHIUM_DUST.get(), 1)
        .output(0.05f, ModItems.BERYLLIUM_DUST.get(), 1)
        .requiresHeat(HeatCondition.SUPERHEATED));

    public ModMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
