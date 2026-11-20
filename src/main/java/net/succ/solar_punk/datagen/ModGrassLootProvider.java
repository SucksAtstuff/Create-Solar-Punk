package net.succ.solar_punk.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ModItems;

import java.util.function.BiConsumer;

// Extra drops added to vanilla loot tables by ModLootModifierProvider.
public class ModGrassLootProvider implements LootTableSubProvider {

    /** Hemp Seeds from breaking grass, like Wheat Seeds but rarer. */
    public static final ResourceKey<LootTable> HEMP_SEEDS_FROM_GRASS = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "gameplay/hemp_seeds_from_grass"));

    public ModGrassLootProvider(HolderLookup.Provider registries) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(HEMP_SEEDS_FROM_GRASS, LootTable.lootTable().withPool(LootPool.lootPool()
                .when(LootItemRandomChanceCondition.randomChance(0.05f))
                .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)).invert())
                .add(LootItem.lootTableItem(ModItems.HEMP_SEEDS.get()))));
    }
}
