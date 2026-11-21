package net.succ.solar_punk.datagen;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.CrushingRecipeGen;
import net.minecraft.world.level.block.Blocks;
import net.succ.solar_punk.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static studio.offbyone.lib.datagen.DatagenUtil.itemTag;

public class ModCrushingRecipeGen extends CrushingRecipeGen {

    private static final TagKey<Item> LEAVES   = itemTag("minecraft", "leaves");
    private static final TagKey<Item> SAPLINGS = itemTag("minecraft", "saplings");
    private static final TagKey<Item> CROPS    = itemTag("c", "crops");
    private static final TagKey<Item> SEEDS    = itemTag("c", "seeds");

    GeneratedRecipe CRUSH_LEAVES = create("leaves_to_biomass", b -> b
        .duration(200)
        .require(LEAVES)
        .output(ModItems.BIOMASS.get(), 2));

    GeneratedRecipe CRUSH_SAPLINGS = create("saplings_to_biomass", b -> b
        .duration(150)
        .require(SAPLINGS)
        .output(ModItems.BIOMASS.get(), 1));

    GeneratedRecipe CRUSH_CROPS = create("crops_to_biomass", b -> b
        .duration(200)
        .require(CROPS)
        .output(ModItems.BIOMASS.get(), 1)
        .output(0.5f, ModItems.BIOMASS.get(), 1));

    GeneratedRecipe CRUSH_SEEDS = create("seeds_to_biomass", b -> b
        .duration(150)
        .require(SEEDS)
        .output(0.75f, ModItems.BIOMASS.get(), 1));

    // Lithium follows Create's ore chain: Raw Lithium and the ores crush into Crushed Raw
    // Lithium with Experience Nuggets, like Create's raw_iron and iron ore recipes (the ores
    // with the same expected yields as iron). Beryllium has no ore of its own: it comes from
    // the 15% bonus here and the salt brine Mixing recipe (see ModMixingRecipeGen).
    GeneratedRecipe CRUSH_RAW_LITHIUM = create("raw_lithium", b -> b
        .duration(400)
        .require(ModTags.Items.RAW_MATERIALS_LITHIUM)
        .output(ModItems.CRUSHED_RAW_LITHIUM.get(), 1)
        .output(0.75f, AllItems.EXP_NUGGET.get(), 1)
        .output(0.15f, ModItems.BERYLLIUM_DUST.get(), 1));

    GeneratedRecipe CRUSH_LITHIUM_ORE = create("lithium_ore", b -> b
        .duration(250)
        .require(ModBlocks.LITHIUM_ORE.get())
        .output(ModItems.CRUSHED_RAW_LITHIUM.get(), 1)
        .output(0.75f, ModItems.CRUSHED_RAW_LITHIUM.get(), 1)
        .output(0.75f, AllItems.EXP_NUGGET.get(), 1)
        .output(0.25f, ModItems.BERYLLIUM_DUST.get(), 1)
        .output(0.125f, Blocks.COBBLESTONE));

    GeneratedRecipe CRUSH_DEEPSLATE_LITHIUM_ORE = create("deepslate_lithium_ore", b -> b
        .duration(350)
        .require(ModBlocks.DEEPSLATE_LITHIUM_ORE.get())
        .output(ModItems.CRUSHED_RAW_LITHIUM.get(), 2)
        .output(0.25f, ModItems.CRUSHED_RAW_LITHIUM.get(), 1)
        .output(0.75f, AllItems.EXP_NUGGET.get(), 1)
        .output(0.35f, ModItems.BERYLLIUM_DUST.get(), 1)
        .output(0.125f, Blocks.COBBLED_DEEPSLATE));

    // Better than the Millstone: a chance of a third Fiber, plus the woody core as Biomass.
    GeneratedRecipe CRUSH_HEMP_STALK = create("hemp_stalk_to_fiber", b -> b
        .duration(150)
        .require(ModItems.HEMP_STALK.get())
        .output(ModItems.HEMP_FIBER.get(), 2)
        .output(0.5f, ModItems.HEMP_FIBER.get(), 1)
        .output(0.25f, ModItems.BIOMASS.get(), 1));

    public ModCrushingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }
}
