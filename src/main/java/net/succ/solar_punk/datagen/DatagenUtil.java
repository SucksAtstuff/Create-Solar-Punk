package net.succ.solar_punk.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.succ.solar_punk.ModTags;

import java.util.stream.Stream;

// Small lookups shared by the data generators.
final class DatagenUtil {

    private DatagenUtil() {}

    /** An item from Create, e.g. {@code createItem("brass_casing")}. */
    static Item createItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }

    static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    /** Any mod's salt. Ours is in all three salt tags, but other mods each use just one. */
    static Ingredient anySalt() {
        return Ingredient.fromValues(Stream.of(
                new Ingredient.TagValue(ModTags.Items.DUSTS_SALT),
                new Ingredient.TagValue(ModTags.Items.GEMS_SALT),
                new Ingredient.TagValue(ModTags.Items.SALT)));
    }

    static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
