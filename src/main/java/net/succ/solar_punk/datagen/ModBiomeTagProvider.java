package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagProvider extends BiomeTagsProvider {
    public ModBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Biomes.HAS_SALT_DEPOSITS)
                .add(Biomes.DESERT)
                .addTag(BiomeTags.IS_BADLANDS)
                .addTag(BiomeTags.IS_SAVANNA);

        // Green, temperate places with room for a little hut
        tag(ModTags.Biomes.HAS_DIRT_HUT)
                .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW)
                .addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_TAIGA);

        // Open, sunny, green places that suit a solarpunk settlement
        tag(ModTags.Biomes.HAS_ABANDONED_VILLAGE)
                .add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW,
                        Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU);

        // No has_geysers tag: geyser placement biomes come from Config.geyserBiomes at
        // runtime via ConfigurableGeyserBiomeModifier, not a static datapack tag - see
        // that class and ModWorldGenProvider#bootstrapBiomeModifiers.
    }
}