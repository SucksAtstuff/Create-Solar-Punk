package net.succ.solar_punk.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.util.List;

/**
 * The Solar Engineer: a villager who works at a Crystallizer, which every village solar house
 * has. Its POI is in {@code #minecraft:acquirable_job_site} so unemployed villagers claim it.
 */
public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, SolarPunk.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, SolarPunk.MODID);

    public static final DeferredHolder<PoiType, PoiType> CRYSTALLIZER_POI = POI_TYPES.register("crystallizer",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.CRYSTALLIZER.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> SOLAR_ENGINEER = PROFESSIONS.register("solar_engineer",
            () -> new VillagerProfession("solar_engineer",
                    holder -> holder.is(CRYSTALLIZER_POI.getKey()),
                    holder -> holder.is(CRYSTALLIZER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC));

    public static void register(IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != SOLAR_ENGINEER.get()) return;
        var trades = event.getTrades();

        // Novice: buys plant matter, sells Salt
        add(trades, 1, buy(ModItems.BIOMASS, 24, 16, 2), buy(ModItems.HEMP_STALK, 20, 16, 2),
                sell(1, ModItems.SALT, 8, 12, 1));
        // Apprentice: Biochar and lighting
        add(trades, 2, buy(ModItems.BIOCHAR, 12, 16, 10),
                sell(2, ModItems.HEMP_FIBER, 6, 12, 5), sell(3, ModBlocks.SALT_LAMP, 1, 12, 5));
        // Journeyman: Lithium and solar parts
        add(trades, 3, buy(ModItems.RAW_LITHIUM, 6, 12, 20),
                sell(4, ModItems.PHOTOVOLTAIC_CELL, 1, 12, 10), sell(5, ModBlocks.GARDEN_LIGHT, 2, 12, 10));
        // Expert: working machines
        add(trades, 4, sell(8, ModBlocks.ANDESITE_SOLAR_PANEL, 1, 8, 15),
                sell(10, ModBlocks.GROW_LAMP, 1, 8, 15), sell(6, ModBlocks.SOLAR_GLASS, 4, 8, 15));
        // Master
        add(trades, 5, sell(16, ModBlocks.BRASS_SOLAR_PANEL, 1, 4, 30),
                sell(12, ModBlocks.SOLAR_SHINGLES, 4, 4, 30));
    }

    private static void add(it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
                            int level, VillagerTrades.ItemListing... listings) {
        trades.get(level).addAll(List.of(listings));
    }

    private static VillagerTrades.ItemListing buy(ItemLike item, int count, int maxTrades, int xp) {
        return new BasicItemListing(new ItemStack(item, count), new ItemStack(Items.EMERALD), maxTrades, xp, 0.05f);
    }

    private static VillagerTrades.ItemListing sell(int emeralds, ItemLike item, int count, int maxTrades, int xp) {
        return new BasicItemListing(emeralds, new ItemStack(item, count), maxTrades, xp, 0.05f);
    }
}
