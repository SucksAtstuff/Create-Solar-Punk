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

    public static final DeferredHolder<SoundEvent, SoundEvent> ALTERNATOR = register("alternator");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEYSER_PUFF = register("geyser_puff");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIOFUEL_ENGINE_LOOP = register("biofuel_engine_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIOFILTER_LOOP = register("biofilter_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BIOMASS_GASIFIER_LOOP = register("biomass_gasifier_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> FERMENTATION_VAT_BUBBLING = register("fermentation_vat_bubbling");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLAR_HEATER_SHIMMER = register("solar_heater_shimmer");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LOOP = register("turbine_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> PARTICLE_ACCELERATOR_HUM = register("particle_accelerator_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> PARTICLE_COLLISION = register("particle_collision");

    // One-shot quench hiss, played once per Crystallizer cycle.
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM_HISS = register("steam_hiss");

    // Fusion Reactor Core ambience, sequenced by FusionReactorCoreBlockEntity#tickAudio.
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_REACTOR_STARTUP = register("fusion_reactor_startup");
    public static final DeferredHolder<SoundEvent, SoundEvent> FUSION_REACTOR_SHUTDOWN = register("fusion_reactor_shutdown");
    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_ON_LOOP = register("reactor_on_loop");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
