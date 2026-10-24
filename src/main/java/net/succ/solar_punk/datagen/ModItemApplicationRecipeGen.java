package net.succ.solar_punk.datagen;

import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.createItem;

public class ModItemApplicationRecipeGen extends ItemApplicationRecipeGen {

    GeneratedRecipe TURBINE_CASING = create("turbine_casing", b -> b
        .require(createItem("industrial_iron_block"))
        .require(ModTags.Items.INGOTS_ZINC)
        .output(ModBlocks.TURBINE_CASING.get()));

    GeneratedRecipe TURBINE_CASING_GLASS = create("turbine_casing_glass", b -> b
        .require(ModBlocks.TURBINE_CASING.get())
        .require(Tags.Items.GLASS_BLOCKS_COLORLESS)
        .output(ModBlocks.TURBINE_CASING_GLASS.get()));

    // Fusion Reactor Casing: a Netherite Ingot applied to a Copper Block.
    GeneratedRecipe FUSION_REACTOR_CASING = create("fusion_reactor_casing", b -> b
        .require(Tags.Items.STORAGE_BLOCKS_COPPER)
        .require(Tags.Items.INGOTS_NETHERITE)
        .output(ModBlocks.FUSION_REACTOR_CASING.get()));

    GeneratedRecipe FUSION_REACTOR_CASING_GLASS = create("fusion_reactor_casing_glass", b -> b
        .require(ModBlocks.FUSION_REACTOR_CASING.get())
        .require(Tags.Items.GLASS_BLOCKS_COLORLESS)
        .output(ModBlocks.FUSION_REACTOR_CASING_GLASS.get()));

    public ModItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}