package net.succ.create_solar_powered.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.succ.create_solar_powered.block.ModBlocks;

import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    protected ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.ANDESITE_SOLAR_PANEL.get());
        dropSelf(ModBlocks.BRASS_SOLAR_PANEL.get());
        dropSelf(ModBlocks.SOLAR_HEATER.get());
        dropSelf(ModBlocks.SALT_BLOCK.get());
        dropSelf(ModBlocks.KINETIC_BATTERY.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                .filter(b -> !(b instanceof LiquidBlock))
                ::iterator;
    }
}
