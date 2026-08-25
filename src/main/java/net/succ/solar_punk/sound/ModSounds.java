package net.succ.solar_punk.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, SolarPunk.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ALTERNATOR =
            SOUND_EVENTS.register("alternator",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "alternator")));

    public static final DeferredHolder<SoundEvent, SoundEvent> GEYSER_PUFF =
            SOUND_EVENTS.register("geyser_puff",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geyser_puff")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BIOFUEL_ENGINE_LOOP =
            SOUND_EVENTS.register("biofuel_engine_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "biofuel_engine_loop")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BIOFILTER_LOOP =
            SOUND_EVENTS.register("biofilter_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "biofilter_loop")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BIOMASS_GASIFIER_LOOP =
            SOUND_EVENTS.register("biomass_gasifier_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "biomass_gasifier_loop")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FERMENTATION_VAT_BUBBLING =
            SOUND_EVENTS.register("fermentation_vat_bubbling",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "fermentation_vat_bubbling")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SOLAR_HEATER_SHIMMER =
            SOUND_EVENTS.register("solar_heater_shimmer",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_heater_shimmer")));

    // One-shot quench hiss, played once per crystallization cycle (see
    // CrystallizerBlockEntity#tick) rather than looped - not an ambient machine hum.
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM_HISS =
            SOUND_EVENTS.register("steam_hiss",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "steam_hiss")));

    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LOOP =
            SOUND_EVENTS.register("turbine_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "turbine_loop")));

    // Fusion Reactor Core ambience - see FusionReactorCoreBlockEntity#tickAudio for the
    // startup -> loop -> shutdown sequencing that picks between these three.
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_REACTOR_STARTUP =
            SOUND_EVENTS.register("fusion_reactor_startup",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "fusion_reactor_startup")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_REACTOR_SHUTDOWN =
            SOUND_EVENTS.register("fusion_reactor_shutdown",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "fusion_reactor_shutdown")));

    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_ON_LOOP =
            SOUND_EVENTS.register("reactor_on_loop",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "reactor_on_loop")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
