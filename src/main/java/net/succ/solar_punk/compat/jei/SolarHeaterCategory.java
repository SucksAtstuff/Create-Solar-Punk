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
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

public class SolarHeaterCategory implements IRecipeCategory<SolarHeaterCategory.Entry> {

    // The Solar Heater does two unrelated things: melt an item into a fluid (a real,
    // data-driven SolarHeaterRecipe) and passively evaporate water into Salt (hardcoded
    // in SolarHeaterBlockEntity - it isn't backed by any recipe at all). Entry lets both
    // show up on this one JEI page: ItemMelt wraps the real recipes, WaterEvaporation is
    // a single fake entry standing in for the hardcoded behaviour, the same trick
    // SolarPowerTowerCategory's DisplayRecipe uses for its two hardcoded modes.
    public sealed interface Entry {
        record ItemMelt(SolarHeaterRecipe recipe) implements Entry {}
        record WaterEvaporation() implements Entry {}
    }

    public static final Entry.WaterEvaporation WATER_EVAPORATION_INSTANCE = new Entry.WaterEvaporation();

    public static final RecipeType<Entry> RECIPE_TYPE =
            RecipeType.create(SolarPunk.MODID, "solar_heating", Entry.class);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;

    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public SolarHeaterCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SOLAR_HEATER.get()));
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<Entry> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.solarpunk.category.solar_heating");
    }

    @Override public int getWidth()  { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        switch (entry) {
            case Entry.ItemMelt(SolarHeaterRecipe recipe) -> {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                        .addIngredients(recipe.ingredient());

                builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                        .setFluidRenderer(recipe.result().getAmount(), false, 16, 16)
                        .addFluidStack(recipe.result().getFluid(), recipe.result().getAmount());
            }
            case Entry.WaterEvaporation ignored -> {
                builder.addSlot(RecipeIngredientRole.INPUT, 5, 12)
                        .setFluidRenderer(Config.solarHeaterWaterPerSalt, false, 16, 16)
                        .addFluidStack(Fluids.WATER, Config.solarHeaterWaterPerSalt);

                builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 12)
                        .addItemStack(new ItemStack(ModItems.SALT.get()));
            }
        }
    }

    @Override
    public void draw(Entry entry, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 28, 12);
    }
}
