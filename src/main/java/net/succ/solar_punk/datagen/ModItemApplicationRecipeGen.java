package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.createItem;

public class ModItemApplicationRecipeGen extends ItemApplicationRecipeGen {

    GeneratedRecipe TURBINE_CASING = create("turbine_casing", b -> b
        .require(createItem("industrial_iron_block"))
        .require(createItem("zinc_ingot"))
        .output(ModBlocks.TURBINE_CASING.get()));

    GeneratedRecipe TURBINE_CASING_GLASS = create("turbine_casing_glass", b -> b
        .require(ModBlocks.TURBINE_CASING.get())
        .require(Items.GLASS)
        .output(ModBlocks.TURBINE_CASING_GLASS.get()));

    // Fusion Reactor Casing: a Netherite Ingot applied to a Copper Block.
    GeneratedRecipe FUSION_REACTOR_CASING = create("fusion_reactor_casing", b -> b
        .require(Items.COPPER_BLOCK)
        .require(Items.NETHERITE_INGOT)
        .output(ModBlocks.FUSION_REACTOR_CASING.get()));

    GeneratedRecipe FUSION_REACTOR_CASING_GLASS = create("fusion_reactor_casing_glass", b -> b
        .require(ModBlocks.FUSION_REACTOR_CASING.get())
        .require(Items.GLASS)
        .output(ModBlocks.FUSION_REACTOR_CASING_GLASS.get()));

    public ModItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}