package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    private static final TagKey<Block> STORAGE_BLOCKS_SALT =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/salt"));
    private static final TagKey<Block> ORES_LITHIUM =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/lithium"));
    private static final TagKey<Block> STORAGE_BLOCKS_LITHIUM =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/lithium"));
    private static final TagKey<Block> STORAGE_BLOCKS_BERYLLIUM =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/beryllium"));

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ANDESITE_SOLAR_PANEL.get())
                .add(ModBlocks.BRASS_SOLAR_PANEL.get())
                .add(ModBlocks.SOLAR_HEATER.get())
                .add(ModBlocks.FIREBOX_BOILER.get())
                .add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.LITHIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get())
                .add(ModBlocks.LITHIUM_BLOCK.get())
                .add(ModBlocks.BERYLLIUM_BLOCK.get())
                .add(ModBlocks.KINETIC_BATTERY.get())
                .add(ModBlocks.HEAT_BATTERY.get())
                .add(ModBlocks.FERMENTATION_VAT.get())
                .add(ModBlocks.BIOMASS_GASIFIER.get())
                .add(ModBlocks.BIOFUEL_ENGINE.get())
                .add(ModBlocks.GEYSER_CAP.get())
                .add(ModBlocks.SOLAR_POWER_TOWER.get())
                .add(ModBlocks.SOLAR_MIRROR.get())
                .add(ModBlocks.GEYSER_VENT.get())
                .add(ModBlocks.BIOFILTER.get())
                .add(ModBlocks.KINETIC_SPRINKLER.get())
                .add(ModBlocks.TURBINE_CASING.get())
                .add(ModBlocks.TURBINE_CASING_GLASS.get())
                .add(ModBlocks.TURBINE_ROTOR.get())
                .add(ModBlocks.ANDESITE_TURBINE_BLADE.get())
                .add(ModBlocks.BRASS_TURBINE_BLADE.get())
                .add(ModBlocks.TURBINE_ROTOR.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.ASH_BLOCK.get())
                .add(ModBlocks.DEAD_GRASS_BLOCK.get())
                .add(ModBlocks.RUINED_DIRT.get());

        tag(Tags.Blocks.STORAGE_BLOCKS).add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.LITHIUM_BLOCK.get())
                .add(ModBlocks.BERYLLIUM_BLOCK.get());
        tag(STORAGE_BLOCKS_SALT).add(ModBlocks.SALT_BLOCK.get());
        tag(STORAGE_BLOCKS_LITHIUM).add(ModBlocks.LITHIUM_BLOCK.get());
        tag(STORAGE_BLOCKS_BERYLLIUM).add(ModBlocks.BERYLLIUM_BLOCK.get());

        // Mid-late-game hard-rock ore - gated behind iron tools, unlike Salt which needs
        // no tool tier at all.
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(ORES_LITHIUM).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(ModBlocks.LITHIUM_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());
        tag(Tags.Blocks.ORE_RATES_SINGULAR).add(ModBlocks.LITHIUM_ORE.get()).add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get());

        tag(GlobalWarmingHandler.POLLUTION_SOURCES)
                .add(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
                     ModBlocks.BIOFUEL_ENGINE.get(), ModBlocks.BIOMASS_GASIFIER.get(), ModBlocks.FIREBOX_BOILER.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "lit_blaze_burner"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("create", "steam_engine"))
                // Create Diesel Generators
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "diesel_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "large_diesel_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "huge_diesel_engine"))
                // Create Aeronautics portable engines (one per dye colour)
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "white_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "orange_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "magenta_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "light_blue_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "yellow_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "lime_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "pink_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "gray_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "light_gray_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "cyan_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "purple_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "blue_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "brown_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "green_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "red_portable_engine"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("simulated", "black_portable_engine"));
    }
}