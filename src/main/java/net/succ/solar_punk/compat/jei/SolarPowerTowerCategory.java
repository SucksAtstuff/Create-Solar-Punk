package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import net.succ.solar_punk.fluid.ModFluids;

// One display-only entry per tower mode (Molten Salt and Steam), showing the Water a
// full-width (3x3) tower spends per 1000 mB of output. The ratio scales with the footprint.
public class SolarPowerTowerCategory extends SolarPunkCategory<SolarPowerTowerCategory.DisplayRecipe> {

    public record DisplayRecipe(boolean steamMode) {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("solar_power_tower", DisplayRecipe.class);

    public static final DisplayRecipe SALT_INSTANCE  = new DisplayRecipe(false);
    public static final DisplayRecipe STEAM_INSTANCE = new DisplayRecipe(true);

    private static final int SHOWN_WIDTH = 3;
    private static final int OUTPUT = 1000;

    public SolarPowerTowerCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.SOLAR_POWER_TOWER.get(), 100, 28);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        int water = Math.round(SolarPowerTowerBlockEntity.waterPerMb(SHOWN_WIDTH, recipe.steamMode()) * OUTPUT);
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(water, false, 16, 16)
                .addFluidStack(Fluids.WATER, water)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                        "jei.solarpunk.solar_power_tower.water_note",
                        SHOWN_WIDTH, SHOWN_WIDTH, recipe.steamMode() ? "0.5" : "1").withStyle(ChatFormatting.GOLD)));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                .setFluidRenderer(OUTPUT, false, 16, 16)
                .addFluidStack(recipe.steamMode()
                        ? ModFluids.STEAM_SOURCE.get()
                        : ModFluids.MOLTEN_SALT_SOURCE.get(), OUTPUT);
    }
}
