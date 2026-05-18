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

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
