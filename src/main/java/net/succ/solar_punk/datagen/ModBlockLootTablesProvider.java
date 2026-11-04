package net.succ.solar_punk.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.TrellisBlock;
import net.succ.solar_punk.block.custom.TrellisPlant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.succ.solar_punk.item.ModItems;

import java.util.List;
import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    protected ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    // Every block drops itself unless it's listed here. A new block that should drop
    // something else (or nothing) needs an entry, or it will silently drop itself.
    @Override
    protected void generate() {
        add(ModBlocks.LITHIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_LITHIUM.get()));
        add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_LITHIUM.get()));
        add(ModBlocks.DEAD_GRASS.get(), noDrop());
        // Like a Snow Block: needs a shovel, drops itself with Silk Touch, otherwise 4 Ash.
        add(ModBlocks.ASH_BLOCK.get(), block -> createSingleItemTableWithSilkTouch(block, ModItems.ASH.get(), ConstantValue.exactly(4)));
        // Solar mirrors are two cells tall (see SolarMirrorBlock); only the lower half
        // should ever produce a drop, same as vanilla's tall-flower loot tables.
        add(ModBlocks.SOLAR_MIRROR.get(), block ->
                createSinglePropConditionTable(block, SolarMirrorBlock.HALF, DoubleBlockHalf.LOWER));
        // Like vanilla glass and panes: only drop with Silk Touch.
        dropWhenSilkTouch(ModBlocks.SOLAR_GLASS.get());
        dropWhenSilkTouch(ModBlocks.SOLAR_GLASS_PANE.get());
        for (var glass : List.of(ModBlocks.GREENHOUSE_GLASS, ModBlocks.GREENHOUSE_GLASS_PANE, ModBlocks.CLEAR_GREENHOUSE_GLASS,
                ModBlocks.CLEAR_GREENHOUSE_GLASS_PANE, ModBlocks.GREENHOUSE_PANEL, ModBlocks.LARGE_GREENHOUSE_PANEL))
            dropWhenSilkTouch(glass.get());
        // The trellis drops itself plus everything growing on it: a Vine for any plant, and the
        // flower or Glow Berries on top of the vines.
        add(ModBlocks.TRELLIS.get(), block -> {
            LootTable.Builder table = createSingleItemTable(block);
            for (TrellisPlant plant : TrellisPlant.values()) {
                if (plant == TrellisPlant.NONE) continue;
                table.withPool(trellisPlantPool(block, plant, Items.VINE));
                if (plant != TrellisPlant.VINES) table.withPool(trellisPlantPool(block, plant, plant.item()));
            }
            return table;
        });
        // A double slab drops two slabs.
        ModBlockFamilies.ALL.forEach(family -> {
            Block slab = family.get(BlockFamily.Variant.SLAB);
            if (slab != null) add(slab, this::createSlabItemTable);
        });

        for (Block block : getKnownBlocks()) {
            if (!map.containsKey(block.getLootTable())) dropSelf(block);
        }
    }

    private LootPool.Builder trellisPlantPool(Block trellis, TrellisPlant plant, Item drop) {
        return applyExplosionCondition(drop, LootPool.lootPool()
                .add(LootItem.lootTableItem(drop))
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(trellis)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(TrellisBlock.PLANT, plant))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                .filter(b -> !(b instanceof LiquidBlock))
                ::iterator;
    }
}
