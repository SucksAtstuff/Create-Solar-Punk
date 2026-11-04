package net.succ.solar_punk.compat.jei;

import net.succ.solar_punk.ModTags;
import net.minecraft.world.item.crafting.Ingredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

// Fermentation is hardcoded in FermentationVatBlockEntity rather than recipe-driven, so
// this shows a single display-only entry.
public class FermentationVatCategory extends SolarPunkCategory<FermentationVatCategory.DisplayRecipe> {

    // Plant matter makes Biofuel; waste makes Rich Fertilizer.
    public record DisplayRecipe(boolean waste) {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("fermentation_vat", DisplayRecipe.class);

    public static final DisplayRecipe BIOFUEL = new DisplayRecipe(false);
    public static final DisplayRecipe RICH_FERTILIZER = new DisplayRecipe(true);

    public FermentationVatCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.FERMENTATION_VAT.get(), 110, 50);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        if (recipe.waste()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                    .addIngredients(Ingredient.of(ModTags.Items.FERMENTATION_WASTE));
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                    .addItemStack(new ItemStack(ModItems.BIOMASS.get()));
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 26, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(Fluids.WATER, 1000);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(recipe.waste() ? ModFluids.RICH_FERTILIZER_SOURCE.get() : ModFluids.BIOFUEL_SOURCE.get(), 1000);
    }
}
