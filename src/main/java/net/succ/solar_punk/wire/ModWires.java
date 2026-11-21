package net.succ.solar_punk.wire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.item.ModDataComponents;
import net.succ.solar_punk.item.ModItems;
import net.succ.offbyonelib.wire.WireSystem;
import net.succ.offbyonelib.wire.WireTier;
import net.succ.offbyonelib.wire.WireTiers;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Solarpunk's wires: the library's wire code with Solarpunk's lang keys, config, components
 * and block entities, and the copper, iron and gold tiers. Tiers are saved on wires by id
 * (solarpunk:copper); wires from before 2.2 saved only "copper" and still load.
 */
public final class ModWires {

    public static final WireSystem SYSTEM = new WireSystem(SolarPunk.MODID, SolarPunk.GOGGLES,
            () -> Config.wireMaxConnections, () -> Config.wireBlocksPerWire,
            () -> ModDataComponents.WIRE_START.get(), () -> ModDataComponents.WIRE_START_TERMINAL.get(),
            () -> ModItems.EMPTY_SPOOL.get(),
            () -> ModBlockEntities.WIRE_CONNECTOR.get(), () -> ModBlockEntities.WIRE_SWITCH.get());

    // Copper first: a wire whose tier can't be found falls back to the first one registered.
    public static final WireTier COPPER = tier("copper", () -> ModItems.COPPER_WIRE.get(),
            () -> Config.wireCopperRate, () -> Config.wireCopperLength);
    public static final WireTier IRON = tier("iron", () -> ModItems.IRON_WIRE.get(),
            () -> Config.wireIronRate, () -> Config.wireIronLength);
    public static final WireTier GOLD = tier("gold", () -> ModItems.GOLD_WIRE.get(),
            () -> Config.wireGoldRate, () -> Config.wireGoldLength);

    private static WireTier tier(String metal, Supplier<Item> wireItem, IntSupplier rate, IntSupplier length) {
        return WireTiers.register(new WireTier(
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, metal), wireItem,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/entity/wire/" + metal + ".png"),
                rate, length));
    }

    /** Registers the tiers. Called from the mod constructor, before any world loads. */
    public static void init() {}

    private ModWires() {}
}
