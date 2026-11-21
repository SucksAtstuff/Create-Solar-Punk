package net.succ.solar_punk.compat.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.succ.solar_punk.SolarPunk;
import net.succ.offbyonelib.compat.jei.AddonRecipeCategory;

// This mod's JEI categories: the library's layout, with recipe types under solarpunk.
public abstract class SolarPunkCategory<T> extends AddonRecipeCategory<T> {

    protected SolarPunkCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int arrowX) {
        super(guiHelper, recipeType, machine, width, arrowX);
    }

    protected SolarPunkCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, ItemLike machine, int width, int height, int arrowX) {
        super(guiHelper, recipeType, machine, width, height, arrowX);
    }

    protected static <T> RecipeType<T> type(String id, Class<T> recipeClass) {
        return type(SolarPunk.MODID, id, recipeClass);
    }
}
