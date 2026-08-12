package net.succ.solar_punk.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SolarPunk.MODID);

    public static final DeferredItem<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BIOMASS = ITEMS.register("biomass",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BIOMASS_PELLET = ITEMS.register("biomass_pellet",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<BiocharItem> BIOCHAR = ITEMS.register("biochar",
            () -> new BiocharItem(new Item.Properties()));

    // Lithium ore chain: mined from Lithium Ore (drops Raw Lithium) or Lithium Brine
    // Extractor (drops Lithium Dust directly). Raw Lithium and Lithium Dust both smelt
    // to Lithium Ingot; crushing Raw Lithium is also the source of Beryllium Dust.
    public static final DeferredItem<Item> RAW_LITHIUM = ITEMS.register("raw_lithium",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LITHIUM_INGOT = ITEMS.register("lithium_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LITHIUM_NUGGET = ITEMS.register("lithium_nugget",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> LITHIUM_DUST = ITEMS.register("lithium_dust",
            () -> new Item(new Item.Properties()));

    // Fusion reactor blanket material - no ore of its own, sourced only as a bonus
    // byproduct of crushing Raw Lithium (and later, the Lithium Brine Extractor).
    public static final DeferredItem<Item> BERYLLIUM_DUST = ITEMS.register("beryllium_dust",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}