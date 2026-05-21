package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {

    private static final TagKey<Fluid> MOLTEN_SALT =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "molten_salt"));
    private static final TagKey<Fluid> MOLTEN =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "molten"));
    private static final TagKey<Fluid> BIOFUEL =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "biofuel"));

    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(FluidTags.LAVA)
                .add(ModFluids.MOLTEN_SALT_SOURCE.get())
                .add(ModFluids.MOLTEN_SALT_FLOWING.get());

        tag(MOLTEN_SALT)
                .add(ModFluids.MOLTEN_SALT_SOURCE.get())
                .add(ModFluids.MOLTEN_SALT_FLOWING.get());
        tag(MOLTEN)
                .add(ModFluids.MOLTEN_SALT_SOURCE.get())
                .add(ModFluids.MOLTEN_SALT_FLOWING.get());

        tag(BIOFUEL)
                .add(ModFluids.BIOFUEL_SOURCE.get())
                .add(ModFluids.BIOFUEL_FLOWING.get());
    }
}