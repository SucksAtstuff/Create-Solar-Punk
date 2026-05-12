package net.succ.solar_punk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModTags {
    public static class Biomes {
        public static final TagKey<Biome> HAS_SALT_DEPOSITS = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "has_salt_deposits")
        );

        public static final TagKey<Biome> HAS_GEYSERS = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "has_geysers")
        );
    }
}