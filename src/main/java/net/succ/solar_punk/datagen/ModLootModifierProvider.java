package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.succ.solar_punk.SolarPunk;

import java.util.concurrent.CompletableFuture;

public class ModLootModifierProvider extends GlobalLootModifierProvider {
    public ModLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SolarPunk.MODID);
    }

    @Override
    protected void start() {
        // Hemp's only natural source, so it can be found without trading or other mods.
        add("hemp_seeds_from_grass", new AddTableLootModifier(new LootItemCondition[]{
                AnyOfCondition.anyOf(
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("blocks/short_grass")),
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("blocks/tall_grass"))
                ).build()
        }, ModGrassLootProvider.HEMP_SEEDS_FROM_GRASS));
    }
}
