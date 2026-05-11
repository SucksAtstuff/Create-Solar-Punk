package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server providers
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)
        ), lookupProvider));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
        ModBlockTagProvider blockTagProvider = generator.addProvider(event.includeServer(),
                new ModBlockTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModItemTagProvider(output, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ModFluidTagsProvider(output, lookupProvider, existingFileHelper));
        ModWorldGenProvider worldGenProvider = generator.addProvider(event.includeServer(),
                new ModWorldGenProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModBiomeTagProvider(output, worldGenProvider.getRegistryProvider(), existingFileHelper));

        // Client providers
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLangProvider(output));
    }
}
