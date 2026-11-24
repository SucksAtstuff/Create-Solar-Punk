package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.FermentingRecipe;

// The fermenting recipes, with their per-footprint-block amounts (a bigger Vat scales them up).
public class FermentationVatCategory extends SolarPunkCategory<FermentingRecipe> {

    public static final RecipeType<FermentingRecipe> RECIPE_TYPE = type("fermentation_vat", FermentingRecipe.class);

    public FermentationVatCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.FERMENTATION_VAT.get(), 110, 50);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FermentingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .addIngredients(recipe.ingredient());

        if (recipe.water() > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 26, 12)
                    .setFluidRenderer(Math.max(1000, recipe.water()), false, 16, 16)
                    .addFluidStack(Fluids.WATER, recipe.water());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 12)
                .setFluidRenderer(Math.max(1000, recipe.result().getAmount()), false, 16, 16)
                .addFluidStack(recipe.result().getFluid(), recipe.result().getAmount());
    }
}
