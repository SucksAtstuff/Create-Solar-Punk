package net.succ.solar_punk.datagen;

import net.succ.offbyonelib.datagen.AddonBlockLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.HempCropBlock;
import net.succ.solar_punk.block.custom.SolarMirrorBlock;
import net.succ.solar_punk.block.custom.RuinedSolarMirrorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.succ.solar_punk.block.custom.BrokenStreetlampBlock;
import net.succ.solar_punk.block.custom.StreetlampBlock;
import net.succ.solar_punk.block.custom.TrellisBlock;
import net.succ.solar_punk.block.custom.TrellisPlant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import net.succ.solar_punk.item.ModItems;

import java.util.List;

public class ModBlockLootTablesProvider extends AddonBlockLootProvider {
    protected ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(registries, ModBlocks.BLOCKS);
    }

    // Every block drops itself unless it's listed here. A new block that should drop
    // something else (or nothing) needs an entry, or it will silently drop itself.
    @Override
    protected void generateDrops() {
        add(ModBlocks.LITHIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_LITHIUM.get()));
        add(ModBlocks.DEEPSLATE_LITHIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_LITHIUM.get()));
        add(ModBlocks.DEAD_GRASS.get(), noDrop());
        // Like a Snow Block: needs a shovel, drops itself with Silk Touch, otherwise 4 Ash.
        add(ModBlocks.ASH_BLOCK.get(), block -> createSingleItemTableWithSilkTouch(block, ModItems.ASH.get(), ConstantValue.exactly(4)));
        // Solar mirrors are two cells tall (see SolarMirrorBlock); only the lower half
        // should ever produce a drop, same as vanilla's tall-flower loot tables.
        add(ModBlocks.SOLAR_MIRROR.get(), block ->
                createSinglePropConditionTable(block, SolarMirrorBlock.HALF, DoubleBlockHalf.LOWER));
        add(ModBlocks.RUINED_SOLAR_MIRROR.get(), block ->
                createSinglePropConditionTable(block, RuinedSolarMirrorBlock.HALF, DoubleBlockHalf.LOWER));
        add(ModBlocks.STREETLAMP.get(), block ->
                createSinglePropConditionTable(block, StreetlampBlock.HALF, DoubleBlockHalf.LOWER));
        add(ModBlocks.BROKEN_STREETLAMP.get(), block ->
                createSinglePropConditionTable(block, BrokenStreetlampBlock.HALF, DoubleBlockHalf.LOWER));
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
        add(ModBlocks.HEMP.get(), this::hempDrops);
        // A double slab drops two slabs.
        familySlabs(ModBlockFamilies.ALL);
    }

    // Like a wheat crop, but only from the lower half (breaking the top breaks the lower half
    // too, see HempCropBlock), and 2-3 Stalks when grown: a Seed while growing, or Stalks
    // plus Seeds (more with Fortune) once grown.
    private LootTable.Builder hempDrops(Block block) {
        LootItemCondition.Builder lower = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(HempCropBlock.HALF, DoubleBlockHalf.LOWER));
        LootItemCondition.Builder grown = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(HempCropBlock.AGE, HempCropBlock.MAX_AGE)
                        .hasProperty(HempCropBlock.HALF, DoubleBlockHalf.LOWER));
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return applyExplosionDecay(block, LootTable.lootTable()
                .withPool(LootPool.lootPool().when(lower)
                        .add(LootItem.lootTableItem(ModItems.HEMP_STALK.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 3)))
                                .when(grown)
                                .otherwise(LootItem.lootTableItem(ModItems.HEMP_SEEDS.get()))))
                .withPool(LootPool.lootPool().when(grown)
                        .add(LootItem.lootTableItem(ModItems.HEMP_SEEDS.get())
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(
                                        enchantments.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3)))));
    }

    private LootPool.Builder trellisPlantPool(Block trellis, TrellisPlant plant, Item drop) {
        return applyExplosionCondition(drop, LootPool.lootPool()
                .add(LootItem.lootTableItem(drop))
                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(trellis)
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(TrellisBlock.PLANT, plant))));
    }
}
