package net.succ.solar_punk.datagen;

import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// Building block sets (stairs, slabs, walls, polished and chiseled variants) built on a base
// block. Each family in ALL gets its blockstates, models, loot, tags, crafting recipes and
// stonecutter recipes generated, so a new set only needs its blocks registered in ModBlocks
// (registerStairs/registerSlab/registerWall) and a family here. The base block itself is not
// covered: it keeps its own entries in each provider.
//
// Stairs, slabs and walls use the base block's texture (see baseTexture for ones stored
// elsewhere). Sets are stone-like (every variant pickaxe-mineable and stonecuttable from the
// base) unless isWooden: then axe-mineable, wooden stair/slab tags, crafting only; or isMoss:
// hoe-mineable, crafting only.
// Full-block variants (polished, chiseled, cut...) use textures/block/<variant id>.png on
// every face, or as the sides only when a <variant id>_top.png exists for the top and bottom.
//
// Only loaded during datagen, after registration, so the .get() calls are safe.
public final class ModBlockFamilies {
    public static final BlockFamily SALT = new BlockFamily.Builder(ModBlocks.SALT_BLOCK.get())
            .stairs(ModBlocks.SALT_STAIRS.get())
            .slab(ModBlocks.SALT_SLAB.get())
            .polished(ModBlocks.POLISHED_SALT.get())
            .getFamily();

    // Polished Salt is both a variant of SALT and the base of its own set, like vanilla's
    // Polished Deepslate.
    public static final BlockFamily POLISHED_SALT = new BlockFamily.Builder(ModBlocks.POLISHED_SALT.get())
            .stairs(ModBlocks.POLISHED_SALT_STAIRS.get())
            .slab(ModBlocks.POLISHED_SALT_SLAB.get())
            .wall(ModBlocks.POLISHED_SALT_WALL.get())
            .getFamily();

    public static final BlockFamily SALT_BRICKS = new BlockFamily.Builder(ModBlocks.SALT_BRICKS.get())
            .stairs(ModBlocks.SALT_BRICK_STAIRS.get())
            .slab(ModBlocks.SALT_BRICK_SLAB.get())
            .wall(ModBlocks.SALT_BRICK_WALL.get())
            .chiseled(ModBlocks.CHISELED_SALT_BRICKS.get())
            .getFamily();

    // The Ash Block is soft (shovel), but its variants are pressed into stone-like blocks.
    public static final BlockFamily ASH = new BlockFamily.Builder(ModBlocks.ASH_BLOCK.get())
            .polished(ModBlocks.POLISHED_ASH.get())
            .getFamily();

    public static final BlockFamily POLISHED_ASH = new BlockFamily.Builder(ModBlocks.POLISHED_ASH.get())
            .stairs(ModBlocks.POLISHED_ASH_STAIRS.get())
            .slab(ModBlocks.POLISHED_ASH_SLAB.get())
            .wall(ModBlocks.POLISHED_ASH_WALL.get())
            .getFamily();

    public static final BlockFamily ASH_BRICKS = new BlockFamily.Builder(ModBlocks.ASH_BRICKS.get())
            .stairs(ModBlocks.ASH_BRICK_STAIRS.get())
            .slab(ModBlocks.ASH_BRICK_SLAB.get())
            .wall(ModBlocks.ASH_BRICK_WALL.get())
            .chiseled(ModBlocks.CHISELED_ASH_BRICKS.get())
            .getFamily();

    // Decorative only: not part of the multiblocks.
    public static final BlockFamily TURBINE_CASING = new BlockFamily.Builder(ModBlocks.TURBINE_CASING.get())
            .stairs(ModBlocks.TURBINE_CASING_STAIRS.get())
            .slab(ModBlocks.TURBINE_CASING_SLAB.get())
            .wall(ModBlocks.TURBINE_CASING_WALL.get())
            .getFamily();

    public static final BlockFamily FUSION_REACTOR_CASING = new BlockFamily.Builder(ModBlocks.FUSION_REACTOR_CASING.get())
            .stairs(ModBlocks.FUSION_REACTOR_CASING_STAIRS.get())
            .slab(ModBlocks.FUSION_REACTOR_CASING_SLAB.get())
            .wall(ModBlocks.FUSION_REACTOR_CASING_WALL.get())
            .getFamily();

