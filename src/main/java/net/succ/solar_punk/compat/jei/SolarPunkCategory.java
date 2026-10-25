package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.succ.solar_punk.SolarPunk;

// Shared layout for this mod's JEI categories: the machine as the icon, a title from
// "jei.solarpunk.category.<id>", and an animated arrow between inputs and outputs.
public abstract class SolarPunkCategory<T> implements IRecipeCategory<T> {

    private static final int DEFAULT_HEIGHT = 40;
    private static final int ARROW_Y = 12;

    private final RecipeType<T> recipeType;
    private final int width;
    private final int height;
    private final int arrowX;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    protected SolarPunkCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int arrowX) {
        this(guiHelper, recipeType, machine, width, DEFAULT_HEIGHT, arrowX);
    }

    protected SolarPunkCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int height, int arrowX) {
        this.recipeType = recipeType;
        this.width = width;
        this.height = height;
        this.arrowX = arrowX;
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(machine));
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    protected static <T> RecipeType<T> type(String id, Class<T> recipeClass) {
        return RecipeType.create(SolarPunk.MODID, id, recipeClass);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + SolarPunk.MODID + ".category." + recipeType.getUid().getPath());
    }

    @Override public int getWidth()  { return width; }
    @Override public int getHeight() { return height; }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, arrowX, ARROW_Y);
    }
}
