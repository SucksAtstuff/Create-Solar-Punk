package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

    private static final TagKey<Item> BLAZE_BURNER_FUEL_REGULAR =
            TagKey.create(Registries.ITEM, ResourceLocation.parse("create:blaze_burner_fuel/regular"));

    // Custom c: sub-tags not predefined in NeoForge
    private static final TagKey<Item> GEMS_SALT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "gems/salt"));
    private static final TagKey<Item> STORAGE_BLOCKS_SALT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/salt"));
    private static final TagKey<Item> BUCKETS_BIOFUEL =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "buckets/biofuel"));
    private static final TagKey<Item> BUCKETS_MOLTEN_SALT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "buckets/molten_salt"));
    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTagProvider,
                              ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Salt — crystalline mineral, compatible with other mods that use c:gems/salt
        tag(Tags.Items.GEMS).add(ModItems.SALT.get());
        tag(GEMS_SALT).add(ModItems.SALT.get());

        // Salt block item
        tag(Tags.Items.STORAGE_BLOCKS).add(ModBlocks.SALT_BLOCK.get().asItem());
        tag(STORAGE_BLOCKS_SALT).add(ModBlocks.SALT_BLOCK.get().asItem());

        // Biomass — biofuel feedstock
        tag(BIO_FUELS).add(ModItems.BIOMASS.get()).add(ModItems.BIOMASS_PELLET.get());

        // Fluid buckets
        tag(Tags.Items.BUCKETS).add(ModFluids.BIOFUEL_BUCKET.get()).add(ModFluids.MOLTEN_SALT_BUCKET.get());
        tag(BUCKETS_BIOFUEL).add(ModFluids.BIOFUEL_BUCKET.get());
        tag(BUCKETS_MOLTEN_SALT).add(ModFluids.MOLTEN_SALT_BUCKET.get());

        // Create compat
        tag(BLAZE_BURNER_FUEL_REGULAR)
                .add(ModItems.BIOMASS.get())
                .add(ModItems.BIOMASS_PELLET.get())
                .add(ModFluids.BIOFUEL_BUCKET.get());
    }
}