package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

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

    // Fusion Reactor blanket modules - same L-shape as the turbine blades above, one
    // tier up: Lithium/Beryllium Ingots instead of Create casings, matching the
    // reactor's capstone status. See plan_for_fusion.md's Tunable knob section.
    //   L L L L
    //   L L
    GeneratedRecipe LITHIUM_BREEDER_MODULE = create(() -> ModBlocks.LITHIUM_BREEDER_MODULE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("LLLL")
                    .patternLine("LL  ")
                    .key('L', ModItems.LITHIUM_INGOT.get()));

    //   B B B B
    //   B B
    GeneratedRecipe BERYLLIUM_REFLECTOR_MODULE = create(() -> ModBlocks.BERYLLIUM_REFLECTOR_MODULE.get())
            .returns(2)
            .recipe(b -> b
                    .patternLine("BBBB")
                    .patternLine("BB  ")
                    .key('B', ModItems.BERYLLIUM_INGOT.get()));

    public ModMechanicalCraftingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    private static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }
}