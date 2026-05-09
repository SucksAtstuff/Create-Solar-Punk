package net.succ.create_solar_powered.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.fluid.ModFluids;
import net.succ.create_solar_powered.recipe.SolarHeaterRecipe;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        solarHeating(output, "stone_to_lava", Ingredient.of(Items.STONE), new FluidStack(Fluids.LAVA, 1000));
        solarHeating(output, "cobblestone_to_lava", Ingredient.of(Items.COBBLESTONE), new FluidStack(Fluids.LAVA, 1000));
        solarHeating(output, "salt_to_molten_salt", Ingredient.of(ModBlocks.SALT_BLOCK.get().asItem()), new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 1000));
    }

    private static void solarHeating(RecipeOutput output, String name, Ingredient ingredient, FluidStack result) {
        output.accept(
                ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "solar_heating/" + name),
                new SolarHeaterRecipe(ingredient, result),
                null
        );
    }
}
