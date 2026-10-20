package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.CrushingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.itemTag;

public class ModCrushingRecipeGen extends CrushingRecipeGen {

    private static final TagKey<Item> LEAVES   = itemTag("minecraft", "leaves");
    private static final TagKey<Item> SAPLINGS = itemTag("minecraft", "saplings");
    private static final TagKey<Item> CROPS    = itemTag("c", "crops");
    private static final TagKey<Item> SEEDS    = itemTag("c", "seeds");

    GeneratedRecipe CRUSH_LEAVES = create("leaves_to_biomass", b -> b
        .duration(200)
        .require(LEAVES)
        .output(ModItems.BIOMASS.get(), 2));

    GeneratedRecipe CRUSH_SAPLINGS = create("saplings_to_biomass", b -> b
        .duration(150)
        .require(SAPLINGS)
        .output(ModItems.BIOMASS.get(), 1));

    GeneratedRecipe CRUSH_CROPS = create("crops_to_biomass", b -> b
        .duration(200)
        .require(CROPS)
        .output(ModItems.BIOMASS.get(), 1)
        .output(0.5f, ModItems.BIOMASS.get(), 1));

    GeneratedRecipe CRUSH_SEEDS = create("seeds_to_biomass", b -> b
        .duration(150)
        .require(SEEDS)
        .output(0.75f, ModItems.BIOMASS.get(), 1));

    // Beryllium has no ore of its own: it comes from this crushing bonus and the
    // salt brine Mixing recipe (see ModMixingRecipeGen), riding on Lithium production.
    GeneratedRecipe CRUSH_RAW_LITHIUM = create("raw_lithium_to_dust", b -> b
        .duration(200)
        .require(ModItems.RAW_LITHIUM.get())
        .output(ModItems.LITHIUM_DUST.get(), 1)
        .output(0.15f, ModItems.BERYLLIUM_DUST.get(), 1));

    public ModCrushingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
