package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.EvaporatingRecipe;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

public class SolarHeaterCategory extends SolarPunkCategory<SolarHeaterCategory.Entry> {

    // The Solar Heater melts items into fluids (SolarHeaterRecipe) and evaporates fluids into
    // items (EvaporatingRecipe, Water -> Salt by default). Both show up on this one page.
    public sealed interface Entry {
        record ItemMelt(SolarHeaterRecipe recipe) implements Entry {}
        record Evaporation(EvaporatingRecipe recipe) implements Entry {}
    }

    public static final RecipeType<Entry> RECIPE_TYPE = type("solar_heating", Entry.class);

    public SolarHeaterCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.SOLAR_HEATER.get(), 100, 28);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        switch (entry) {
            case Entry.ItemMelt(SolarHeaterRecipe recipe) -> {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                        .addIngredients(recipe.ingredient());

                builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                        .setFluidRenderer(recipe.result().getAmount(), false, 16, 16)
                        .addFluidStack(recipe.result().getFluid(), recipe.result().getAmount());
            }
            case Entry.Evaporation(EvaporatingRecipe recipe) -> {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                        .setFluidRenderer(recipe.ingredient().getAmount(), false, 16, 16)
                        .addFluidStack(recipe.ingredient().getFluid(), recipe.ingredient().getAmount());

                builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                        .addItemStack(recipe.result());
            }
        }
    }

    @Override
    protected Recipe<?> recipeOf(Entry entry) {
        return switch (entry) {
            case Entry.ItemMelt melt -> melt.recipe();
            case Entry.Evaporation evaporation -> evaporation.recipe();
        };
    }
}
