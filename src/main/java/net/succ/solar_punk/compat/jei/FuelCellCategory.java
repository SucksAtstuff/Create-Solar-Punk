package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.compat.northstar.NorthstarCompat;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.util.EnergyFormat;

// The Fuel Cell stack is hardcoded in EndPlateBlockEntity, so this shows one display-only
// entry: 1000 mB Hydrogen + 500 mB Oxygen -> 1000 mB Water, and the FE it makes.
public class FuelCellCategory extends SolarPunkCategory<FuelCellCategory.DisplayRecipe> {

    public record DisplayRecipe() {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("fuel_cell", DisplayRecipe.class);
    public static final DisplayRecipe INSTANCE = new DisplayRecipe();

    private static final int HYDROGEN = 1000;

    public FuelCellCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.FUEL_CELL.get(), 110, 47);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        var hydrogen = builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(HYDROGEN, false, 16, 16)
                .addFluidStack(ModFluids.HYDROGEN_SOURCE.get(), HYDROGEN);
        // Northstar's Liquid Hydrogen burns too, when it's installed.
        if (NorthstarCompat.LIQUID_HYDROGEN != Fluids.EMPTY)
            hydrogen.addFluidStack(NorthstarCompat.LIQUID_HYDROGEN, HYDROGEN);

        builder.addSlot(RecipeIngredientRole.INPUT, 24, 12)
                .setFluidRenderer(HYDROGEN, false, 16, 16)
                .addFluidStack(ModFluids.OXYGEN_SOURCE.get(), HYDROGEN / 2);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 12)
                .setFluidRenderer(HYDROGEN, false, 16, 16)
                .addFluidStack(Fluids.WATER, HYDROGEN);
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        long fe = (long) Config.fuelCellFePerTick * HYDROGEN / Math.max(1, Config.fuelCellHydrogenPerTick);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei." + SolarPunk.MODID + ".energy_made", EnergyFormat.fe(fe)),
                5, 31, 0x404040, false);
    }
}
