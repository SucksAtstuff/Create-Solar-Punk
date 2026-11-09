package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.CompactingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
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
        // Rammed Earth: a soil, Gravel and Clay packed together with Ash or Biochar as the
        // binder. The soil decides the colour.
        ModBlocks.RAMMED_EARTH.forEach((color, set) -> create(color.id, b -> b
                .require(color.soil.get())
                .require(Items.GRAVEL)
                .require(Items.CLAY_BALL)
                .require(Ingredient.of(ModItems.ASH.get(), ModItems.BIOCHAR.get()))
                .output(set.block().get(), 2)));
    }
}