package net.succ.solar_punk.block.custom;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * What grows on a {@link TrellisBlock}. Vines come first; a flower or Glow Berries then go on
 * top of the vines. Flowers share one model whose grey flower is tinted from the flower's own
 * texture (see FlowerTint), so each one here only needs its block.
 */
public enum TrellisPlant implements StringRepresentable {
    NONE("none", null),
    VINES("vines", () -> Blocks.VINE),
    GLOW_BERRIES("glow_berries", () -> Blocks.CAVE_VINES),
    DANDELION("dandelion", () -> Blocks.DANDELION),
    POPPY("poppy", () -> Blocks.POPPY),
    BLUE_ORCHID("blue_orchid", () -> Blocks.BLUE_ORCHID),
    ALLIUM("allium", () -> Blocks.ALLIUM),
    AZURE_BLUET("azure_bluet", () -> Blocks.AZURE_BLUET),
    RED_TULIP("red_tulip", () -> Blocks.RED_TULIP),
    ORANGE_TULIP("orange_tulip", () -> Blocks.ORANGE_TULIP),
    WHITE_TULIP("white_tulip", () -> Blocks.WHITE_TULIP),
    PINK_TULIP("pink_tulip", () -> Blocks.PINK_TULIP),
    OXEYE_DAISY("oxeye_daisy", () -> Blocks.OXEYE_DAISY),
    CORNFLOWER("cornflower", () -> Blocks.CORNFLOWER),
    LILY_OF_THE_VALLEY("lily_of_the_valley", () -> Blocks.LILY_OF_THE_VALLEY),
    TORCHFLOWER("torchflower", () -> Blocks.TORCHFLOWER),
    WITHER_ROSE("wither_rose", () -> Blocks.WITHER_ROSE);

    private final String name;
    @Nullable private final Supplier<Block> block;

    TrellisPlant(String name, @Nullable Supplier<Block> block) {
        this.name = name;
        this.block = block;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean hasVines() {
        return this != NONE;
    }

    public boolean isFlower() {
        return this != NONE && this != VINES && this != GLOW_BERRIES;
    }

    /** The flower block whose texture tints the flower model; null for non-flowers. */
    @Nullable
    public Block flowerBlock() {
        return isFlower() ? block.get() : null;
    }

    /** The item this layer is added with and drops as (Glow Berries for the berries). */
    public Item item() {
        if (this == NONE) return Items.AIR;
        if (this == GLOW_BERRIES) return Items.GLOW_BERRIES;
        return block.get().asItem();
    }

    /** The layer the item in hand adds, or null if it doesn't add one. */
    @Nullable
    public static TrellisPlant forItem(ItemStack stack) {
        for (TrellisPlant plant : values()) {
            if (plant != NONE && stack.is(plant.item())) return plant;
        }
        return null;
    }
}
