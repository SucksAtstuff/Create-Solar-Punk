package net.succ.solar_punk.display;

import net.succ.solar_punk.block.entity.custom.ParticleAcceleratorInputBlockEntity;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.block.entity.custom.KineticBatteryBlockEntity;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;

// Display Link sources. The IDs are saved on placed Display Links, so don't rename them.
public class ModDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, SolarPunk.MODID);

    // --- Heat Battery ---

    public static final DeferredHolder<DisplaySource, DisplaySource> HEAT_BATTERY_CHARGE = register("heat_battery_charge",
            () -> new PercentDisplaySource<>(ModDisplaySources::heatBattery,
                    be -> be.getHeatStored() / (float) Config.heatBatteryMaxHeat));

    public static final DeferredHolder<DisplaySource, DisplaySource> HEAT_BATTERY_STATUS = register("heat_battery_status",
            HeatBatteryStatusDisplaySource::new);

    // --- Kinetic Battery ---

    public static final DeferredHolder<DisplaySource, DisplaySource> KINETIC_BATTERY_CHARGE = register("kinetic_battery_charge",
            () -> new PercentDisplaySource<>(ModDisplaySources::kineticBattery,
                    be -> be.getChargeLevel() / (float) Config.kineticBatteryMaxCharge));

    // --- Solar Power Tower ---

    public static final DeferredHolder<DisplaySource, DisplaySource> SOLAR_POWER_TOWER_EFFICIENCY = register("solar_power_tower_efficiency",
            () -> new PercentDisplaySource<>(ModDisplaySources::towerController, SolarPowerTowerBlockEntity::getEfficiency));

    public static final DeferredHolder<DisplaySource, DisplaySource> SOLAR_POWER_TOWER_MIRROR_COUNT = register("solar_power_tower_mirror_count",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::towerController,
                    tower -> Component.literal(String.valueOf(tower.getMirrorCount())).withStyle(ChatFormatting.YELLOW)));

    public static final DeferredHolder<DisplaySource, DisplaySource> SOLAR_POWER_TOWER_GENERATION_RATE = register("solar_power_tower_generation_rate",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::towerController,
                    tower -> Component.literal(String.format(Locale.ROOT, "%.1f mB/s", tower.getGenerationRate() * 20f))
                            .withStyle(tower.steamMode ? ChatFormatting.WHITE : ChatFormatting.GOLD)));

    // --- Fusion Reactor ---

    // Named "lithium_buffer" for old Display Links, but shows the whole Lithium supply:
    // the buffer alone is at most a few hundred mB and reads as empty even with a full
    // stack of Lithium Dust waiting in the slot.
    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_LITHIUM_BUFFER = register("fusion_reactor_lithium_buffer",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal((int) core.getLithiumSupplyMb() + " mB").withStyle(ChatFormatting.AQUA)));

    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_STATUS = register("fusion_reactor_status",
            FusionReactorStatusDisplaySource::new);

    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_STEAM_OUTPUT = register("fusion_reactor_steam_output",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal(core.steamOutputPerTick * 20 + " mB/s").withStyle(ChatFormatting.WHITE)));

    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_THROTTLE = register("fusion_reactor_throttle",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal(core.getThrottlePercent() + "%").withStyle(ChatFormatting.RED)));

    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_DEUTERIUM = register("fusion_reactor_deuterium",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal(core.deuteriumTank.getFluidAmount() + " / " + core.deuteriumTank.getCapacity() + " mB")
                            .withStyle(ChatFormatting.AQUA)));

    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_STEAM_TANK = register("fusion_reactor_steam_tank",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal(Math.round(core.steamTank.getFluidAmount() * 100f / core.steamTank.getCapacity()) + "%")
                            .withStyle(ChatFormatting.WHITE)));

    // Tritium Capsules waiting in the reactor, as mB of fuel (like the Lithium supply)
    public static final DeferredHolder<DisplaySource, DisplaySource> FUSION_REACTOR_TRITIUM_SUPPLY = register("fusion_reactor_tritium_supply",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::reactorCore,
                    core -> Component.literal((int) core.getTritiumSupplyMb() + " mB").withStyle(ChatFormatting.GREEN)));

    // --- Particle Accelerator ---

    public static final DeferredHolder<DisplaySource, DisplaySource> PARTICLE_ACCELERATOR_STATUS = register("particle_accelerator_status",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::acceleratorInput,
                    ParticleAcceleratorInputBlockEntity::getStatusLine));

    public static final DeferredHolder<DisplaySource, DisplaySource> PARTICLE_ACCELERATOR_RECIPE = register("particle_accelerator_recipe",
            () -> new NumericLineDisplaySource<>(ModDisplaySources::acceleratorInput,
                    input -> input.getRecipeResult().isEmpty()
                            ? Component.literal("None").withStyle(ChatFormatting.GRAY)
                            : input.getRecipeResult().getHoverName().copy().withStyle(ChatFormatting.GOLD)));

    public static final DeferredHolder<DisplaySource, DisplaySource> PARTICLE_ACCELERATOR_PROGRESS = register("particle_accelerator_progress",
            () -> new PercentDisplaySource<>(ModDisplaySources::acceleratorInput,
                    ParticleAcceleratorInputBlockEntity::getProgressFraction));

    private static DeferredHolder<DisplaySource, DisplaySource> register(String name, Supplier<DisplaySource> source) {
        return DISPLAY_SOURCES.register(name, source);
    }

    // --- Block entity lookups ---

    @Nullable
    private static HeatBatteryBlockEntity heatBattery(DisplayLinkContext context) {
        return context.getSourceBlockEntity() instanceof HeatBatteryBlockEntity be ? be : null;
    }

    @Nullable
    private static KineticBatteryBlockEntity kineticBattery(DisplayLinkContext context) {
        return context.getSourceBlockEntity() instanceof KineticBatteryBlockEntity be ? be : null;
    }

    @Nullable
    private static SolarPowerTowerBlockEntity towerController(DisplayLinkContext context) {
        return context.getSourceBlockEntity() instanceof SolarPowerTowerBlockEntity tower ? tower.getControllerBE() : null;
    }

    @Nullable
    private static ParticleAcceleratorInputBlockEntity acceleratorInput(DisplayLinkContext context) {
        return context.getSourceBlockEntity() instanceof ParticleAcceleratorInputBlockEntity be ? be : null;
    }

    // The Core is buried once the reactor is built, so these sources are linked to the
    // Casing shell and reach the Core through the Casing's proxy.
    @Nullable
    static FusionReactorCoreBlockEntity reactorCore(DisplayLinkContext context) {
        if (context.getSourceBlockEntity() instanceof FusionReactorCasingBlockEntity casing)
            return casing.core();
        if (context.getSourceBlockEntity() instanceof FusionReactorCoreBlockEntity core)
            return core;
        return null;
    }

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}
