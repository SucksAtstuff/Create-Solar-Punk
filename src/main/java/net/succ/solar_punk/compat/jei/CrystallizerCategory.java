package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.CrystallizerRecipe;

// Shows every data-driven CrystallizerRecipe. The second input and the byproduct slots
// only appear when the recipe uses them.
public class CrystallizerCategory extends SolarPunkCategory<CrystallizerRecipe> {

    public static final RecipeType<CrystallizerRecipe> RECIPE_TYPE = type("crystallizing", CrystallizerRecipe.class);

    public CrystallizerCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.CRYSTALLIZER.get(), 130, 44);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrystallizerRecipe recipe, IFocusGroup focuses) {
        // A column with one slot sits level with the arrow; with two, they straddle it.
        boolean twoInputs = !recipe.inputB().isEmpty();
        boolean twoOutputs = !recipe.byproduct().isEmpty();
        builder.addSlot(RecipeIngredientRole.INPUT, 5, twoInputs ? 4 : 13)
                .setFluidRenderer(recipe.inputA().getAmount(), false, 16, 16)
                .addFluidStack(recipe.inputA().getFluid(), recipe.inputA().getAmount());

        if (twoInputs) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 22)
                    .setFluidRenderer(recipe.inputB().getAmount(), false, 16, 16)
                    .addFluidStack(recipe.inputB().getFluid(), recipe.inputB().getAmount());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, twoOutputs ? 4 : 13)
                .addItemStack(recipe.result());

        if (twoOutputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 22)
                    .setFluidRenderer(recipe.byproduct().getAmount(), false, 16, 16)
                    .addFluidStack(recipe.byproduct().getFluid(), recipe.byproduct().getAmount());
        }
    }
}
