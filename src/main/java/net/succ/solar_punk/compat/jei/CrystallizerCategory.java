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
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 4)
                .setFluidRenderer(recipe.inputA().getAmount(), false, 16, 16)
                .addFluidStack(recipe.inputA().getFluid(), recipe.inputA().getAmount());

        if (!recipe.inputB().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 22)
                    .setFluidRenderer(recipe.inputB().getAmount(), false, 16, 16)
                    .addFluidStack(recipe.inputB().getFluid(), recipe.inputB().getAmount());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 4)
                .addItemStack(recipe.result());

        if (!recipe.byproduct().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 22)
                    .setFluidRenderer(recipe.byproduct().getAmount(), false, 16, 16)
                    .addFluidStack(recipe.byproduct().getFluid(), recipe.byproduct().getAmount());
        }
    }
}
