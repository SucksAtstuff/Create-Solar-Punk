package net.succ.solar_punk.datagen;

import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

// Building block sets (stairs, slabs, walls, polished and chiseled variants) built on a base
// block. Each family in ALL gets its blockstates, models, loot, tags, crafting recipes and
// stonecutter recipes generated, so a new set only needs its blocks registered in ModBlocks
// (registerStairs/registerSlab/registerWall) and a family here. The base block itself is not
// covered: it keeps its own entries in each provider.
//
// Stairs, slabs and walls use the base block's texture (see baseTexture for ones stored
// elsewhere). Assumes stone-like sets: every variant is pickaxe-mineable and stonecuttable from the base.
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

    public static final BlockFamily SALT_BRICKS = new BlockFamily.Builder(ModBlocks.SALT_BRICKS.get())
            .stairs(ModBlocks.SALT_BRICK_STAIRS.get())
            .slab(ModBlocks.SALT_BRICK_SLAB.get())
            .wall(ModBlocks.SALT_BRICK_WALL.get())
            .chiseled(ModBlocks.CHISELED_SALT_BRICKS.get())
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

    public static final List<BlockFamily> ALL = List.of(SALT, SALT_BRICKS, TURBINE_CASING, FUSION_REACTOR_CASING);

    // Base blocks whose texture isn't at textures/block/<id>.png.
    private static final Map<Block, ResourceLocation> BASE_TEXTURES = Map.of(
            ModBlocks.TURBINE_CASING.get(), tex("industrial_iron_casing/industrial_iron_casing"),
            ModBlocks.FUSION_REACTOR_CASING.get(), tex("fusion_reactor_casing/fusion_reactor_casing"));

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
