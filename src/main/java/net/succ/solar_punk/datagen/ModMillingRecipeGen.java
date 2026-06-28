package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MillingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModMillingRecipeGen extends MillingRecipeGen {

    private static final TagKey<Item> LEAVES   = tag("minecraft", "leaves");
    private static final TagKey<Item> SAPLINGS = tag("minecraft", "saplings");
    private static final TagKey<Item> CROPS    = tag("c", "crops");

    GeneratedRecipe MILL_LEAVES = create("leaves_to_biomass", b -> b
        .duration(150)
        .require(LEAVES)
        .output(ModItems.BIOMASS.get(), 1));

    GeneratedRecipe MILL_SAPLINGS = create("saplings_to_biomass", b -> b
        .duration(100)
        .require(SAPLINGS)
        .output(0.5f, ModItems.BIOMASS.get(), 1));

    GeneratedRecipe MILL_CROPS = create("crops_to_biomass", b -> b
        .duration(100)
        .require(CROPS)
        .output(ModItems.BIOMASS.get(), 1));

    public ModMillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
