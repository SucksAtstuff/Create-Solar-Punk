package net.succ.solar_punk;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("solarpunk/config");
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // "modid:path" - same character set Minecraft's ResourceLocation allows.
    private static final Pattern MODID_PATH = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
    // "modid:path=amount" as used by per_block_pollution.
    private static final Pattern MODID_PATH_EQUALS_AMOUNT = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+=\\d+$");

    /**
     * Builds a validator for a list config entry that must look like "modid:path".
     * Logs a warning naming the offending entry instead of letting it fail silently -
     * a malformed entry (missing colon, stray quote, wrong case, etc.) is dropped from
     * the list rather than reverting the whole config to defaults.
     */
    private static Predicate<Object> modIdPathValidator(String configKey) {
        return value -> {
            if (!(value instanceof String s)) {
                LOGGER.warn("Config '{}': entry {} is not a string, ignoring it.", configKey, value);
                return false;
            }
            if (!MODID_PATH.matcher(s).matches()) {
                LOGGER.warn("Config '{}': entry \"{}\" is not a valid \"modid:path\" block ID, ignoring it. " +
                        "Check for typos, a missing mod-id prefix, or a stray/missing quote in the TOML file.",
                        configKey, s);
                return false;
            }
            return true;
        };
    }

    /**
     * Same as {@link #modIdPathValidator(String)} but for "modid:path=amount" entries
     * (per_block_pollution).
     */
    private static Predicate<Object> modIdPathAmountValidator(String configKey) {
        return value -> {
            if (!(value instanceof String s)) {
                LOGGER.warn("Config '{}': entry {} is not a string, ignoring it.", configKey, value);
                return false;
            }
            if (!MODID_PATH_EQUALS_AMOUNT.matcher(s).matches()) {
                LOGGER.warn("Config '{}': entry \"{}\" is not valid \"modid:path=amount\" format, ignoring it. " +
                        "Check for typos, a missing mod-id prefix, or a stray/missing quote in the TOML file.",
                        configKey, s);
                return false;
            }
            return true;
        };
    }

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

    private static final ModConfigSpec.IntValue CFG_TURBINE_MAX_RPM;
    private static final ModConfigSpec.IntValue CFG_TURBINE_RPM_PER_LAYER;
    private static final ModConfigSpec.IntValue CFG_TURBINE_SU_PER_LAYER;
    private static final ModConfigSpec.IntValue CFG_TURBINE_STEAM_PER_LAYER_PER_TICK;
    private static final ModConfigSpec.IntValue CFG_TURBINE_CONDENSATE_RATIO;

    // -------------------------------------------------------------------------
    // Machines — speed and output amounts
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_MELT_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_EVAPORATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_WATER_PER_SALT;

    private static final ModConfigSpec.IntValue CFG_FIREBOX_BOILER_STEAM_PER_TICK;

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_WATER_PER_BATCH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_BIOFUEL_PER_BATCH;

    private static final ModConfigSpec.IntValue CFG_GASIFIER_BURN_TICKS;
    private static final ModConfigSpec.IntValue CFG_PELLET_BURN_TICKS;
    private static final ModConfigSpec.IntValue CFG_PELLET_BIOCHAR_AMOUNT;

    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_MB;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_PERIOD;

    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_MAX_HEAT;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_PER_MB;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_DECAY;

    private static final ModConfigSpec.IntValue CFG_SPRINKLER_FLUID_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_RANGE;

    private static final ModConfigSpec.IntValue CFG_DEUTERIUM_EXTRACTOR_CYCLE_TICKS;
    private static final ModConfigSpec.IntValue CFG_DEUTERIUM_EXTRACTOR_WATER_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_DEUTERIUM_EXTRACTOR_DEUTERIUM_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_DEUTERIUM_EXTRACTOR_SU;

    private static final ModConfigSpec.IntValue CFG_LITHIUM_BRINE_EXTRACTOR_CYCLE_TICKS;
    private static final ModConfigSpec.IntValue CFG_LITHIUM_BRINE_EXTRACTOR_WATER_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_LITHIUM_BRINE_EXTRACTOR_BERYLLIUM_CHANCE;
    private static final ModConfigSpec.IntValue CFG_LITHIUM_BRINE_EXTRACTOR_SU;

    // -------------------------------------------------------------------------
    // Tanks
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_TURBINE_STEAM_TANK;
    private static final ModConfigSpec.IntValue CFG_TURBINE_CONDENSATE_TANK;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_TANK;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_TANK;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_TANK_PER_BLOCK;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_TANK;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_TANK;
    private static final ModConfigSpec.IntValue CFG_FIREBOX_BOILER_TANK;
    private static final ModConfigSpec.IntValue CFG_CRYSTALLIZER_TANK;
    private static final ModConfigSpec.IntValue CFG_DEUTERIUM_EXTRACTOR_TANK;
    private static final ModConfigSpec.IntValue CFG_LITHIUM_BRINE_EXTRACTOR_TANK;

    // -------------------------------------------------------------------------
    // Multiblock minimum sizes
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_MIN_HEIGHT;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_HEIGHT;

    // -------------------------------------------------------------------------
    // Solar Power Tower — mirror array
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_BASE_RADIUS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_RADIUS_PER_HEIGHT;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_MAX_RADIUS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MAX_TRACKED_MIRRORS;

    // -------------------------------------------------------------------------
    // World gen — geysers
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_GEYSER_SPAWN_CHANCE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CFG_GEYSER_BIOMES;

    // -------------------------------------------------------------------------
    // Global warming
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.BooleanValue CFG_GLOBAL_WARMING_ENABLED;
    private static final ModConfigSpec.IntValue CFG_POLLUTION_PER_SOURCE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CFG_PER_BLOCK_POLLUTION;
    private static final ModConfigSpec.IntValue CFG_AERONAUTICS_ENGINE_POLLUTION;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CFG_AUTO_DETECT_KEYWORDS;
    private static final ModConfigSpec.IntValue CFG_AUTO_DETECT_POLLUTION;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CFG_POLLUTION_BLACKLIST;
    private static final ModConfigSpec.IntValue CFG_POLLUTION_RADIUS_BLOCKS;
    private static final ModConfigSpec.IntValue CFG_POLLUTION_DECAY_RATE;
    private static final ModConfigSpec.IntValue CFG_LEAF_ABSORPTION_PER_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_SU;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_ABSORPTION_PER_SECOND;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_RADIUS_BLOCKS;
    private static final ModConfigSpec.IntValue CFG_BIOME_DECAY_THRESHOLD;
    private static final ModConfigSpec.IntValue CFG_BIOME_DECAY_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_BLOCKS_DECAYED_PER_INTERVAL;
    private static final ModConfigSpec.ConfigValue<String> CFG_DEAD_BIOME;

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

        BUILDER.push("steam_turbine");
        CFG_TURBINE_MAX_RPM              = BUILDER.comment("Maximum RPM the turbine can reach (at max height)").defineInRange("max_rpm", 64, 1, 256);
        CFG_TURBINE_RPM_PER_LAYER        = BUILDER.comment("RPM added per rotor layer").defineInRange("rpm_per_layer", 4, 1, 256);
        CFG_TURBINE_SU_PER_LAYER         = BUILDER.comment("SU capacity per rotor layer at full blade efficiency (1 brass blade per arm). Default 256 gives 344 064 SU at max height + full brass, beating the Create superheated steam engine (294 912 SU).").defineInRange("su_per_layer", 256, 1, 1_000_000);
        CFG_TURBINE_STEAM_PER_LAYER_PER_TICK = BUILDER.comment("mB of steam consumed per rotor layer per tick at full blade efficiency (less-efficient blades waste more steam). Default 1 means a max-height turbine consumes 21 mB/t, matching one max-size Solar Power Tower in steam mode at noon.").defineInRange("steam_per_layer_per_tick", 1, 1, 10_000);
        CFG_TURBINE_CONDENSATE_RATIO = BUILDER.comment("mB of steam required to produce 1 mB of condensate water. Higher values mean less water produced per steam consumed. Default 10 means 10 mB steam -> 1 mB water.").defineInRange("condensate_ratio", 10, 1, 10_000);
        BUILDER.pop();

        BUILDER.pop(); // generators

        BUILDER.push("machines");

        BUILDER.push("solar_heater");
        CFG_SOLAR_HEATER_MELT_TICKS       = BUILDER.comment("Ticks to melt one item").defineInRange("melt_ticks", 200, 1, 100_000);
        CFG_SOLAR_HEATER_EVAPORATION_TICKS = BUILDER.comment("Ticks to evaporate enough water to produce one salt").defineInRange("evaporation_ticks", 200, 1, 100_000);
        CFG_SOLAR_HEATER_WATER_PER_SALT   = BUILDER.comment("mB of water consumed per salt produced").defineInRange("water_per_salt_mb", 250, 1, 10_000);
        BUILDER.pop();

        BUILDER.push("firebox_boiler");
        CFG_FIREBOX_BOILER_STEAM_PER_TICK = BUILDER.comment(
                "mB of Steam produced (and mB of water consumed, 1:1) per tick while lit.",
                "Kept low on purpose: a maxed-out Solar Power Tower in steam mode makes 21 mB/t.",
                "Matching that with Firebox Boilers means a wall of them plus a steady fuel supply -",
                "meant as an early bootstrap for the Steam Turbine, not a replacement for the tower.")
                .defineInRange("steam_per_tick", 2, 1, 10_000);
        BUILDER.pop();

        BUILDER.push("fermentation_vat");
        CFG_FERMENTATION_TICKS            = BUILDER.comment("Ticks per fermentation batch").defineInRange("fermentation_ticks", 400, 1, 100_000);
        CFG_FERMENTATION_WATER_PER_BATCH  = BUILDER.comment("mB of water consumed per batch (per footprint block)").defineInRange("water_per_batch_mb", 1000, 1, 100_000);
        CFG_FERMENTATION_BIOFUEL_PER_BATCH = BUILDER.comment("mB of biofuel produced per batch (per footprint block)").defineInRange("biofuel_per_batch_mb", 1000, 1, 100_000);
        BUILDER.pop();

        BUILDER.push("biomass_gasifier");
        CFG_GASIFIER_BURN_TICKS = BUILDER.comment("Ticks raw Biomass burns for in the Gasifier.").defineInRange("burn_ticks", 200, 1, 100_000);
        CFG_PELLET_BURN_TICKS   = BUILDER.comment("Ticks one Biomass Pellet burns for in the Gasifier. Pellets are denser, so they burn longer.").defineInRange("pellet_burn_ticks", 400, 1, 100_000);
        CFG_PELLET_BIOCHAR_AMOUNT = BUILDER.comment("Biochar produced per Biomass Pellet burned (raw Biomass always produces 1).").defineInRange("pellet_biochar_amount", 2, 1, 64);
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

        BUILDER.push("biofilter");
        CFG_BIOFILTER_SU = BUILDER.comment("Stress units consumed while the Biofilter is running").defineInRange("su", 256, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("deuterium_extractor");
        CFG_DEUTERIUM_EXTRACTOR_CYCLE_TICKS = BUILDER.comment("Ticks per extraction cycle").defineInRange("cycle_ticks", 100, 1, 100_000);
        CFG_DEUTERIUM_EXTRACTOR_WATER_PER_CYCLE = BUILDER.comment("mB of water consumed per cycle").defineInRange("water_per_cycle_mb", 50, 1, 10_000);
        CFG_DEUTERIUM_EXTRACTOR_DEUTERIUM_PER_CYCLE = BUILDER.comment("mB of Deuterium produced per cycle").defineInRange("deuterium_per_cycle_mb", 50, 1, 10_000);
        CFG_DEUTERIUM_EXTRACTOR_SU = BUILDER.comment("Stress units consumed while a shaft drives the extractor. Idle (unpowered) it produces nothing.").defineInRange("su", 64, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("lithium_brine_extractor");
        CFG_LITHIUM_BRINE_EXTRACTOR_CYCLE_TICKS = BUILDER.comment("Ticks per extraction cycle").defineInRange("cycle_ticks", 600, 1, 100_000);
        CFG_LITHIUM_BRINE_EXTRACTOR_WATER_PER_CYCLE = BUILDER.comment("mB of water consumed per cycle (alongside 1 Salt item)").defineInRange("water_per_cycle_mb", 250, 1, 10_000);
        CFG_LITHIUM_BRINE_EXTRACTOR_BERYLLIUM_CHANCE = BUILDER.comment("Percent chance each cycle to also produce 1 Beryllium Dust alongside the guaranteed Lithium Dust").defineInRange("beryllium_bonus_chance_percent", 5, 0, 100);
        CFG_LITHIUM_BRINE_EXTRACTOR_SU = BUILDER.comment("Stress units consumed while a shaft drives the extractor. Idle (unpowered) it produces nothing.").defineInRange("su", 128, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.pop(); // machines

        BUILDER.push("tanks");
        CFG_TURBINE_STEAM_TANK      = BUILDER.comment("Steam Turbine: steam input tank capacity (mB)").defineInRange("steam_turbine_steam_mb", 16000, 100, 1_000_000);
        CFG_TURBINE_CONDENSATE_TANK = BUILDER.comment("Steam Turbine: condensate water tank capacity (mB)").defineInRange("steam_turbine_condensate_mb", 8000, 100, 1_000_000);
        CFG_HEAT_BATTERY_TANK               = BUILDER.comment("Heat Battery: molten salt tank capacity (mB)").defineInRange("heat_battery_mb", 8000, 100, 1_000_000);
        CFG_BIOFUEL_ENGINE_TANK             = BUILDER.comment("Biofuel Engine: fuel tank capacity (mB)").defineInRange("biofuel_engine_mb", 8000, 100, 1_000_000);
        CFG_FERMENTATION_VAT_TANK_PER_BLOCK = BUILDER.comment("Fermentation Vat: fluid capacity per multiblock block (mB)").defineInRange("fermentation_vat_per_block_mb", 8000, 100, 1_000_000);
        CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK = BUILDER.comment("Solar Power Tower: fluid capacity per multiblock block (mB)").defineInRange("solar_power_tower_per_block_mb", 8000, 100, 1_000_000);
        CFG_SPRINKLER_TANK                  = BUILDER.comment("Kinetic Sprinkler: fluid tank capacity (mB)").defineInRange("kinetic_sprinkler_mb", 4000, 100, 1_000_000);
        CFG_SOLAR_HEATER_TANK               = BUILDER.comment("Solar Heater: output fluid tank capacity (mB)").defineInRange("solar_heater_mb", 8000, 100, 1_000_000);
        CFG_FIREBOX_BOILER_TANK             = BUILDER.comment("Firebox Boiler: water and steam tank capacity (mB, each tank)").defineInRange("firebox_boiler_mb", 4000, 100, 1_000_000);
        CFG_CRYSTALLIZER_TANK                = BUILDER.comment("Crystallizer: input A, input B, and byproduct output tank capacity (mB, each tank)").defineInRange("crystallizer_mb", 4000, 100, 1_000_000);
        CFG_DEUTERIUM_EXTRACTOR_TANK         = BUILDER.comment("Deuterium Extractor: water and Deuterium tank capacity (mB, each tank)").defineInRange("deuterium_extractor_mb", 4000, 100, 1_000_000);
        CFG_LITHIUM_BRINE_EXTRACTOR_TANK     = BUILDER.comment("Lithium Brine Extractor: water tank capacity (mB)").defineInRange("lithium_brine_extractor_mb", 4000, 100, 1_000_000);
        BUILDER.pop();

        BUILDER.push("multiblocks");
        CFG_FERMENTATION_VAT_MIN_WIDTH    = BUILDER.comment("Fermentation Vat: minimum footprint width (2 = 2x2, 3 = 3x3) needed to ferment").defineInRange("fermentation_vat_min_width", 2, 2, 3);
        CFG_FERMENTATION_VAT_MIN_HEIGHT   = BUILDER.comment("Fermentation Vat: minimum height needed to ferment").defineInRange("fermentation_vat_min_height", 4, 1, 16);
        CFG_SOLAR_POWER_TOWER_MIN_WIDTH   = BUILDER.comment("Solar Power Tower: minimum footprint width needed to produce Molten Salt").defineInRange("solar_power_tower_min_width", 3, 1, 3);
        CFG_SOLAR_POWER_TOWER_MIN_HEIGHT  = BUILDER.comment("Solar Power Tower: minimum block height needed to produce Molten Salt").defineInRange("solar_power_tower_min_height", 3, 1, 20);
        BUILDER.pop();

        BUILDER.push("solar_power_tower_mirrors");
        CFG_SOLAR_POWER_TOWER_MIRROR_BASE_RADIUS = BUILDER.comment("Solar Power Tower: base radius (blocks) of the heliostat mirror field search, before height scaling").defineInRange("solar_power_tower_mirror_base_radius", 4, 0, 64);
        CFG_SOLAR_POWER_TOWER_MIRROR_RADIUS_PER_HEIGHT = BUILDER.comment("Solar Power Tower: extra mirror field radius (blocks) added per block of tower height").defineInRange("solar_power_tower_mirror_radius_per_height", 2, 0, 16);
        CFG_SOLAR_POWER_TOWER_MIRROR_MAX_RADIUS = BUILDER.comment("Solar Power Tower: hard cap on the mirror field search radius (blocks), regardless of height").defineInRange("solar_power_tower_mirror_max_radius", 24, 1, 128);
        CFG_SOLAR_POWER_TOWER_MAX_TRACKED_MIRRORS = BUILDER.comment("Solar Power Tower: maximum number of mirrors a single tower will track/count").defineInRange("solar_power_tower_max_tracked_mirrors", 128, 1, 512);
        BUILDER.pop();

        BUILDER.push("world_gen");
        CFG_GEYSER_SPAWN_CHANCE = BUILDER.comment("Per-attempt spawn chance for Geyser Vents (0 = never, 100 = always)").defineInRange("geyser_spawn_chance", 100, 0, 100);
        CFG_GEYSER_BIOMES       = BUILDER.comment("Biomes where Geyser Vents can spawn (resource location format, e.g. minecraft:desert)")
                .defineListAllowEmpty("geyser_biomes",
                        () -> new ArrayList<>(List.of("minecraft:desert", "minecraft:badlands", "minecraft:eroded_badlands",
                                "minecraft:wooded_badlands", "minecraft:savanna", "minecraft:savanna_plateau",
                                "minecraft:windswept_savanna")),
                        () -> "minecraft:plains",
                        modIdPathValidator("geyser_biomes"));
        BUILDER.pop();

        BUILDER.push("global_warming");
        CFG_GLOBAL_WARMING_ENABLED = BUILDER.comment(
                "Enable the global warming system. When on, blocks in the #solarpunk:pollution_sources tag",
                "(campfires, furnaces, blaze burners, steam engines, etc.) emit black smog while active",
                "and slowly convert the biome of their chunk to a dead wasteland.").define("enabled", false);
        CFG_POLLUTION_PER_SOURCE   = BUILDER.comment("Default pollution units per second for any active source not listed in per_block_pollution.").defineInRange("pollution_per_active_source_per_second", 5, 0, 1_000_000);
        CFG_PER_BLOCK_POLLUTION    = BUILDER.comment(
                "Per-block pollution overrides in \"block_id=amount\" format.",
                "Any block in #solarpunk:pollution_sources not listed here uses the default above.")
                .defineListAllowEmpty("per_block_pollution",
                        () -> new ArrayList<>(List.of(
                                "minecraft:campfire=2",
                                "minecraft:soul_campfire=3",
                                "minecraft:furnace=5",
                                "minecraft:blast_furnace=8",
                                "minecraft:smoker=4",
                                "create:lit_blaze_burner=15",
                                "create:steam_engine=20",
                                "createdieselgenerators:diesel_engine=15",
                                "createdieselgenerators:large_diesel_engine=30",
                                "createdieselgenerators:huge_diesel_engine=50",
                                "solarpunk:biofuel_engine=10",
                                "solarpunk:biomass_gasifier=5",
                                "solarpunk:firebox_boiler=6"
                        )),
                        () -> "block_id=0",
                        modIdPathAmountValidator("per_block_pollution"));
        CFG_AERONAUTICS_ENGINE_POLLUTION = BUILDER.comment(
                "Pollution units per second for each Create Aeronautics portable engine (all 16 dye colours).",
                "Set to 0 to disable. Has no effect if Create Aeronautics is not installed.")
                .defineInRange("aeronautics_portable_engine_pollution_per_second", 15, 0, 1_000_000);
        CFG_AUTO_DETECT_KEYWORDS = BUILDER.comment(
                "Substrings matched against block registry paths for automatic pollution detection.",
                "Any block whose registry path contains one of these keywords, and is not already in",
                "#solarpunk:pollution_sources, will emit the fallback pollution amount below.",
                "Blocks with no block entity are ignored even if their name matches.")
                .defineListAllowEmpty("auto_detect_keywords",
                        () -> new ArrayList<>(List.of(
                                "furnace", "engine", "motor", "boiler", "burner",
                                "kiln", "forge", "oven", "incinerator", "smokestack", "generator"
                        )),
                        () -> "keyword",
                        e -> e instanceof String);
        CFG_AUTO_DETECT_POLLUTION = BUILDER.comment(
                "Pollution units per second emitted by auto-detected blocks (keyword match, no explicit tag entry).")
                .defineInRange("auto_detect_pollution_per_second", 3, 0, 1_000_000);
        CFG_POLLUTION_BLACKLIST = BUILDER.comment(
                "Block IDs that never count as pollution sources, even if in #solarpunk:pollution_sources",
                "or matched by an auto-detect keyword (format: \"modid:path\").")
                .defineListAllowEmpty("pollution_blacklist",
                        () -> new ArrayList<>(List.of(
                                "create_new_age:generator_coil"
                        )),
                        () -> "modid:path",
                        modIdPathValidator("pollution_blacklist"));
        CFG_POLLUTION_RADIUS_BLOCKS = BUILDER.comment("Block radius around each active pollution source that receives pollution (0 = source chunk only).").defineInRange("pollution_radius_blocks", 64, 0, 512);
        CFG_POLLUTION_DECAY_RATE   = BUILDER.comment("Pollution units removed from each chunk per second (0 = pollution never decays).").defineInRange("pollution_decay_rate_per_second", 1, 0, 1_000_000);
        CFG_LEAF_ABSORPTION_PER_INTERVAL = BUILDER.comment("Pollution absorbed per leaf block in a chunk each decay interval. 0 to disable tree absorption.").defineInRange("leaf_absorption_per_interval", 1, 0, 1_000_000);
        CFG_BIOFILTER_ABSORPTION_PER_SECOND = BUILDER.comment("Pollution units removed per second by each placed Biofilter block.").defineInRange("biofilter_absorption_per_second", 10, 0, 1_000_000);
        CFG_BIOFILTER_RADIUS_BLOCKS = BUILDER.comment("Block radius around a Biofilter in which it reduces pollution (0 = own chunk only).").defineInRange("biofilter_radius_blocks", 32, 0, 512);
        CFG_BIOME_DECAY_THRESHOLD       = BUILDER.comment("Pollution level a chunk must reach before its biome converts.").defineInRange("biome_decay_threshold", 10000, 1, Integer.MAX_VALUE);
        CFG_BIOME_DECAY_INTERVAL        = BUILDER.comment("Ticks between each decay and biome-conversion check (1200 = once per minute).").defineInRange("biome_decay_interval_ticks", 1200, 20, 72000);
        CFG_BLOCKS_DECAYED_PER_INTERVAL = BUILDER.comment("Random surface blocks replaced per polluted chunk each decay interval (grass dies, leaves fall, flowers wither). 0 to disable.").defineInRange("blocks_decayed_per_interval", 8, 0, 256);
        CFG_DEAD_BIOME                  = BUILDER.comment("The biome polluted chunks convert to once they reach the threshold (resource location format).").define("dead_biome", "minecraft:badlands");
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
    public static int turbineMaxRpm, turbineRpmPerLayer, turbineSuPerLayer, turbineSteamPerLayerPerTick, turbineCondensateRatio;
    public static int turbineSteamTank, turbineCondensateTank;

    public static int solarHeaterMeltTicks, solarHeaterEvaporationTicks, solarHeaterWaterPerSalt;
    public static int fireboxBoilerSteamPerTick;
    public static int fermentationTicks, fermentationWaterPerBatch, fermentationBiofuelPerBatch;
    public static int gasifierBurnTicks, pelletBurnTicks, pelletBiocharAmount;
    public static int biofuelConsumeMb, biofuelConsumePeriod;
    public static int heatBatteryMaxHeat, heatBatteryHeatPerMb, heatBatteryHeatDecay;
    public static int sprinklerFluidPerCycle, sprinklerRange;

    public static int deuteriumExtractorCycleTicks, deuteriumExtractorWaterPerCycle, deuteriumExtractorDeuteriumPerCycle, deuteriumExtractorSu;
    public static int lithiumBrineExtractorCycleTicks, lithiumBrineExtractorWaterPerCycle, lithiumBrineExtractorBerylliumChance, lithiumBrineExtractorSu;

    public static int heatBatteryTank, biofuelEngineTank;
    public static int fermentationVatTankPerBlock, solarPowerTowerTankPerBlock;
    public static int sprinklerTank, solarHeaterTank, fireboxBoilerTank, crystallizerTank;
    public static int deuteriumExtractorTank, lithiumBrineExtractorTank;

    public static int fermentationVatMinWidth, fermentationVatMinHeight;
    public static int solarPowerTowerMinWidth, solarPowerTowerMinHeight;

    public static int solarPowerTowerMirrorBaseRadius, solarPowerTowerMirrorRadiusPerHeight;
    public static int solarPowerTowerMirrorMaxRadius, solarPowerTowerMaxTrackedMirrors;

    public static int geyserSpawnChance;
    public static List<? extends String> geyserBiomes;

    public static boolean globalWarmingEnabled;
    public static int pollutionPerSource, pollutionRadiusBlocks, pollutionDecayRate;
    public static int leafAbsorptionPerInterval, biofilterSu, biofilterAbsorptionPerSecond, biofilterRadiusBlocks;
    public static int biomeDecayThreshold, biomeDecayInterval, blocksDecayedPerInterval;
    public static String deadBiome;
    public static Map<String, Integer> perBlockPollution = new java.util.HashMap<>();
    public static List<? extends String> autoDetectKeywords = new ArrayList<>();
    public static int autoDetectPollution;
    public static int aeronauticsEnginePollution;
    public static Set<String> pollutionBlacklist = new java.util.HashSet<>();

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

        turbineMaxRpm               = CFG_TURBINE_MAX_RPM.get();
        turbineRpmPerLayer          = CFG_TURBINE_RPM_PER_LAYER.get();
        turbineSuPerLayer           = CFG_TURBINE_SU_PER_LAYER.get();
        turbineSteamPerLayerPerTick = CFG_TURBINE_STEAM_PER_LAYER_PER_TICK.get();
        turbineCondensateRatio      = CFG_TURBINE_CONDENSATE_RATIO.get();

        solarHeaterMeltTicks       = CFG_SOLAR_HEATER_MELT_TICKS.get();
        solarHeaterEvaporationTicks = CFG_SOLAR_HEATER_EVAPORATION_TICKS.get();
        solarHeaterWaterPerSalt    = CFG_SOLAR_HEATER_WATER_PER_SALT.get();

        fireboxBoilerSteamPerTick = CFG_FIREBOX_BOILER_STEAM_PER_TICK.get();

        fermentationTicks          = CFG_FERMENTATION_TICKS.get();
        fermentationWaterPerBatch  = CFG_FERMENTATION_WATER_PER_BATCH.get();
        fermentationBiofuelPerBatch = CFG_FERMENTATION_BIOFUEL_PER_BATCH.get();

        gasifierBurnTicks    = CFG_GASIFIER_BURN_TICKS.get();
        pelletBurnTicks      = CFG_PELLET_BURN_TICKS.get();
        pelletBiocharAmount  = CFG_PELLET_BIOCHAR_AMOUNT.get();

        biofuelConsumeMb     = CFG_BIOFUEL_CONSUME_MB.get();
        biofuelConsumePeriod = CFG_BIOFUEL_CONSUME_PERIOD.get();

        heatBatteryMaxHeat   = CFG_HEAT_BATTERY_MAX_HEAT.get();
        heatBatteryHeatPerMb = CFG_HEAT_BATTERY_HEAT_PER_MB.get();
        heatBatteryHeatDecay = CFG_HEAT_BATTERY_HEAT_DECAY.get();

        sprinklerFluidPerCycle = CFG_SPRINKLER_FLUID_PER_CYCLE.get();
        sprinklerRange         = CFG_SPRINKLER_RANGE.get();

        deuteriumExtractorCycleTicks        = CFG_DEUTERIUM_EXTRACTOR_CYCLE_TICKS.get();
        deuteriumExtractorWaterPerCycle     = CFG_DEUTERIUM_EXTRACTOR_WATER_PER_CYCLE.get();
        deuteriumExtractorDeuteriumPerCycle = CFG_DEUTERIUM_EXTRACTOR_DEUTERIUM_PER_CYCLE.get();
        deuteriumExtractorSu                = CFG_DEUTERIUM_EXTRACTOR_SU.get();

        lithiumBrineExtractorCycleTicks     = CFG_LITHIUM_BRINE_EXTRACTOR_CYCLE_TICKS.get();
        lithiumBrineExtractorWaterPerCycle  = CFG_LITHIUM_BRINE_EXTRACTOR_WATER_PER_CYCLE.get();
        lithiumBrineExtractorBerylliumChance = CFG_LITHIUM_BRINE_EXTRACTOR_BERYLLIUM_CHANCE.get();
        lithiumBrineExtractorSu              = CFG_LITHIUM_BRINE_EXTRACTOR_SU.get();

        turbineSteamTank        = CFG_TURBINE_STEAM_TANK.get();
        turbineCondensateTank   = CFG_TURBINE_CONDENSATE_TANK.get();
        heatBatteryTank              = CFG_HEAT_BATTERY_TANK.get();
        biofuelEngineTank            = CFG_BIOFUEL_ENGINE_TANK.get();
        fermentationVatTankPerBlock  = CFG_FERMENTATION_VAT_TANK_PER_BLOCK.get();
        solarPowerTowerTankPerBlock  = CFG_SOLAR_POWER_TOWER_TANK_PER_BLOCK.get();
        sprinklerTank                = CFG_SPRINKLER_TANK.get();
        solarHeaterTank              = CFG_SOLAR_HEATER_TANK.get();
        fireboxBoilerTank            = CFG_FIREBOX_BOILER_TANK.get();
        crystallizerTank             = CFG_CRYSTALLIZER_TANK.get();
        deuteriumExtractorTank       = CFG_DEUTERIUM_EXTRACTOR_TANK.get();
        lithiumBrineExtractorTank    = CFG_LITHIUM_BRINE_EXTRACTOR_TANK.get();

        fermentationVatMinWidth     = CFG_FERMENTATION_VAT_MIN_WIDTH.get();
        fermentationVatMinHeight    = CFG_FERMENTATION_VAT_MIN_HEIGHT.get();
        solarPowerTowerMinWidth     = CFG_SOLAR_POWER_TOWER_MIN_WIDTH.get();
        solarPowerTowerMinHeight    = CFG_SOLAR_POWER_TOWER_MIN_HEIGHT.get();

        solarPowerTowerMirrorBaseRadius      = CFG_SOLAR_POWER_TOWER_MIRROR_BASE_RADIUS.get();
        solarPowerTowerMirrorRadiusPerHeight = CFG_SOLAR_POWER_TOWER_MIRROR_RADIUS_PER_HEIGHT.get();
        solarPowerTowerMirrorMaxRadius       = CFG_SOLAR_POWER_TOWER_MIRROR_MAX_RADIUS.get();
        solarPowerTowerMaxTrackedMirrors     = CFG_SOLAR_POWER_TOWER_MAX_TRACKED_MIRRORS.get();

        geyserSpawnChance = CFG_GEYSER_SPAWN_CHANCE.get();
        geyserBiomes      = CFG_GEYSER_BIOMES.get();

        globalWarmingEnabled  = CFG_GLOBAL_WARMING_ENABLED.get();
        pollutionPerSource    = CFG_POLLUTION_PER_SOURCE.get();
        pollutionRadiusBlocks = CFG_POLLUTION_RADIUS_BLOCKS.get();
        pollutionDecayRate    = CFG_POLLUTION_DECAY_RATE.get();
        leafAbsorptionPerInterval       = CFG_LEAF_ABSORPTION_PER_INTERVAL.get();
        biofilterSu                     = CFG_BIOFILTER_SU.get();
        biofilterAbsorptionPerSecond    = CFG_BIOFILTER_ABSORPTION_PER_SECOND.get();
        biofilterRadiusBlocks           = CFG_BIOFILTER_RADIUS_BLOCKS.get();
        biomeDecayThreshold       = CFG_BIOME_DECAY_THRESHOLD.get();
        biomeDecayInterval        = CFG_BIOME_DECAY_INTERVAL.get();
        blocksDecayedPerInterval  = CFG_BLOCKS_DECAYED_PER_INTERVAL.get();
        deadBiome                 = CFG_DEAD_BIOME.get();

        perBlockPollution = new java.util.HashMap<>();
        for (String entry : CFG_PER_BLOCK_POLLUTION.get()) {
            int sep = entry.lastIndexOf('=');
            if (sep < 0) continue;
            try {
                perBlockPollution.put(entry.substring(0, sep), Integer.parseInt(entry.substring(sep + 1)));
            } catch (NumberFormatException ignored) {}
        }

        autoDetectKeywords = CFG_AUTO_DETECT_KEYWORDS.get();
        autoDetectPollution = CFG_AUTO_DETECT_POLLUTION.get();
        pollutionBlacklist = new java.util.HashSet<>(CFG_POLLUTION_BLACKLIST.get());
        GlobalWarmingHandler.invalidateAutoDetectedCache();

        aeronauticsEnginePollution = CFG_AERONAUTICS_ENGINE_POLLUTION.get();
        for (String colour : List.of("white", "orange", "magenta", "light_blue", "yellow", "lime",
                "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black")) {
            perBlockPollution.put("simulated:" + colour + "_portable_engine", aeronauticsEnginePollution);
        }
    }
}
