package net.succ.create_solar_powered.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.fluid.ModFluids;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, Create_solar_powered.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FluidTags.LAVA)
                .add(ModFluids.MOLTEN_SALT_SOURCE.get())
                .add(ModFluids.MOLTEN_SALT_FLOWING.get());
    }
}