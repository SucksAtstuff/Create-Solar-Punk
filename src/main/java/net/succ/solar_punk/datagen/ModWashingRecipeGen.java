package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.WashingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModWashingRecipeGen extends WashingRecipeGen {

    // Like Create's crushed ores: 9 nuggets and a secondary. Lithium is really pulled out of
    // salt brines, so the secondary is Salt.
    GeneratedRecipe WASH_CRUSHED_RAW_LITHIUM = create("crushed_raw_lithium", b -> b
        .require(ModItems.CRUSHED_RAW_LITHIUM.get())
        .output(ModItems.LITHIUM_NUGGET.get(), 9)
        .output(0.5f, ModItems.SALT.get(), 1));

    public ModWashingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
