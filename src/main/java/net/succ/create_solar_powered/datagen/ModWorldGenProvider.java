package net.succ.create_solar_powered.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.ModTags;
import net.succ.create_solar_powered.block.ModBlocks;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final ResourceKey<ConfiguredFeature<?, ?>> SALT_DEPOSIT_CONFIGURED = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "salt_deposit")
    );

    public static final ResourceKey<PlacedFeature> SALT_DEPOSIT_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "salt_deposit")
    );

    public static final ResourceKey<BiomeModifier> ADD_SALT_DEPOSITS = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID, "add_salt_deposits")
    );

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldGenProvider::bootstrapBiomeModifiers);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Create_solar_powered.MODID));
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(SALT_DEPOSIT_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(
                List.of(
                        OreConfiguration.target(new TagMatchTest(BlockTags.SAND),
                                ModBlocks.SALT_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new BlockMatchTest(Blocks.SANDSTONE),
                                ModBlocks.SALT_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new BlockMatchTest(Blocks.RED_SANDSTONE),
                                ModBlocks.SALT_BLOCK.get().defaultBlockState()),
                        OreConfiguration.target(new TagMatchTest(BlockTags.TERRACOTTA),
                                ModBlocks.SALT_BLOCK.get().defaultBlockState())
                ),
                20, 0.0f
        )));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        var configured = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(SALT_DEPOSIT_CONFIGURED);
        context.register(SALT_DEPOSIT_PLACED, new PlacedFeature(configured, List.of(
                CountPlacement.of(4),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(48), VerticalAnchor.absolute(90)),
                BiomeFilter.biome()
        )));
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placed = context.lookup(Registries.PLACED_FEATURE);
        context.register(ADD_SALT_DEPOSITS, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.HAS_SALT_DEPOSITS),
                HolderSet.direct(placed.getOrThrow(SALT_DEPOSIT_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }
}