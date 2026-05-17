package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    private static final TagKey<Block> STORAGE_BLOCKS_SALT =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/salt"));

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
                .add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.KINETIC_BATTERY.get())
                .add(ModBlocks.HEAT_BATTERY.get())
                .add(ModBlocks.FERMENTATION_VAT.get())
                .add(ModBlocks.BIOMASS_GASIFIER.get())
                .add(ModBlocks.BIOFUEL_ENGINE.get())
                .add(ModBlocks.GEYSER_CAP.get());

        tag(Tags.Blocks.STORAGE_BLOCKS).add(ModBlocks.SALT_BLOCK.get());
        tag(STORAGE_BLOCKS_SALT).add(ModBlocks.SALT_BLOCK.get());
    }
}