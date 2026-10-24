package net.succ.solar_punk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

    // Common c: tags for our materials, so other mods' versions work in our recipes and
    // machines (and ours in theirs), plus Create's sheets and zinc that our recipes use.
    public static class Items {
        // Salt: other mods disagree on the tag (Northstar uses c:dusts/salt, Create: Food
        // uses c:salt), so ours is in all three and recipes accept any of them.
        public static final TagKey<Item> GEMS_SALT = common("gems/salt");
        public static final TagKey<Item> DUSTS_SALT = common("dusts/salt");
        public static final TagKey<Item> SALT = common("salt");
        public static final TagKey<Item> STORAGE_BLOCKS_SALT = common("storage_blocks/salt");

        public static final TagKey<Item> ORES_LITHIUM = common("ores/lithium");
        public static final TagKey<Item> RAW_MATERIALS_LITHIUM = common("raw_materials/lithium");
        public static final TagKey<Item> INGOTS_LITHIUM = common("ingots/lithium");
        public static final TagKey<Item> NUGGETS_LITHIUM = common("nuggets/lithium");
        public static final TagKey<Item> DUSTS_LITHIUM = common("dusts/lithium");
        public static final TagKey<Item> STORAGE_BLOCKS_LITHIUM = common("storage_blocks/lithium");

        public static final TagKey<Item> INGOTS_BERYLLIUM = common("ingots/beryllium");
        public static final TagKey<Item> NUGGETS_BERYLLIUM = common("nuggets/beryllium");
        public static final TagKey<Item> DUSTS_BERYLLIUM = common("dusts/beryllium");
        public static final TagKey<Item> STORAGE_BLOCKS_BERYLLIUM = common("storage_blocks/beryllium");

        public static final TagKey<Item> PLATES_IRON = common("plates/iron");
        public static final TagKey<Item> PLATES_COPPER = common("plates/copper");
        public static final TagKey<Item> PLATES_BRASS = common("plates/brass");
        public static final TagKey<Item> INGOTS_ZINC = common("ingots/zinc");

        private static TagKey<Item> common(String path) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
        }
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
