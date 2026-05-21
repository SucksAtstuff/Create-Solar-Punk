package net.succ.solar_punk.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.ModRecipeTypes;
import net.succ.solar_punk.recipe.SolarHeaterRecipe;

import java.util.List;

@JeiPlugin
public class SolarPunkJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new SolarHeaterCategory(guiHelper),
                new SolarPowerTowerCategory(guiHelper),
                new FermentationVatCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        List<SolarHeaterRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.SOLAR_HEATING.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(SolarHeaterCategory.RECIPE_TYPE, recipes);
        registration.addRecipes(SolarPowerTowerCategory.RECIPE_TYPE, List.of(SolarPowerTowerCategory.INSTANCE));
        registration.addRecipes(FermentationVatCategory.RECIPE_TYPE, List.of(FermentationVatCategory.INSTANCE));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SOLAR_HEATER.get()),
                SolarHeaterCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SOLAR_POWER_TOWER.get()),
                SolarPowerTowerCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.FERMENTATION_VAT.get()),
                FermentationVatCategory.RECIPE_TYPE
        );
    }
}