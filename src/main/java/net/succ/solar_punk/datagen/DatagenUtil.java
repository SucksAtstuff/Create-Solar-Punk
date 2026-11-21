package net.succ.solar_punk.datagen;

import net.minecraft.world.item.crafting.Ingredient;
import net.succ.solar_punk.ModTags;

import java.util.stream.Stream;

// Solarpunk-only datagen lookups; the generic ones (createItem, itemTag, blockTag) are in the
// library's DatagenUtil.
final class DatagenUtil {

    private DatagenUtil() {}

    /** Any mod's salt. Ours is in all three salt tags, but other mods each use just one. */
    static Ingredient anySalt() {
        return Ingredient.fromValues(Stream.of(
                new Ingredient.TagValue(ModTags.Items.DUSTS_SALT),
                new Ingredient.TagValue(ModTags.Items.GEMS_SALT),
                new Ingredient.TagValue(ModTags.Items.SALT)));
    }
}
