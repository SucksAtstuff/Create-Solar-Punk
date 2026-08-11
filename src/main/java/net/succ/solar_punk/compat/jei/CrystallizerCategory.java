package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.CrystallizerRecipe;

// Unlike the old fixed Salt Crystallizer category, this reads real data-driven
// CrystallizerRecipes (see SolarPunkJeiPlugin.registerRecipes) - every recipe a datapack
// adds shows up here automatically, inputB and byproduct slots included only when the
// recipe actually uses them.
public class CrystallizerCategory implements IRecipeCategory<CrystallizerRecipe> {

    public static final RecipeType<CrystallizerRecipe> RECIPE_TYPE =
            RecipeType.create(SolarPunk.MODID, "crystallizing", CrystallizerRecipe.class);

    private static final int WIDTH  = 130;
    private static final int HEIGHT = 40;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public CrystallizerCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CRYSTALLIZER.get()));
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<CrystallizerRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.solarpunk.category.crystallizing");
    }

    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() {
        return icon;
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

    @Override
    public void draw(CrystallizerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 44, 12);
    }
}
