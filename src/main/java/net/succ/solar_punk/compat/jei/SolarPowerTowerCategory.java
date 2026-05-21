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
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;

public class SolarPowerTowerCategory implements IRecipeCategory<SolarPowerTowerCategory.DisplayRecipe> {

    public record DisplayRecipe() {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE =
            RecipeType.create(SolarPunk.MODID, "solar_power_tower", DisplayRecipe.class);

    public static final DisplayRecipe INSTANCE = new DisplayRecipe();

    private static final int WIDTH  = 100;
    private static final int HEIGHT = 40;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public SolarPowerTowerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SOLAR_POWER_TOWER.get()));
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.solarpunk.category.solar_power_tower");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(Fluids.WATER, 1000);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), 1000);
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 28, 12);
    }
}