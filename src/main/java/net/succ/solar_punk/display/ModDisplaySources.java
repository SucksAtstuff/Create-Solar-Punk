package net.succ.solar_punk.display;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, SolarPunk.MODID);

    public static final DeferredHolder<DisplaySource, HeatBatteryChargeDisplaySource> HEAT_BATTERY_CHARGE =
            DISPLAY_SOURCES.register("heat_battery_charge", HeatBatteryChargeDisplaySource::new);

    public static final DeferredHolder<DisplaySource, HeatBatteryStatusDisplaySource> HEAT_BATTERY_STATUS =
            DISPLAY_SOURCES.register("heat_battery_status", HeatBatteryStatusDisplaySource::new);

    public static final DeferredHolder<DisplaySource, KineticBatteryChargeDisplaySource> KINETIC_BATTERY_CHARGE =
            DISPLAY_SOURCES.register("kinetic_battery_charge", KineticBatteryChargeDisplaySource::new);

    public static final DeferredHolder<DisplaySource, SolarPowerTowerEfficiencyDisplaySource> SOLAR_POWER_TOWER_EFFICIENCY =
            DISPLAY_SOURCES.register("solar_power_tower_efficiency", SolarPowerTowerEfficiencyDisplaySource::new);

    public static final DeferredHolder<DisplaySource, SolarPowerTowerMirrorCountDisplaySource> SOLAR_POWER_TOWER_MIRROR_COUNT =
            DISPLAY_SOURCES.register("solar_power_tower_mirror_count", SolarPowerTowerMirrorCountDisplaySource::new);

    public static final DeferredHolder<DisplaySource, SolarPowerTowerGenerationRateDisplaySource> SOLAR_POWER_TOWER_GENERATION_RATE =
            DISPLAY_SOURCES.register("solar_power_tower_generation_rate", SolarPowerTowerGenerationRateDisplaySource::new);

    public static final DeferredHolder<DisplaySource, FusionReactorLithiumBufferDisplaySource> FUSION_REACTOR_LITHIUM_BUFFER =
            DISPLAY_SOURCES.register("fusion_reactor_lithium_buffer", FusionReactorLithiumBufferDisplaySource::new);

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}
