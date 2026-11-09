package net.succ.solar_punk.wire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.item.ModItems;

import java.util.function.Supplier;

/** The metal a wire is made of: how much FE it carries, how long it can be, and how it looks. */
public enum WireTier implements StringRepresentable {
    IRON("iron", ModItems.IRON_WIRE::get, "solarpunk:textures/entity/wire/iron.png"),
    COPPER("copper", ModItems.COPPER_WIRE::get, "solarpunk:textures/entity/wire/copper.png"),
    GOLD("gold", ModItems.GOLD_WIRE::get, "solarpunk:textures/entity/wire/gold.png");

    public static final StringRepresentable.EnumCodec<WireTier> CODEC = StringRepresentable.fromEnum(WireTier::values);

    private final String name;
    private final Supplier<Item> wire;
    private final ResourceLocation texture;

    WireTier(String name, Supplier<Item> wire, String texture) {
        this.name = name;
        this.wire = wire;
        this.texture = ResourceLocation.parse(texture);
    }

    public int rate() {
        return switch (this) {
            case IRON -> Config.wireIronRate;
            case COPPER -> Config.wireCopperRate;
            case GOLD -> Config.wireGoldRate;
        };
    }

    public int maxLength() {
        return switch (this) {
            case IRON -> Config.wireIronLength;
            case COPPER -> Config.wireCopperLength;
            case GOLD -> Config.wireGoldLength;
        };
    }

    public Item wireItem() {
        return wire.get();
    }

    /** The twisted-strand texture WireConnectorRenderer wraps around the wire. */
    public ResourceLocation texture() {
        return texture;
    }

    /** Wire items a wire of this length uses (and gives back when it's removed). */
    public static int wiresFor(double length) {
        return Math.max(1, (int) Math.ceil(length / Config.wireBlocksPerWire));
    }

    public static WireTier byName(String name) {
        WireTier tier = CODEC.byName(name);
        return tier == null ? COPPER : tier;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
