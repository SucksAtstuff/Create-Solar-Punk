package net.succ.solar_punk.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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

    static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
