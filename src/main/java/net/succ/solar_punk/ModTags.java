package net.succ.solar_punk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Biomes {
        public static final TagKey<Biome> HAS_SALT_DEPOSITS = TagKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "has_salt_deposits")
        );
    }

    // Fusion Reactor multiblock membership - see plan_for_fusion.md. Internal
    // categorisation tags (not `c:`), same role for the reactor's fixed voxel-sphere
    // scan that block-type `instanceof` checks play for the Turbine's structure scan:
    // lets the scan accept either casing block (Fusion Reactor Casing or its Glass
    // variant) and either blanket module type without hardcoding block classes.
    public static class Blocks {
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
}