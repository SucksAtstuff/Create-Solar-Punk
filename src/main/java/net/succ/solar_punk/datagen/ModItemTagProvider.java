package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static net.succ.solar_punk.datagen.DatagenUtil.itemTag;

public class ModItemTagProvider extends ItemTagsProvider {

    private static final TagKey<Item> BLAZE_BURNER_FUEL_REGULAR = itemTag("create", "blaze_burner_fuel/regular");

    // Custom c: sub-tags not predefined in NeoForge
    private static final TagKey<Item> BUCKETS_BIOFUEL = itemTag("c", "buckets/biofuel");
    private static final TagKey<Item> BUCKETS_MOLTEN_SALT = itemTag("c", "buckets/molten_salt");
    private static final TagKey<Item> BUCKETS_FERTILIZER = itemTag("c", "buckets/fertilizer");
    private static final TagKey<Item> BIO_FUELS = itemTag("c", "bio_fuels");
    private static final TagKey<Item> BUCKETS_STEAM = itemTag("c", "buckets/steam");

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTagProvider,
                              ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.Items.GEMS).add(ModItems.SALT.get());
        tag(ModTags.Items.GEMS_SALT).add(ModItems.SALT.get());
        tag(ModTags.Items.DUSTS_SALT).add(ModItems.SALT.get());
        tag(ModTags.Items.SALT).add(ModItems.SALT.get());

        // Salt block item
        tag(Tags.Items.GLASS_BLOCKS).add(ModBlocks.SOLAR_GLASS.get().asItem());
        tag(Tags.Items.GLASS_PANES).add(ModBlocks.SOLAR_GLASS_PANE.get().asItem());
        tag(Tags.Items.GLASS_BLOCKS).add(ModBlocks.GREENHOUSE_GLASS.get().asItem(), ModBlocks.CLEAR_GREENHOUSE_GLASS.get().asItem(),
                ModBlocks.GREENHOUSE_PANEL.get().asItem(), ModBlocks.LARGE_GREENHOUSE_PANEL.get().asItem());
        tag(Tags.Items.GLASS_PANES).add(ModBlocks.GREENHOUSE_GLASS_PANE.get().asItem(), ModBlocks.CLEAR_GREENHOUSE_GLASS_PANE.get().asItem());
        tag(Tags.Items.STORAGE_BLOCKS).add(ModBlocks.SALT_BLOCK.get().asItem());
        tag(ModTags.Items.STORAGE_BLOCKS_SALT).add(ModBlocks.SALT_BLOCK.get().asItem());

        // Lithium ore chain: the standard c:raw_materials, c:ingots, c:nuggets and c:dusts tags
        tag(Tags.Items.ORES).add(ModBlocks.LITHIUM_ORE.get().asItem(), ModBlocks.DEEPSLATE_LITHIUM_ORE.get().asItem());
        tag(ModTags.Items.ORES_LITHIUM).add(ModBlocks.LITHIUM_ORE.get().asItem(), ModBlocks.DEEPSLATE_LITHIUM_ORE.get().asItem());
        tag(Tags.Items.RAW_MATERIALS).add(ModItems.RAW_LITHIUM.get());
        tag(ModTags.Items.RAW_MATERIALS_LITHIUM).add(ModItems.RAW_LITHIUM.get());
        tag(Tags.Items.INGOTS).add(ModItems.LITHIUM_INGOT.get());
        tag(ModTags.Items.INGOTS_LITHIUM).add(ModItems.LITHIUM_INGOT.get());
        tag(Tags.Items.NUGGETS).add(ModItems.LITHIUM_NUGGET.get());
        tag(ModTags.Items.NUGGETS_LITHIUM).add(ModItems.LITHIUM_NUGGET.get());
        tag(Tags.Items.DUSTS).add(ModItems.LITHIUM_DUST.get(), ModItems.CARBON_DUST.get(), ModItems.ASH.get());
        tag(ModTags.Items.DUSTS_LITHIUM).add(ModItems.LITHIUM_DUST.get());

        // Beryllium: no ore of its own (see ModItems), just the dust byproduct
        tag(Tags.Items.DUSTS).add(ModItems.BERYLLIUM_DUST.get());
        tag(ModTags.Items.DUSTS_BERYLLIUM).add(ModItems.BERYLLIUM_DUST.get());
        tag(Tags.Items.INGOTS).add(ModItems.BERYLLIUM_INGOT.get());
        tag(ModTags.Items.INGOTS_BERYLLIUM).add(ModItems.BERYLLIUM_INGOT.get());
        tag(Tags.Items.NUGGETS).add(ModItems.BERYLLIUM_NUGGET.get());
        tag(ModTags.Items.NUGGETS_BERYLLIUM).add(ModItems.BERYLLIUM_NUGGET.get());

        // Storage blocks
        tag(Tags.Items.STORAGE_BLOCKS).add(ModBlocks.LITHIUM_BLOCK.get().asItem())
                .add(ModBlocks.BERYLLIUM_BLOCK.get().asItem());
        tag(ModTags.Items.STORAGE_BLOCKS_LITHIUM).add(ModBlocks.LITHIUM_BLOCK.get().asItem());
        tag(ModTags.Items.STORAGE_BLOCKS_BERYLLIUM).add(ModBlocks.BERYLLIUM_BLOCK.get().asItem());

        // Biomass: biofuel feedstock
        tag(BIO_FUELS).add(ModItems.BIOMASS.get()).add(ModItems.BIOMASS_PELLET.get());

        // Fluid buckets
        tag(Tags.Items.BUCKETS).add(ModFluids.BIOFUEL_BUCKET.get())
                .add(ModFluids.MOLTEN_SALT_BUCKET.get())
                .add(ModFluids.STEAM_BUCKET.get())
                .add(ModFluids.FERTILIZER_BUCKET.get())
                .add(ModFluids.RICH_FERTILIZER_BUCKET.get());
        // Waste the Fermentation Vat turns into Rich Fertilizer.
        tag(ModTags.Items.FERMENTATION_WASTE).add(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
                Items.BONE, Items.PUFFERFISH);
        tag(BUCKETS_BIOFUEL).add(ModFluids.BIOFUEL_BUCKET.get());
        tag(BUCKETS_MOLTEN_SALT).add(ModFluids.MOLTEN_SALT_BUCKET.get());
        tag(BUCKETS_FERTILIZER).add(ModFluids.FERTILIZER_BUCKET.get());
        tag(BUCKETS_STEAM).add(ModFluids.STEAM_BUCKET.get());

        tag(ModTags.Items.WIRES).addTag(ModTags.Items.WIRES_METAL);
        tag(ModTags.Items.WIRES_METAL).add(ModItems.COPPER_WIRE.get(), ModItems.IRON_WIRE.get(), ModItems.GOLD_WIRE.get());
        tag(ModTags.Items.WIRES_COPPER).add(ModItems.COPPER_WIRE.get());
        tag(ModTags.Items.WIRES_IRON).add(ModItems.IRON_WIRE.get());
        tag(ModTags.Items.WIRES_GOLD).add(ModItems.GOLD_WIRE.get());

        // Create compat
        tag(BLAZE_BURNER_FUEL_REGULAR)
                .add(ModItems.BIOMASS.get())
                .add(ModItems.BIOMASS_PELLET.get())
                .add(ModFluids.BIOFUEL_BUCKET.get());
    }
}