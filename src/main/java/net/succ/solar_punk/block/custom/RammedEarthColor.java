package net.succ.solar_punk.block.custom;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.MapColor;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.ModBlocks;

import java.util.function.Supplier;

/**
 * The Rammed Earth colours. Every colour shares one grey texture (textures/block/rammed_earth.png),
 * tinted with {@link #color} in ModClientEvents, so a new colour is one line here. Each gets a
 * block, stairs, slab and wall (ModBlocks.RAMMED_EARTH), compacted from its {@link #soil}
 * (ModCompactingRecipeGen).
 */
public enum RammedEarthColor {
    PLAIN("rammed_earth", 0x9C7656, MapColor.DIRT, () -> Ingredient.of(Items.DIRT)),
    SANDY("sandy_rammed_earth", 0xE2D2A6, MapColor.SAND, () -> Ingredient.of(Items.SAND)),
    RED("red_rammed_earth", 0xC9703F, MapColor.COLOR_ORANGE, () -> Ingredient.of(Items.RED_SAND)),
    ASHEN("ashen_rammed_earth", 0x7D7773, MapColor.COLOR_GRAY, () -> Ingredient.of(ModBlocks.ASH_BLOCK.get())),
    MUDDY("muddy_rammed_earth", 0x5C565B, MapColor.TERRACOTTA_CYAN, () -> Ingredient.of(Items.MUD)),
    CLAY("clay_rammed_earth", 0xB8BFCE, MapColor.CLAY, () -> Ingredient.of(Items.CLAY)),
    // Ruined Dirt from global warming decay, so wasteland ground can be built with again.
    RUINED("ruined_rammed_earth", 0x7D6351, MapColor.COLOR_BROWN, () -> Ingredient.of(ModBlocks.RUINED_DIRT.get())),
    SALTY("salty_rammed_earth", 0xF2ECE4, MapColor.QUARTZ, () -> Ingredient.of(ModTags.Items.STORAGE_BLOCKS_SALT)),
    // Terracotta: the vanilla block's colour, lifted a little since the texture darkens it.
    TERRACOTTA("terracotta_rammed_earth", 0xAF6C4D, MapColor.TERRACOTTA_ORANGE, () -> Ingredient.of(Items.TERRACOTTA)),
    WHITE_TERRACOTTA("white_terracotta_rammed_earth", 0xF0CDB9, MapColor.TERRACOTTA_WHITE, () -> Ingredient.of(Items.WHITE_TERRACOTTA)),
    ORANGE_TERRACOTTA("orange_terracotta_rammed_earth", 0xB95F2B, MapColor.TERRACOTTA_ORANGE, () -> Ingredient.of(Items.ORANGE_TERRACOTTA)),
    MAGENTA_TERRACOTTA("magenta_terracotta_rammed_earth", 0xAB657C, MapColor.TERRACOTTA_MAGENTA, () -> Ingredient.of(Items.MAGENTA_TERRACOTTA)),
    LIGHT_BLUE_TERRACOTTA("light_blue_terracotta_rammed_earth", 0x827C9E, MapColor.TERRACOTTA_LIGHT_BLUE, () -> Ingredient.of(Items.LIGHT_BLUE_TERRACOTTA)),
    YELLOW_TERRACOTTA("yellow_terracotta_rammed_earth", 0xD69928, MapColor.TERRACOTTA_YELLOW, () -> Ingredient.of(Items.YELLOW_TERRACOTTA)),
    LIME_TERRACOTTA("lime_terracotta_rammed_earth", 0x76873C, MapColor.TERRACOTTA_LIGHT_GREEN, () -> Ingredient.of(Items.LIME_TERRACOTTA)),
    PINK_TERRACOTTA("pink_terracotta_rammed_earth", 0xB95A5A, MapColor.TERRACOTTA_PINK, () -> Ingredient.of(Items.PINK_TERRACOTTA)),
    GRAY_TERRACOTTA("gray_terracotta_rammed_earth", 0x423028, MapColor.TERRACOTTA_GRAY, () -> Ingredient.of(Items.GRAY_TERRACOTTA)),
    LIGHT_GRAY_TERRACOTTA("light_gray_terracotta_rammed_earth", 0x9B7A70, MapColor.TERRACOTTA_LIGHT_GRAY, () -> Ingredient.of(Items.LIGHT_GRAY_TERRACOTTA)),
    CYAN_TERRACOTTA("cyan_terracotta_rammed_earth", 0x646969, MapColor.TERRACOTTA_CYAN, () -> Ingredient.of(Items.CYAN_TERRACOTTA)),
    PURPLE_TERRACOTTA("purple_terracotta_rammed_earth", 0x885063, MapColor.TERRACOTTA_PURPLE, () -> Ingredient.of(Items.PURPLE_TERRACOTTA)),
    BLUE_TERRACOTTA("blue_terracotta_rammed_earth", 0x554469, MapColor.TERRACOTTA_BLUE, () -> Ingredient.of(Items.BLUE_TERRACOTTA)),
    BROWN_TERRACOTTA("brown_terracotta_rammed_earth", 0x593B28, MapColor.TERRACOTTA_BROWN, () -> Ingredient.of(Items.BROWN_TERRACOTTA)),
    GREEN_TERRACOTTA("green_terracotta_rammed_earth", 0x575E30, MapColor.TERRACOTTA_GREEN, () -> Ingredient.of(Items.GREEN_TERRACOTTA)),
    RED_TERRACOTTA("red_terracotta_rammed_earth", 0xA44635, MapColor.TERRACOTTA_RED, () -> Ingredient.of(Items.RED_TERRACOTTA)),
    BLACK_TERRACOTTA("black_terracotta_rammed_earth", 0x2B1912, MapColor.TERRACOTTA_BLACK, () -> Ingredient.of(Items.BLACK_TERRACOTTA)),
    ;

    /** The base block's id; stairs, slab and wall add _stairs, _slab and _wall. */
    public final String id;
    /** RGB tint for the grey texture. */
    public final int color;
    public final MapColor mapColor;
    /** The soil compacted with Gravel, Clay and a binder to make it. Only used by datagen. */
    public final Supplier<Ingredient> soil;

    RammedEarthColor(String id, int color, MapColor mapColor, Supplier<Ingredient> soil) {
        this.id = id;
        this.color = color;
        this.mapColor = mapColor;
        this.soil = soil;
    }
}
