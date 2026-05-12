package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        solarHeating(output, "stone_to_lava", Ingredient.of(Items.STONE), new FluidStack(Fluids.LAVA, 1000));
        solarHeating(output, "cobblestone_to_lava", Ingredient.of(Items.COBBLESTONE), new FluidStack(Fluids.LAVA, 1000));
        // Salt item gives 100 mB; block gives 1000 mB (vs 900 mB for 9 items — ~11% bonus for bulk)
        solarHeating(output, "salt_item_to_molten_salt", Ingredient.of(ModItems.SALT.get()), new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 100));
        solarHeating(output, "salt_to_molten_salt", Ingredient.of(ModBlocks.SALT_BLOCK.get().asItem()), new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 1000));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SALT_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.SALT.get())
                .unlockedBy("has_salt", has(ModItems.SALT.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SALT.get(), 9)
                .requires(ModBlocks.SALT_BLOCK.get())
                .unlockedBy("has_salt_block", has(ModBlocks.SALT_BLOCK.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "salt_from_salt_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SOLAR_HEATER.get())
                .pattern("GGG")
                .pattern("C C")
                .pattern("AAA")
                .define('G', Items.GLASS_PANE)
                .define('C', Items.COPPER_INGOT)
                .define('A', createItem("andesite_alloy"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ANDESITE_SOLAR_PANEL.get())
                .pattern("GGG")
                .pattern("AAA")
                .pattern("ASA")
                .define('G', Items.GLASS_PANE)
                .define('A', createItem("andesite_alloy"))
                .define('S', createItem("shaft"))
                .unlockedBy("has_andesite_alloy", has(createItem("andesite_alloy")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BRASS_SOLAR_PANEL.get())
                .pattern("GGG")
                .pattern("BBB")
                .pattern("EBE")
                .define('G', Items.GLASS_PANE)
                .define('B', createItem("brass_ingot"))
                .define('E', createItem("electron_tube"))
                .unlockedBy("has_brass_ingot", has(createItem("brass_ingot")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.KINETIC_BATTERY.get())
                .pattern("AAA")
                .pattern("AFA")
                .pattern("AAA")
                .define('A', createItem("andesite_alloy"))
                .define('F', createItem("flywheel"))
                .unlockedBy("has_flywheel", has(createItem("flywheel")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HEAT_BATTERY.get())
                .pattern("DDD")
                .pattern("BSB")
                .pattern("DDD")
                .define('D', Items.DEEPSLATE_BRICKS)
                .define('B', createItem("brass_ingot"))
                .define('S', ModBlocks.SALT_BLOCK.get())
                .unlockedBy("has_brass_ingot", has(createItem("brass_ingot")))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GEYSER_CAP.get())
                .pattern("RAR")
                .pattern("SCS")
                .pattern("AHA")
                .define('R', createItem("polished_rose_quartz"))
                .define('A', createItem("andesite_alloy"))
                .define('C', createItem("andesite_casing"))
                .define('S', createItem("shaft"))
                .define('H', createItem("chute"))
                .unlockedBy("has_andesite_casing", has(createItem("andesite_casing")))
                .save(output);
    }

    private static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }

    private static void solarHeating(RecipeOutput output, String name, Ingredient ingredient, FluidStack result) {
        output.accept(
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_heating/" + name),
                new SolarHeaterRecipe(ingredient, result),
                null
        );
    }
}
