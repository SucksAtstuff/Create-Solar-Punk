package net.succ.create_solar_powered.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Create_solar_powered.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ANDESITE_SOLAR_PANEL.get())
                .add(ModBlocks.BRASS_SOLAR_PANEL.get())
                .add(ModBlocks.SOLAR_HEATER.get())
                .add(ModBlocks.SALT_BLOCK.get())
                .add(ModBlocks.KINETIC_BATTERY.get())
                .add(ModBlocks.HEAT_BATTERY.get());

    }
}
