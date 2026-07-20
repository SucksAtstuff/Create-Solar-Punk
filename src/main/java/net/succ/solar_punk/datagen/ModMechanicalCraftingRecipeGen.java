package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public class ModMechanicalCraftingRecipeGen extends MechanicalCraftingRecipeGen {

    // Andesite blade: 4x2 andesite casing in an L-shape, outputs 2
    //   A A A A
    //   A A
    GeneratedRecipe ANDESITE_TURBINE_BLADE = create(() -> ModBlocks.ANDESITE_TURBINE_BLADE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("AAAA")
                    .patternLine("AA  ")
                    .key('A', createItem("andesite_casing")));

    // Brass blade: same shape with brass casing, outputs 2
    //   B B B B
    //   B B
    GeneratedRecipe BRASS_TURBINE_BLADE = create(() -> ModBlocks.BRASS_TURBINE_BLADE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("BBBB")
                    .patternLine("BB  ")
                    .key('B', createItem("brass_casing")));

    public ModMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    private static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }
}