    // Two per Rammed Earth colour: the block, and its bricks (with the chiseled bricks). Tinted:
    // every colour shares the grey rammed_earth textures, and the models use the tinted parents
    // in models/block/tinted/.
    public static final List<BlockFamily> RAMMED_EARTH = ModBlocks.RAMMED_EARTH.values().stream()
            .flatMap(set -> Stream.of(
                    new BlockFamily.Builder(set.block().get())
                            .stairs(set.stairs().get())
                            .slab(set.slab().get())
                            .wall(set.wall().get())
                            .getFamily(),
                    new BlockFamily.Builder(set.bricks().get())
                            .stairs(set.brickStairs().get())
                            .slab(set.brickSlab().get())
                            .wall(set.brickWall().get())
                            .chiseled(set.chiseledBricks().get())
                            .getFamily()))
            .toList();

    // Wooden: axe-mineable and in the wooden stair/slab tags, with no stonecutter recipes.
    public static final BlockFamily CHARRED_TIMBER = new BlockFamily.Builder(ModBlocks.CHARRED_TIMBER.get())
            .stairs(ModBlocks.CHARRED_TIMBER_STAIRS.get())
            .slab(ModBlocks.CHARRED_TIMBER_SLAB.get())
            .getFamily();

    // Built on vanilla's Moss Block: hoe-mineable, crafting only.
    public static final BlockFamily MOSS = new BlockFamily.Builder(Blocks.MOSS_BLOCK)
            .stairs(ModBlocks.MOSS_STAIRS.get())
            .slab(ModBlocks.MOSS_SLAB.get())
            .getFamily();

    public static final List<BlockFamily> ALL = Stream.concat(
            Stream.of(SALT, POLISHED_SALT, SALT_BRICKS, ASH, POLISHED_ASH, ASH_BRICKS, TURBINE_CASING, FUSION_REACTOR_CASING,
                    CHARRED_TIMBER, MOSS),
            RAMMED_EARTH.stream()).toList();

    // Base blocks whose texture isn't at textures/block/<id>.png.
    private static final Map<Block, ResourceLocation> BASE_TEXTURES = new HashMap<>();
    static {
        BASE_TEXTURES.put(ModBlocks.TURBINE_CASING.get(), tex("industrial_iron_casing/industrial_iron_casing"));
        BASE_TEXTURES.put(ModBlocks.FUSION_REACTOR_CASING.get(), tex("fusion_reactor_casing/fusion_reactor_casing"));
        ModBlocks.RAMMED_EARTH.values().forEach(set -> {
            BASE_TEXTURES.put(set.block().get(), tex("rammed_earth"));
            BASE_TEXTURES.put(set.bricks().get(), tex("rammed_earth_bricks"));
        });
    }

    // Tinted full-block variants (the chiseled bricks), which share one grey texture.
    private static final Map<Block, ResourceLocation> TINTED_VARIANT_TEXTURES = new HashMap<>();
    static {
        ModBlocks.RAMMED_EARTH.values().forEach(set ->
                TINTED_VARIANT_TEXTURES.put(set.chiseledBricks().get(), tex("chiseled_rammed_earth")));
    }

    @Nullable
    public static ResourceLocation tintedVariantTexture(Block block) {
        return TINTED_VARIANT_TEXTURES.get(block);
    }

    public static boolean isWooden(BlockFamily family) {
        return family == CHARRED_TIMBER;
    }

    public static boolean isMoss(BlockFamily family) {
        return family == MOSS;
    }

    // Stone-like families get stonecutter recipes; wood and moss don't.
    public static boolean isStonecuttable(BlockFamily family) {
        return !isWooden(family) && !isMoss(family);
    }

    public static boolean isTinted(BlockFamily family) {
        return RAMMED_EARTH.contains(family);
    }

    // BlockFamily keeps its variants in a HashMap, so they come out in a different order each
    // run. Datagen goes through them in Variant order instead, so tag files don't reshuffle.
    public static Map<BlockFamily.Variant, Block> variants(BlockFamily family) {
        Map<BlockFamily.Variant, Block> sorted = new EnumMap<>(BlockFamily.Variant.class);
        sorted.putAll(family.getVariants());
        return sorted;
    }

    // The texture stairs, slabs and walls use, or null for textures/block/<base id>.png.
    @Nullable
    public static ResourceLocation baseTexture(BlockFamily family) {
        return BASE_TEXTURES.get(family.getBaseBlock());
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/" + path);
    }

    private ModBlockFamilies() {}
}
