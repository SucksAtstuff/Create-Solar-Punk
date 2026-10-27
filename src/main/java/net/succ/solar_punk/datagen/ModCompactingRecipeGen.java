package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.CompactingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModCompactingRecipeGen extends CompactingRecipeGen {

    GeneratedRecipe BIOMASS_TO_PELLET = create("biomass_to_pellet", b -> b
        .require(ModItems.BIOMASS.get())
        .require(ModItems.BIOMASS.get())
        .output(ModItems.BIOMASS_PELLET.get(), 1));

    GeneratedRecipe CARBON_DUST_TO_BIOCHAR = create("carbon_dust_to_biochar", b -> b
        .require(ModItems.CARBON_DUST.get())
        .require(ModItems.CARBON_DUST.get())
        .require(ModItems.CARBON_DUST.get())
        .require(ModItems.CARBON_DUST.get())
        .output(ModItems.BIOCHAR.get(), 1));

    public ModCompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}