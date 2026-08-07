package net.succ.solar_punk.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.succ.solar_punk.Config;

/**
 * Same job as NeoForge's stock "neoforge:add_features" modifier, but the biome list
 * isn't baked into datapack JSON - it's read from Config.geyserBiomes each time this
 * modifier is applied, so editing "geyser_biomes" in the config actually moves where
 * Geyser Vents can generate, without needing a datapack override.
 * <p>
 * Config is guaranteed to be loaded by the time this runs: COMMON config loads during
 * mod construction, and biome modifiers are only evaluated once dynamic registries are
 * baked at world/server startup, well after that.
 */
public record ConfigurableGeyserBiomeModifier(HolderSet<PlacedFeature> features,
                                               GenerationStep.Decoration step) implements BiomeModifier {

    public static final MapCodec<ConfigurableGeyserBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConfigurableGeyserBiomeModifier::features),
                    GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConfigurableGeyserBiomeModifier::step))
            .apply(instance, ConfigurableGeyserBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !matchesConfiguredBiomes(biome)) return;
        BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
        features.forEach(holder -> generationSettings.addFeature(step, holder));
    }

    private boolean matchesConfiguredBiomes(Holder<Biome> biome) {
        return biome.unwrapKey()
                .map(key -> Config.geyserBiomes.contains(key.location().toString()))
                .orElse(false);
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModBiomeModifierTypes.CONFIGURABLE_GEYSER_BIOMES.get();
    }
}
