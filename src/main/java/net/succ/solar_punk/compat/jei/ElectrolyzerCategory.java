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

// Electrolysis is hardcoded in ElectrolyzerBlockEntity, so this shows one display-only
// entry: 1000 mB Water -> 1000 mB Hydrogen + 500 mB Oxygen, and the FE that takes.
public class ElectrolyzerCategory extends SolarPunkCategory<ElectrolyzerCategory.DisplayRecipe> {

    public record DisplayRecipe() {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("electrolysis", DisplayRecipe.class);
    public static final DisplayRecipe INSTANCE = new DisplayRecipe();

    private static final int WATER = 1000;

    public ElectrolyzerCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.ELECTROLYZER.get(), 110, 28);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(WATER, false, 16, 16)
                .addFluidStack(Fluids.WATER, WATER);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                .setFluidRenderer(WATER, false, 16, 16)
                .addFluidStack(NorthstarCompat.electrolyzerHydrogen(), WATER);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 12)
                .setFluidRenderer(WATER, false, 16, 16)
                .addFluidStack(ModFluids.OXYGEN_SOURCE.get(), WATER / 2);
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        long fe = (long) Config.electrolyzerFePerTick * WATER / Math.max(1, Config.electrolyzerWaterPerTick);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei." + SolarPunk.MODID + ".energy_used", EnergyFormat.fe(fe)),
                5, 31, 0x404040, false);
    }
}
