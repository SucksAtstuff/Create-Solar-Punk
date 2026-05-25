package net.succ.solar_punk;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // -------------------------------------------------------------------------
    // Generators
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_ANDESITE_MORNING_RPM;
    private static final ModConfigSpec.IntValue CFG_ANDESITE_NOON_RPM;
    private static final ModConfigSpec.IntValue CFG_ANDESITE_MORNING_SU;
    private static final ModConfigSpec.IntValue CFG_ANDESITE_NOON_SU;

    private static final ModConfigSpec.IntValue CFG_BRASS_MORNING_FE;
    private static final ModConfigSpec.IntValue CFG_BRASS_NOON_FE;
    private static final ModConfigSpec.IntValue CFG_BRASS_BUFFER;
    private static final ModConfigSpec.IntValue CFG_BRASS_MAX_EXTRACT;

    private static final ModConfigSpec.IntValue CFG_GEYSER_RPM;
    private static final ModConfigSpec.IntValue CFG_GEYSER_SU;

    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_RPM;
    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_SU;
    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_MAX_CHARGE;
    private static final ModConfigSpec.DoubleValue CFG_KINETIC_BATTERY_CHARGE_RATE;
    private static final ModConfigSpec.DoubleValue CFG_KINETIC_BATTERY_DISCHARGE_RATE;

    private static final ModConfigSpec.IntValue CFG_GASIFIER_RPM;
    private static final ModConfigSpec.IntValue CFG_GASIFIER_SU;

    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_RPM;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_SU;

    // -------------------------------------------------------------------------
    // Machines — speed and output amounts
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_MELT_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_EVAPORATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_WATER_PER_SALT;

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_WATER_PER_BATCH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_BIOFUEL_PER_BATCH;

    private static final ModConfigSpec.IntValue CFG_GASIFIER_BURN_TICKS;

    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_MB;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_PERIOD;

    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_MAX_HEAT;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_PER_MB;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_DECAY;

    private static final ModConfigSpec.IntValue CFG_SPRINKLER_FLUID_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_RANGE;

    // -------------------------------------------------------------------------
    // Tanks
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_TANK;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_TANK;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_TANK_PER_BLOCK;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_TANK;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_TANK;

    // -------------------------------------------------------------------------
    // Multiblock minimum sizes
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_HEIGHT;

    // -------------------------------------------------------------------------
    // World gen — geysers
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_GEYSER_SPAWN_CHANCE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CFG_GEYSER_BIOMES;

    static {
        BUILDER.push("generators");

        BUILDER.push("andesite_solar_panel");
        CFG_ANDESITE_MORNING_RPM = BUILDER.comment("RPM output at dawn and dusk").defineInRange("morning_rpm", 8, 1, 256);
        CFG_ANDESITE_NOON_RPM    = BUILDER.comment("RPM output at noon (clear weather)").defineInRange("noon_rpm", 16, 1, 256);
        CFG_ANDESITE_MORNING_SU  = BUILDER.comment("Stress capacity at dawn and dusk").defineInRange("morning_su", 128, 1, 1_000_000);
        CFG_ANDESITE_NOON_SU     = BUILDER.comment("Stress capacity at noon (clear weather)").defineInRange("noon_su", 256, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("brass_solar_panel");
        CFG_BRASS_MORNING_FE  = BUILDER.comment("FE/t generated at dawn and dusk").defineInRange("morning_fe_per_tick", 40, 1, 100_000);
        CFG_BRASS_NOON_FE     = BUILDER.comment("FE/t generated at noon (clear weather)").defineInRange("noon_fe_per_tick", 80, 1, 100_000);
        CFG_BRASS_BUFFER      = BUILDER.comment("Internal FE buffer capacity").defineInRange("buffer_capacity", 100_000, 1000, 10_000_000);
        CFG_BRASS_MAX_EXTRACT = BUILDER.comment("Max FE extracted per tick by adjacent cables").defineInRange("max_extract", 80, 1, 100_000);
        BUILDER.pop();

        BUILDER.push("geyser_cap");
        CFG_GEYSER_RPM = BUILDER.comment("RPM output").defineInRange("rpm", 32, 1, 256);
        CFG_GEYSER_SU  = BUILDER.comment("Stress capacity").defineInRange("su", 512, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("kinetic_battery");
        CFG_KINETIC_BATTERY_RPM            = BUILDER.comment("RPM output while discharging").defineInRange("rpm", 16, 1, 256);
        CFG_KINETIC_BATTERY_SU             = BUILDER.comment("Stress capacity while discharging").defineInRange("su", 16, 1, 1_000_000);
        CFG_KINETIC_BATTERY_MAX_CHARGE     = BUILDER.comment("Maximum stored charge").defineInRange("max_charge", 1200, 100, 1_000_000);
        CFG_KINETIC_BATTERY_CHARGE_RATE    = BUILDER.comment("Charge gained per tick while a spinning network is connected").defineInRange("charge_rate", 1.0, 0.01, 100.0);
        CFG_KINETIC_BATTERY_DISCHARGE_RATE = BUILDER.comment("Charge lost per tick while discharging").defineInRange("discharge_rate", 0.5, 0.01, 100.0);
        BUILDER.pop();

        BUILDER.push("biomass_gasifier");
        CFG_GASIFIER_RPM = BUILDER.comment("RPM output while burning").defineInRange("rpm", 8, 1, 256);
        CFG_GASIFIER_SU  = BUILDER.comment("Stress capacity while burning").defineInRange("su", 256, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("biofuel_engine");
        CFG_BIOFUEL_ENGINE_RPM = BUILDER.comment("RPM output while running").defineInRange("rpm", 16, 1, 256);
        CFG_BIOFUEL_ENGINE_SU  = BUILDER.comment("Stress capacity while running").defineInRange("su", 512, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.pop(); // generators

        BUILDER.push("machines");

        BUILDER.push("solar_heater");
        CFG_SOLAR_HEATER_MELT_TICKS       = BUILDER.comment("Ticks to melt one item").defineInRange("melt_ticks", 200, 1, 100_000);
        CFG_SOLAR_HEATER_EVAPORATION_TICKS = BUILDER.comment("Ticks to evaporate enough water to produce one salt").defineInRange("evaporation_ticks", 200, 1, 100_000);
        CFG_SOLAR_HEATER_WATER_PER_SALT   = BUILDER.comment("mB of water consumed per salt produced").defineInRange("water_per_salt_mb", 250, 1, 10_000);
        BUILDER.pop();

        BUILDER.push("fermentation_vat");
        CFG_FERMENTATION_TICKS            = BUILDER.comment("Ticks per fermentation batch").defineInRange("fermentation_ticks", 400, 1, 100_000);
        CFG_FERMENTATION_WATER_PER_BATCH  = BUILDER.comment("mB of water consumed per batch (per footprint block)").defineInRange("water_per_batch_mb", 1000, 1, 100_000);
        CFG_FERMENTATION_BIOFUEL_PER_BATCH = BUILDER.comment("mB of biofuel produced per batch (per footprint block)").defineInRange("biofuel_per_batch_mb", 1000, 1, 100_000);
        BUILDER.pop();

        BUILDER.push("biomass_gasifier");
        CFG_GASIFIER_BURN_TICKS = BUILDER.comment("Ticks one fuel item burns for").defineInRange("burn_ticks", 300, 1, 100_000);
        BUILDER.pop();

        BUILDER.push("biofuel_engine");
        CFG_BIOFUEL_CONSUME_MB     = BUILDER.comment("mB of biofuel consumed per consumption cycle").defineInRange("consume_mb", 50, 1, 10_000);
        CFG_BIOFUEL_CONSUME_PERIOD = BUILDER.comment("Ticks between each fuel consumption").defineInRange("consume_period_ticks", 40, 1, 10_000);
        BUILDER.pop();

        BUILDER.push("heat_battery");
        CFG_HEAT_BATTERY_MAX_HEAT   = BUILDER.comment("Maximum heat the battery can store").defineInRange("max_heat", 80_000, 1000, 10_000_000);
        CFG_HEAT_BATTERY_HEAT_PER_MB = BUILDER.comment("Heat units gained per mB of molten salt consumed").defineInRange("heat_per_mb", 10, 1, 10_000);
        CFG_HEAT_BATTERY_HEAT_DECAY  = BUILDER.comment("Heat units lost per tick (passive dissipation)").defineInRange("heat_decay_per_tick", 2, 0, 10_000);
        BUILDER.pop();

        BUILDER.push("kinetic_sprinkler");
        CFG_SPRINKLER_FLUID_PER_CYCLE = BUILDER.comment("mB of fluid consumed per cycle").defineInRange("fluid_per_cycle_mb", 100, 1, 10_000);
        CFG_SPRINKLER_RANGE           = BUILDER.comment("Horizontal radius (blocks) of the watering area").defineInRange("range", 2, 1, 16);
        BUILDER.pop();

        BUILDER.pop(); // machines

        BUILDER.push("tanks");
        CFG_HEAT_BATTERY_TANK               = BUILDER.comment("Heat Battery: molten salt tank capacity (mB)").defineInRange("heat_battery_mb", 8000, 100, 1_000_000);
        CFG_BIOFUEL_ENGINE_TANK             = BUILDER.comment("Biofuel Engine: fuel tank capacity (mB)").defineInRange("biofuel_engine_mb", 8000, 100, 1_000_000);
        CFG_FERMENTATION_VAT_TANK_PER_BLOCK = BUILDER.comment("Fermentation Vat: fluid capacity per multiblock block (mB)").defineInRange("fermentation_vat_per_block_mb", 8000, 100, 1_000_000);
        CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK = BUILDER.comment("Solar Power Tower: fluid capacity per multiblock block (mB)").defineInRange("solar_power_tower_per_block_mb", 8000, 100, 1_000_000);
        CFG_SPRINKLER_TANK                  = BUILDER.comment("Kinetic Sprinkler: fluid tank capacity (mB)").defineInRange("kinetic_sprinkler_mb", 4000, 100, 1_000_000);
        CFG_SOLAR_HEATER_TANK               = BUILDER.comment("Solar Heater: output fluid tank capacity (mB)").defineInRange("solar_heater_mb", 8000, 100, 1_000_000);
        BUILDER.pop();

        BUILDER.push("multiblocks");
        CFG_FERMENTATION_VAT_MIN_WIDTH    = BUILDER.comment("Fermentation Vat: minimum footprint width (2 = 2x2, 3 = 3x3) needed to ferment").defineInRange("fermentation_vat_min_width", 2, 2, 3);
        CFG_SOLAR_POWER_TOWER_MIN_WIDTH   = BUILDER.comment("Solar Power Tower: minimum footprint width needed to produce Molten Salt").defineInRange("solar_power_tower_min_width", 3, 1, 3);
        CFG_SOLAR_POWER_TOWER_MIN_HEIGHT  = BUILDER.comment("Solar Power Tower: minimum block height needed to produce Molten Salt").defineInRange("solar_power_tower_min_height", 3, 1, 20);
        BUILDER.pop();

        BUILDER.push("world_gen");
        CFG_GEYSER_SPAWN_CHANCE = BUILDER.comment("Per-attempt spawn chance for Geyser Vents (0 = never, 100 = always)").defineInRange("geyser_spawn_chance", 100, 0, 100);
        CFG_GEYSER_BIOMES       = BUILDER.comment("Biomes where Geyser Vents can spawn (resource location format, e.g. minecraft:desert)")
                .defineListAllowEmpty("geyser_biomes",
                        () -> new ArrayList<>(List.of("minecraft:desert", "minecraft:badlands", "minecraft:eroded_badlands",
                                "minecraft:wooded_badlands", "minecraft:savanna", "minecraft:savanna_plateau",
                                "minecraft:windswept_savanna")),
                        e -> e instanceof String s && s.contains(":"));
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    // -------------------------------------------------------------------------
    // Cached public values (populated in onLoad)
    // -------------------------------------------------------------------------

    public static int andesiteMorningRpm, andesiteNoonRpm, andesiteMorningSu, andesiteNoonSu;
    public static int brassMorningFe, brassNoonFe, brassBuffer, brassMaxExtract;
    public static int geyserCapRpm, geyserCapSu;
    public static int kineticBatteryRpm, kineticBatterySu, kineticBatteryMaxCharge;
    public static double kineticBatteryChargeRate, kineticBatteryDischargeRate;
    public static int gasifierRpm, gasifierSu;
    public static int biofuelEngineRpm, biofuelEngineSu;

    public static int solarHeaterMeltTicks, solarHeaterEvaporationTicks, solarHeaterWaterPerSalt;
    public static int fermentationTicks, fermentationWaterPerBatch, fermentationBiofuelPerBatch;
    public static int gasifierBurnTicks;
    public static int biofuelConsumeMb, biofuelConsumePeriod;
    public static int heatBatteryMaxHeat, heatBatteryHeatPerMb, heatBatteryHeatDecay;
    public static int sprinklerFluidPerCycle, sprinklerRange;

    public static int heatBatteryTank, biofuelEngineTank;
    public static int fermentationVatTankPerBlock, solarPowerTowerTankPerBlock;
    public static int sprinklerTank, solarHeaterTank;

    public static int fermentationVatMinWidth;
    public static int solarPowerTowerMinWidth, solarPowerTowerMinHeight;

    public static int geyserSpawnChance;
    public static List<? extends String> geyserBiomes;

    static void onLoad(final ModConfigEvent event) {
        andesiteMorningRpm   = CFG_ANDESITE_MORNING_RPM.get();
        andesiteNoonRpm      = CFG_ANDESITE_NOON_RPM.get();
        andesiteMorningSu    = CFG_ANDESITE_MORNING_SU.get();
        andesiteNoonSu       = CFG_ANDESITE_NOON_SU.get();

        brassMorningFe       = CFG_BRASS_MORNING_FE.get();
        brassNoonFe          = CFG_BRASS_NOON_FE.get();
        brassBuffer          = CFG_BRASS_BUFFER.get();
        brassMaxExtract      = CFG_BRASS_MAX_EXTRACT.get();

        geyserCapRpm         = CFG_GEYSER_RPM.get();
        geyserCapSu          = CFG_GEYSER_SU.get();

        kineticBatteryRpm          = CFG_KINETIC_BATTERY_RPM.get();
        kineticBatterySu           = CFG_KINETIC_BATTERY_SU.get();
        kineticBatteryMaxCharge    = CFG_KINETIC_BATTERY_MAX_CHARGE.get();
        kineticBatteryChargeRate   = CFG_KINETIC_BATTERY_CHARGE_RATE.get();
        kineticBatteryDischargeRate = CFG_KINETIC_BATTERY_DISCHARGE_RATE.get();

        gasifierRpm          = CFG_GASIFIER_RPM.get();
        gasifierSu           = CFG_GASIFIER_SU.get();

        biofuelEngineRpm     = CFG_BIOFUEL_ENGINE_RPM.get();
        biofuelEngineSu      = CFG_BIOFUEL_ENGINE_SU.get();

        solarHeaterMeltTicks       = CFG_SOLAR_HEATER_MELT_TICKS.get();
        solarHeaterEvaporationTicks = CFG_SOLAR_HEATER_EVAPORATION_TICKS.get();
        solarHeaterWaterPerSalt    = CFG_SOLAR_HEATER_WATER_PER_SALT.get();

        fermentationTicks          = CFG_FERMENTATION_TICKS.get();
        fermentationWaterPerBatch  = CFG_FERMENTATION_WATER_PER_BATCH.get();
        fermentationBiofuelPerBatch = CFG_FERMENTATION_BIOFUEL_PER_BATCH.get();

        gasifierBurnTicks    = CFG_GASIFIER_BURN_TICKS.get();

        biofuelConsumeMb     = CFG_BIOFUEL_CONSUME_MB.get();
        biofuelConsumePeriod = CFG_BIOFUEL_CONSUME_PERIOD.get();

        heatBatteryMaxHeat   = CFG_HEAT_BATTERY_MAX_HEAT.get();
        heatBatteryHeatPerMb = CFG_HEAT_BATTERY_HEAT_PER_MB.get();
        heatBatteryHeatDecay = CFG_HEAT_BATTERY_HEAT_DECAY.get();

        sprinklerFluidPerCycle = CFG_SPRINKLER_FLUID_PER_CYCLE.get();
        sprinklerRange         = CFG_SPRINKLER_RANGE.get();

        heatBatteryTank              = CFG_HEAT_BATTERY_TANK.get();
        biofuelEngineTank            = CFG_BIOFUEL_ENGINE_TANK.get();
        fermentationVatTankPerBlock  = CFG_FERMENTATION_VAT_TANK_PER_BLOCK.get();
        solarPowerTowerTankPerBlock  = CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK.get();
        sprinklerTank                = CFG_SPRINKLER_TANK.get();
        solarHeaterTank              = CFG_SOLAR_HEATER_TANK.get();

        fermentationVatMinWidth     = CFG_FERMENTATION_VAT_MIN_WIDTH.get();
        solarPowerTowerMinWidth     = CFG_SOLAR_POWER_TOWER_MIN_WIDTH.get();
        solarPowerTowerMinHeight    = CFG_SOLAR_POWER_TOWER_MIN_HEIGHT.get();

        geyserSpawnChance = CFG_GEYSER_SPAWN_CHANCE.get();
        geyserBiomes      = CFG_GEYSER_BIOMES.get();
    }
}
