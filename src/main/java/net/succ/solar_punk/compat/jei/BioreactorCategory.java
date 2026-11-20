package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

// The Bioreactor is hardcoded in BioreactorBlockEntity, so this is one display-only entry
// covering all of it: Water, a Kelp or Seagrass to start the culture, and optional Fertilizer
// in; Biomass, Oxygen and the Algae Culture itself (which can be piped out) out. Amounts are
// per Biomass, where the culture used up regrows from the same amount of Water.
public class BioreactorCategory extends SolarPunkCategory<BioreactorCategory.DisplayRecipe> {

    public record DisplayRecipe() {}

    public static final RecipeType<DisplayRecipe> RECIPE_TYPE = type("bioreactor", DisplayRecipe.class);
    public static final DisplayRecipe INSTANCE = new DisplayRecipe();

    private static final String KEY = "jei." + SolarPunk.MODID + ".bioreactor.";

    public BioreactorCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.BIOREACTOR.get(), 150, 62, 64);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        int water = Config.bioreactorCulturePerBiomass;
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                .setFluidRenderer(water, false, 16, 16)
                .addFluidStack(Fluids.WATER, water);
        builder.addSlot(RecipeIngredientRole.INPUT, 24, 12)
                .addIngredients(Ingredient.of(ModTags.Items.ALGAE_SEEDS))
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable(KEY + "seed_note").withStyle(ChatFormatting.GOLD)));
        int nutrient = Config.bioreactorNutrientPerBiomass;
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 12)
                .setFluidRenderer(nutrient, false, 16, 16)
                .addFluidStack(ModFluids.FERTILIZER_SOURCE.get(), nutrient)
                .addFluidStack(ModFluids.RICH_FERTILIZER_SOURCE.get(), nutrient)
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable(KEY + "optional").withStyle(ChatFormatting.GOLD)));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 12)
                .addItemStack(new ItemStack(ModItems.BIOMASS.get()));
        if (Config.bioreactorOxygenPerBiomass > 0)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 12)
                    .setFluidRenderer(Config.bioreactorOxygenPerBiomass, false, 16, 16)
                    .addFluidStack(ModFluids.OXYGEN_SOURCE.get(), Config.bioreactorOxygenPerBiomass);
        int seed = Config.bioreactorSeedMb;
        builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 12)
                .setFluidRenderer(seed, false, 16, 16)
                .addFluidStack(ModFluids.ALGAE_CULTURE_SOURCE.get(), seed)
                .addRichTooltipCallback((view, tooltip) ->
                        tooltip.add(Component.translatable(KEY + "culture_note").withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        float perMinute = 1200f / Config.bioreactorTicksPerBiomass;
        int y = drawText(guiGraphics, Component.translatable(KEY + "rate", String.format("%.1f", perMinute)), 5, 32);
        drawText(guiGraphics, Component.translatable(KEY + "boost",
                format(Config.bioreactorFertilizerBoost), format(Config.bioreactorRichFertilizerBoost)), 5, y);
    }

    private static String format(double boost) {
        return boost == Math.rint(boost) ? String.valueOf((int) boost) : String.valueOf(boost);
    }
}
