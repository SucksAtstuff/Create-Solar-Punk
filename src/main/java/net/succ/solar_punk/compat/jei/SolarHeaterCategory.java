package net.succ.solar_punk.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

public class SolarHeaterCategory extends SolarPunkCategory<SolarHeaterCategory.Entry> {

    // The Solar Heater melts items into fluids (data-driven SolarHeaterRecipes) and also
    // evaporates water into Salt (hardcoded in SolarHeaterBlockEntity). ItemMelt wraps the
    // real recipes and WaterEvaporation is a single display-only entry, so both show up
    // on this one page.
    public sealed interface Entry {
        record ItemMelt(SolarHeaterRecipe recipe) implements Entry {}
        record WaterEvaporation() implements Entry {}
    }

    public static final Entry.WaterEvaporation WATER_EVAPORATION_INSTANCE = new Entry.WaterEvaporation();

    public static final RecipeType<Entry> RECIPE_TYPE = type("solar_heating", Entry.class);

    public SolarHeaterCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.SOLAR_HEATER.get(), 100, 28);
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
}
