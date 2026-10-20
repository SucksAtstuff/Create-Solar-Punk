package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;

// One display-only entry per tower mode (Molten Salt and Steam).
public class SolarPowerTowerCategory extends SolarPunkCategory<SolarPowerTowerCategory.DisplayRecipe> {

    public record DisplayRecipe(boolean steamMode) {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("solar_power_tower", DisplayRecipe.class);

    public static final DisplayRecipe SALT_INSTANCE  = new DisplayRecipe(false);
    public static final DisplayRecipe STEAM_INSTANCE = new DisplayRecipe(true);

    public SolarPowerTowerCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.SOLAR_POWER_TOWER.get(), 100, 28);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(Fluids.WATER, 1000);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                .setFluidRenderer(1000, false, 16, 16)
                .addFluidStack(recipe.steamMode()
                        ? ModFluids.STEAM_SOURCE.get()
                        : ModFluids.MOLTEN_SALT_SOURCE.get(), 1000);
    }
}
