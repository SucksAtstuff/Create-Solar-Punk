package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.CuttingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModCuttingRecipeGen extends CuttingRecipeGen {

    // Metal sheets sawn into wire. Gold is softer and gives more.
    GeneratedRecipe COPPER_WIRE = create("copper_wire", b -> b
        .require(ModTags.Items.PLATES_COPPER)
        .output(ModItems.COPPER_WIRE.get(), 2));

    GeneratedRecipe IRON_WIRE = create("iron_wire", b -> b
        .require(ModTags.Items.PLATES_IRON)
        .output(ModItems.IRON_WIRE.get(), 2));

    GeneratedRecipe GOLD_WIRE = create("gold_wire", b -> b
        .require(ModTags.Items.PLATES_GOLD)
        .output(ModItems.GOLD_WIRE.get(), 3));

    public ModCuttingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
