package net.succ.solar_punk.compat.jei;

import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.recipe.ParticleAcceleratorRecipe;

import java.util.Arrays;

// Shows every data-driven ParticleAcceleratorRecipe: up to 9 ingredients (3 per row), the
// result, and underneath the ring diameter, stress and speed it needs.
public class ParticleAcceleratorCategory extends SolarPunkCategory<ParticleAcceleratorRecipe> {

    public static final RecipeType<ParticleAcceleratorRecipe> RECIPE_TYPE =
            type("particle_accelerator", ParticleAcceleratorRecipe.class);

    private static final int TEXT_Y = 62;

    public ParticleAcceleratorCategory(IGuiHelper guiHelper) {
        super(guiHelper, RECIPE_TYPE, ModBlocks.PARTICLE_ACCELERATOR_INPUT.get(), 160, 94, 66);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ParticleAcceleratorRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            SizedIngredient ingredient = recipe.ingredients().get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, 5 + (i % 3) * 18, 4 + (i / 3) * 18)
                    .addItemStacks(Arrays.asList(ingredient.getItems()));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 13)
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(ParticleAcceleratorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        Font font = Minecraft.getInstance().font;
        String key = "jei." + SolarPunk.MODID + ".particle_accelerator.";
        guiGraphics.drawString(font, Component.translatable(key + "diameter", recipe.minDiameter()), 5, TEXT_Y, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable(key + "stress",
                CreateLang.number(recipe.stress()).component()), 5, TEXT_Y + 10, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable(key + "speed", recipe.minSpeed()), 5, TEXT_Y + 20, 0x404040, false);
    }
}
