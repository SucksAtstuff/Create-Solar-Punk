package net.succ.solar_punk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;

public class ModTags {
    public static class Biomes {
        public static final TagKey<Biome> HAS_SALT_DEPOSITS = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "has_salt_deposits")
        );
    }

    // The reactor_* tags define Fusion Reactor membership: which blocks count as Casing
    // (solid or glass) and as each blanket module type in FusionReactorCoreBlockEntity's
    // structure scan.
    public static class Blocks {
        // Blocks that pollute while active (see GlobalWarmingHandler).
        public static final TagKey<Block> POLLUTION_SOURCES = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "pollution_sources")
        );

        public static final TagKey<Block> REACTOR_CASING = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "reactor_casing")
        );
        public static final TagKey<Block> REACTOR_BLANKET_LITHIUM = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "reactor_blanket_lithium")
        );
        public static final TagKey<Block> REACTOR_BLANKET_BERYLLIUM = TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "reactor_blanket_beryllium")
        );
    }

    // Solar behaviour per dimension - see SolarHelper. Lets modpacks and space mods opt
    // their dimensions in with a datapack, with no compile dependency either way.
    public static class DimensionTypes {
        // No day/night cycle and no weather: solar blocks run at noon output around the clock.
        public static final TagKey<DimensionType> ALWAYS_SUNNY = TagKey.create(
                Registries.DIMENSION_TYPE,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "always_sunny")
        );
        // Normal day/night cycle but no weather, e.g. airless moons.
        public static final TagKey<DimensionType> NO_WEATHER = TagKey.create(
                Registries.DIMENSION_TYPE,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "no_weather")
        );
    }
}
