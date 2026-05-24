package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
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

    public ModMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
