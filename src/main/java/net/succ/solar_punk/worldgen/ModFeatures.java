package net.succ.solar_punk.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.worldgen.feature.GeyserFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, SolarPunk.MODID);

    public static final DeferredHolder<Feature<?>, GeyserFeature> GEYSER_BLOB =
            FEATURES.register("geyser_blob", () -> new GeyserFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
