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
    private static final TagKey<Fluid> FERTILIZER =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "fertilizer"));
    private static final TagKey<Fluid> STEAM =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "steam"));
    private static final TagKey<Fluid> HEMP_SEED_OIL =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "hemp_seed_oil"));
    // Northstar's Oxygen Sealer and Oxygen Filler take anything in #northstar:breathable, which
    // includes #c:oxygen, so our Oxygen works as life support there
    private static final TagKey<Fluid> OXYGEN =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "oxygen"));
    // Cosmonautics' #rocketnautics:rocket_fuel includes #c:hydrogen
    private static final TagKey<Fluid> HYDROGEN =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "hydrogen"));
    // Cosmonautics rocket oxidizer (Water by default)
    private static final TagKey<Fluid> ROCKET_OXIDIZER =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("rocketnautics", "oxidizer"));

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

        tag(FERTILIZER)
                .add(ModFluids.FERTILIZER_SOURCE.get())
                .add(ModFluids.FERTILIZER_FLOWING.get());

        tag(STEAM)
                .add(ModFluids.STEAM_SOURCE.get())
                .add(ModFluids.STEAM_FLOWING.get());

        tag(HEMP_SEED_OIL)
                .add(ModFluids.HEMP_SEED_OIL_SOURCE.get())
                .add(ModFluids.HEMP_SEED_OIL_FLOWING.get());

        tag(OXYGEN)
                .add(ModFluids.OXYGEN_SOURCE.get())
                .add(ModFluids.OXYGEN_FLOWING.get());
        tag(HYDROGEN)
                .add(ModFluids.HYDROGEN_SOURCE.get())
                .add(ModFluids.HYDROGEN_FLOWING.get());
        tag(ROCKET_OXIDIZER).addTag(OXYGEN);
    }
}