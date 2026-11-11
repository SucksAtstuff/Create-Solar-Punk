package net.succ.solar_punk;

import net.minecraft.world.item.DyeColor;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final ModConfigSpec.IntValue CFG_SHINGLE_MORNING_FE;
    private static final ModConfigSpec.IntValue CFG_SHINGLE_NOON_FE;
    private static final ModConfigSpec.IntValue CFG_SHINGLE_BUFFER;
    private static final ModConfigSpec.IntValue CFG_SHINGLE_MAX_EXTRACT;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_MORNING_FE;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_NOON_FE;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_PANE_MORNING_FE;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_PANE_NOON_FE;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_BUFFER;
    private static final ModConfigSpec.IntValue CFG_SOLAR_GLASS_MAX_EXTRACT;

    private static final ModConfigSpec.IntValue CFG_GEYSER_RPM;
    private static final ModConfigSpec.IntValue CFG_GEYSER_SU;

    private static final ModConfigSpec.BooleanValue CFG_GEYSER_ERUPTIONS;
    private static final ModConfigSpec.IntValue CFG_GEYSER_MIN_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_GEYSER_MAX_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_GEYSER_WARNING;
    private static final ModConfigSpec.IntValue CFG_GEYSER_ERUPTION;
    private static final ModConfigSpec.DoubleValue CFG_GEYSER_DAMAGE;
    private static final ModConfigSpec.IntValue CFG_GEYSER_HEIGHT;

    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_RPM;
    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_SU;
    private static final ModConfigSpec.IntValue CFG_KINETIC_BATTERY_MAX_CHARGE;
    private static final ModConfigSpec.DoubleValue CFG_KINETIC_BATTERY_DISCHARGE_RATE;

    private static final ModConfigSpec.IntValue CFG_GASIFIER_RPM;
    private static final ModConfigSpec.IntValue CFG_GASIFIER_SU;

    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_RPM;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_ENGINE_SU;
    private static final ModConfigSpec.IntValue CFG_MOTOR_MAX_RPM, CFG_MOTOR_SU, CFG_MOTOR_FE_PER_RPM;

    private static final ModConfigSpec.IntValue CFG_TURBINE_MAX_RPM;
    private static final ModConfigSpec.IntValue CFG_TURBINE_RPM_PER_LAYER;
    private static final ModConfigSpec.IntValue CFG_TURBINE_SU_PER_LAYER;
    private static final ModConfigSpec.IntValue CFG_TURBINE_STEAM_PER_LAYER_PER_TICK;
    private static final ModConfigSpec.IntValue CFG_TURBINE_CONDENSATE_RATIO;
    private static final ModConfigSpec.BooleanValue CFG_TURBINE_CROWNS_STEAM;
    private static final ModConfigSpec.DoubleValue CFG_TURBINE_CROWNS_MIN_VAPOR_QUALITY;

    private static final ModConfigSpec.DoubleValue CFG_SOLAR_POWER_TOWER_SALT_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CFG_SOLAR_POWER_TOWER_STEAM_MULTIPLIER;

    private static final ModConfigSpec.BooleanValue CFG_SERENE_SEASONS_ENABLED;
    private static final ModConfigSpec.DoubleValue CFG_SERENE_SEASONS_SUMMER_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CFG_SERENE_SEASONS_WINTER_MULTIPLIER;

    private static final ModConfigSpec.BooleanValue CFG_SUN_ANGLE;
    private static final ModConfigSpec.DoubleValue CFG_SPACE_SOLAR_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue CFG_SPACE_REAL_SUN;

    // -------------------------------------------------------------------------
    // Machines: speed and output amounts
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_MELT_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_EVAPORATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_HEATER_WATER_PER_SALT;

    private static final ModConfigSpec.IntValue CFG_FIREBOX_BOILER_STEAM_PER_TICK;

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_TICKS;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_WATER_PER_BATCH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_BIOFUEL_PER_BATCH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_RICH_FERTILIZER_PER_BATCH;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_RICH_EXTRA_RANGE;

    private static final ModConfigSpec.IntValue CFG_GASIFIER_BURN_TICKS;
    private static final ModConfigSpec.IntValue CFG_PELLET_BURN_TICKS;
    private static final ModConfigSpec.IntValue CFG_PELLET_BIOCHAR_AMOUNT;

    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_MB;
    private static final ModConfigSpec.IntValue CFG_BIOFUEL_CONSUME_PERIOD;

    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_MAX_HEAT;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_PER_MB;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_HEAT_DECAY;
    private static final ModConfigSpec.IntValue CFG_HEAT_BATTERY_NEW_AGE_MAX_TEMPERATURE;
    private static final ModConfigSpec.DoubleValue CFG_HEAT_BATTERY_NEW_AGE_HEAT_RATE;

    private static final ModConfigSpec.IntValue CFG_SPRINKLER_FLUID_PER_CYCLE;
    private static final ModConfigSpec.IntValue CFG_SPRINKLER_RANGE;

    private static final ModConfigSpec.IntValue CFG_REACTOR_STEAM_PER_LAYER_PER_TICK;
    private static final ModConfigSpec.DoubleValue CFG_REACTOR_LITHIUM_OUTPUT_RATIO;
    private static final ModConfigSpec.IntValue CFG_REACTOR_FUEL_PER_STEAM_MB;
    private static final ModConfigSpec.DoubleValue CFG_REACTOR_BERYLLIUM_FUEL_WASTE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CFG_REACTOR_BREEDER_FLOOR_RATIO;
    private static final ModConfigSpec.IntValue CFG_REACTOR_TRITIUM_CAPSULE_MB;
    private static final ModConfigSpec.DoubleValue CFG_REACTOR_BREEDER_CAPTURE_RATIO;

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
    private static final ModConfigSpec.IntValue CFG_ELECTROLYZER_TANK;
    private static final ModConfigSpec.IntValue CFG_FUSION_REACTOR_STEAM_TANK;
    private static final ModConfigSpec.IntValue CFG_FUSION_REACTOR_DEUTERIUM_TANK;

    // -------------------------------------------------------------------------
    // Multiblock minimum sizes
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_FERMENTATION_VAT_MIN_HEIGHT;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_WIDTH;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIN_HEIGHT;

    // -------------------------------------------------------------------------
    // Solar Power Tower: mirror array
    // -------------------------------------------------------------------------

    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_BASE_RADIUS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_RADIUS_PER_HEIGHT;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MIRROR_MAX_RADIUS;
    private static final ModConfigSpec.IntValue CFG_SOLAR_POWER_TOWER_MAX_TRACKED_MIRRORS;

    // -------------------------------------------------------------------------
    // World gen: geysers
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
    private static final ModConfigSpec.IntValue CFG_TRELLIS_ABSORPTION_PER_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_SU;
    private static final ModConfigSpec.IntValue CFG_STREETLAMP_FE_PER_TICK;
    private static final ModConfigSpec.IntValue CFG_STREETLAMP_BUFFER;
    private static final ModConfigSpec.IntValue CFG_STREETLAMP_MAX_RECEIVE;
    private static final ModConfigSpec.IntValue CFG_GROW_LAMP_FE_PER_TICK, CFG_GROW_LAMP_BUFFER, CFG_GROW_LAMP_MAX_RECEIVE, CFG_GROW_LAMP_RANGE;
    private static final ModConfigSpec.DoubleValue CFG_GROW_LAMP_GROWTH_BOOST;
    private static final ModConfigSpec.IntValue CFG_ELECTROLYZER_FE_PER_TICK, CFG_ELECTROLYZER_WATER_PER_TICK, CFG_ELECTROLYZER_BUFFER, CFG_ELECTROLYZER_MAX_RECEIVE;
    private static final ModConfigSpec.IntValue CFG_SOLAR_PLANT_BED_MORNING_FE, CFG_SOLAR_PLANT_BED_NOON_FE, CFG_SOLAR_PLANT_BED_FE_PER_TICK, CFG_SOLAR_PLANT_BED_BUFFER;
    private static final ModConfigSpec.DoubleValue CFG_SOLAR_PLANT_BED_GROWTH_BOOST;
    private static final ModConfigSpec.DoubleValue CFG_AGRIVOLTAICS_BONUS;
    private static final ModConfigSpec.IntValue CFG_AGRIVOLTAICS_RANGE;
    private static final ModConfigSpec.IntValue CFG_WIRE_IRON_RATE, CFG_WIRE_IRON_LENGTH;
    private static final ModConfigSpec.IntValue CFG_WIRE_COPPER_RATE, CFG_WIRE_COPPER_LENGTH;
    private static final ModConfigSpec.IntValue CFG_WIRE_GOLD_RATE, CFG_WIRE_GOLD_LENGTH;
    private static final ModConfigSpec.IntValue CFG_WIRE_BLOCKS_PER_WIRE;
    private static final ModConfigSpec.IntValue CFG_WIRE_MAX_CONNECTIONS;
    private static final ModConfigSpec.IntValue CFG_GREENHOUSE_SCAN_INTERVAL;
    private static final ModConfigSpec.IntValue CFG_GREENHOUSE_MAX_VOLUME;
    private static final ModConfigSpec.DoubleValue CFG_GREENHOUSE_GROWTH_BOOST;
    private static final ModConfigSpec.DoubleValue CFG_GREENHOUSE_POLLINATION_BONUS;
    private static final ModConfigSpec.DoubleValue CFG_BIOFILTER_ABSORPTION_PER_RPM;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_RADIUS_BLOCKS;
    private static final ModConfigSpec.IntValue CFG_BIOFILTER_POLLUTION_PER_CARBON;
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

        BUILDER.push("agrivoltaics");
        CFG_AGRIVOLTAICS_BONUS = BUILDER.comment("Extra output for an Andesite or Brass Solar Panel with a plant below it, cooling it (0.1 = 10% more). 0 to disable").defineInRange("bonus", 0.1, 0.0, 10.0);
        CFG_AGRIVOLTAICS_RANGE = BUILDER.comment("How many blocks below a panel it looks for a plant, through air").defineInRange("range", 4, 1, 16);
        BUILDER.pop();

        BUILDER.push("solar_shingles");
        CFG_SHINGLE_MORNING_FE  = BUILDER.comment("FE/t per shingle at dawn and dusk, before the roof angle is applied").defineInRange("morning_fe_per_tick", 4, 1, 100_000);
        CFG_SHINGLE_NOON_FE     = BUILDER.comment("FE/t per shingle at noon (clear weather), before the roof angle is applied").defineInRange("noon_fe_per_tick", 8, 1, 100_000);
        CFG_SHINGLE_BUFFER      = BUILDER.comment("FE buffer per shingle. Energy flows across a roof towards whichever shingle is being drained, so this also limits how fast a big roof can deliver").defineInRange("buffer_capacity", 4_000, 100, 10_000_000);
        CFG_SHINGLE_MAX_EXTRACT = BUILDER.comment("Max FE per tick a single shingle hands to adjacent cables, which is the most a whole roof can deliver through one shingle").defineInRange("max_extract", 1_000, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("solar_glass");
        CFG_SOLAR_GLASS_MORNING_FE      = BUILDER.comment("FE/t per Solar Glass block at dawn and dusk").defineInRange("morning_fe_per_tick", 2, 1, 100_000);
        CFG_SOLAR_GLASS_NOON_FE         = BUILDER.comment("FE/t per Solar Glass block at noon (clear weather)").defineInRange("noon_fe_per_tick", 4, 1, 100_000);
        CFG_SOLAR_GLASS_PANE_MORNING_FE = BUILDER.comment("FE/t per Solar Glass Pane at dawn and dusk").defineInRange("pane_morning_fe_per_tick", 1, 1, 100_000);
        CFG_SOLAR_GLASS_PANE_NOON_FE    = BUILDER.comment("FE/t per Solar Glass Pane at noon (clear weather)").defineInRange("pane_noon_fe_per_tick", 2, 1, 100_000);
        CFG_SOLAR_GLASS_BUFFER          = BUILDER.comment("FE buffer per glass block or pane. Energy flows across touching glass (and Solar Shingles) towards whichever block is being drained").defineInRange("buffer_capacity", 2_000, 100, 10_000_000);
        CFG_SOLAR_GLASS_MAX_EXTRACT     = BUILDER.comment("Max FE per tick a single glass block or pane hands to adjacent cables").defineInRange("max_extract", 1_000, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("geyser_cap");
        CFG_GEYSER_RPM = BUILDER.comment("RPM output").defineInRange("rpm", 32, 1, 256);
        CFG_GEYSER_SU  = BUILDER.comment("Stress capacity").defineInRange("su", 512, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("geyser_vent");
        CFG_GEYSER_ERUPTIONS    = BUILDER.comment("Uncapped Geyser Vents erupt now and then, scalding and launching anything standing over them").define("eruptions", true);
        CFG_GEYSER_MIN_INTERVAL = BUILDER.comment("Shortest time between eruptions, in seconds").defineInRange("min_interval_seconds", 120, 10, 86_400);
        CFG_GEYSER_MAX_INTERVAL = BUILDER.comment("Longest time between eruptions, in seconds (raised to the minimum if lower)").defineInRange("max_interval_seconds", 300, 10, 86_400);
        CFG_GEYSER_WARNING      = BUILDER.comment("How long the vent rumbles before it erupts, in seconds").defineInRange("warning_seconds", 5, 0, 60);
        CFG_GEYSER_ERUPTION     = BUILDER.comment("How long an eruption lasts, in seconds").defineInRange("eruption_seconds", 6, 1, 60);
        CFG_GEYSER_DAMAGE       = BUILDER.comment("Damage dealt every half second to anything in the eruption").defineInRange("damage", 3.0, 0.0, 1000.0);
        CFG_GEYSER_HEIGHT       = BUILDER.comment("How many blocks high the eruption reaches (blocks in the way cut it short)").defineInRange("height", 8, 1, 32);
        BUILDER.pop();

        BUILDER.push("kinetic_battery");
        CFG_KINETIC_BATTERY_RPM            = BUILDER.comment("RPM output while discharging").defineInRange("rpm", 16, 1, 256);
        CFG_KINETIC_BATTERY_SU             = BUILDER.comment("Stress capacity while discharging").defineInRange("su", 16, 1, 1_000_000);
        CFG_KINETIC_BATTERY_MAX_CHARGE     = BUILDER.comment("Maximum stored charge").defineInRange("max_charge", 1200, 100, 1_000_000);
        CFG_KINETIC_BATTERY_DISCHARGE_RATE = BUILDER.comment("Charge lost per tick while discharging. Charging at the battery's own RPM gains the same amount, faster networks proportionally more").defineInRange("discharge_rate", 0.5, 0.01, 100.0);
        BUILDER.pop();

        BUILDER.push("biomass_gasifier");
        CFG_GASIFIER_RPM = BUILDER.comment("RPM output while burning").defineInRange("rpm", 8, 1, 256);
        CFG_GASIFIER_SU  = BUILDER.comment("Stress capacity while burning").defineInRange("su", 256, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("biofuel_engine");
        CFG_BIOFUEL_ENGINE_RPM = BUILDER.comment("RPM output while running").defineInRange("rpm", 16, 1, 256);
        CFG_BIOFUEL_ENGINE_SU  = BUILDER.comment("Stress capacity while running").defineInRange("su", 512, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("electric_motor");
        CFG_MOTOR_MAX_RPM    = BUILDER.comment("Highest speed the Electric Motor can be set to").defineInRange("max_rpm", 64, 1, 256);
        CFG_MOTOR_SU         = BUILDER.comment("Stress capacity while running (per RPM, like the other generators: Create multiplies it by the set speed)").defineInRange("su", 128, 1, 1_000_000);
        CFG_MOTOR_FE_PER_RPM = BUILDER.comment("FE per tick used per RPM of the set speed, whatever the load").defineInRange("fe_per_rpm", 4, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("steam_turbine");
        CFG_TURBINE_MAX_RPM              = BUILDER.comment("Maximum RPM the turbine can reach (at max height)").defineInRange("max_rpm", 64, 1, 256);
        CFG_TURBINE_RPM_PER_LAYER        = BUILDER.comment("RPM added per rotor layer").defineInRange("rpm_per_layer", 4, 1, 256);
        CFG_TURBINE_SU_PER_LAYER         = BUILDER.comment("SU capacity per rotor layer at full blade efficiency (1 brass blade per arm). Default 256 gives 344 064 SU at max height + full brass, beating the Create superheated steam engine (294 912 SU).").defineInRange("su_per_layer", 256, 1, 1_000_000);
        CFG_TURBINE_STEAM_PER_LAYER_PER_TICK = BUILDER.comment("mB of steam consumed per rotor layer per tick at full blade efficiency (less-efficient blades waste more steam). Default 1 means a max-height turbine consumes 21 mB/t, matching one max-size Solar Power Tower in steam mode at noon.").defineInRange("steam_per_layer_per_tick", 1, 1, 10_000);
        CFG_TURBINE_CONDENSATE_RATIO = BUILDER.comment("mB of steam required to produce 1 mB of condensate water. Higher values mean less water produced per steam consumed. Default 10 means 10 mB steam -> 1 mB water.").defineInRange("condensate_ratio", 10, 1, 10_000);
        CFG_TURBINE_CROWNS_STEAM = BUILDER.comment(
                "With Create: CROWNS installed, also burn its steam (water heated past boiling, carrying",
                "CROWNS' gas state), 1 mB of it giving 1 mB of Steam. Has no effect if CROWNS is absent.")
                .define("crowns_steam", true);
        CFG_TURBINE_CROWNS_MIN_VAPOR_QUALITY = BUILDER.comment(
                "Lowest CROWNS vapor quality accepted: 0 = liquid water, 1 = fully boiled (dry or superheated)",
                "steam. Wetter steam is refused.")
                .defineInRange("crowns_min_vapor_quality", 0.9, 0.01, 1.0);
        BUILDER.pop();

        BUILDER.push("solar_power_tower");
        CFG_SOLAR_POWER_TOWER_SALT_MULTIPLIER = BUILDER.comment(
                "Molten Salt output multiplier. The base rate formula yields 9 at max size",
                "(3x3x20 tower, full sun, full mirror field), so default 2.0 = 18 mB/t while the",
                "sun is up. Production only runs during the daylight half of the day/night cycle,",
                "while a charged Heat Battery drains a steady 1 mB/t around the clock, so 18 mB/t",
                "by day averages out to 9 mB/t over a full day/night cycle - exactly enough to hold",
                "9 Heat Batteries (a max-size boiler's worth) superheated continuously. Raise it for",
                "headroom to recharge a drained bank, cover rainy days, or run more batteries.")
                .defineInRange("salt_output_multiplier", 2.0, 0.0, 1000.0);
        CFG_SOLAR_POWER_TOWER_STEAM_MULTIPLIER = BUILDER.comment(
                "Steam output multiplier. Default 7/3 = 21 mB/t at max size, matching one",
                "max-height Steam Turbine's consumption.")
                .defineInRange("steam_output_multiplier", 7.0 / 3.0, 0.0, 1000.0);
        BUILDER.pop();

        BUILDER.push("serene_seasons");
        CFG_SERENE_SEASONS_ENABLED = BUILDER.comment(
                "Scale solar output (both Solar Panels and the Solar Power Tower) with the current season",
                "when Serene Seasons is installed. Summer boosts output, winter cuts it, spring and autumn",
                "sit near neutral, ramping smoothly in between. Has no effect if Serene Seasons is absent.",
                "Assumes Serene Seasons is managing the Overworld (its default) - only Overworld generators",
                "are affected. Set to false to keep flat year-round output even with Serene Seasons present.")
                .define("enabled", true);
        CFG_SERENE_SEASONS_SUMMER_MULTIPLIER = BUILDER.comment(
                "Peak solar output multiplier at midsummer, in a temperate biome.")
                .defineInRange("summer_multiplier", 1.15, 1.0, 5.0);
        CFG_SERENE_SEASONS_WINTER_MULTIPLIER = BUILDER.comment(
                "Lowest solar output multiplier at midwinter, in a temperate biome. Hot biomes (desert,",
                "savanna, badlands, jungle) barely swing at all regardless, matching Serene Seasons treating",
                "them as seasonless.")
                .defineInRange("winter_multiplier", 0.7, 0.05, 1.0);
        BUILDER.pop();

        CFG_SUN_ANGLE = BUILDER.comment(
                "Only with Sable installed (Create Aeronautics, Create: Cosmonautics): solar output depends on",
                "the angle to the sun. Solar Shingles make more on the slope facing the sun and less on the",
                "one facing away, and panels on a tilted contraption follow its tilt. Set to false to ignore",
                "angles, so every solar block only follows the time of day.")
                .define("sun_angle", true);

        BUILDER.push("space");
        CFG_SPACE_SOLAR_MULTIPLIER = BUILDER.comment(
                "Output multiplier for Solar Panels and the Solar Power Tower in dimensions tagged",
                "solarpunk:always_sunny (e.g. Create: Cosmonautics deep space). Those dimensions have no",
                "weather, so 1.0 keeps output at the normal noon value while the sun is up.")
                .defineInRange("output_multiplier", 1.0, 0.0, 10.0);
        CFG_SPACE_REAL_SUN = BUILDER.comment(
                "In Create: Cosmonautics deep space, follow the real sun: panels facing away from it, in a",
                "planet's shadow, or shaded by the station's hull stop producing. Set to false to make deep",
                "space always sunny instead, so panels only need a clear view straight up.")
                .define("real_sun", true);
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
        CFG_FERMENTATION_RICH_FERTILIZER_PER_BATCH = BUILDER.comment("mB of Rich Fertilizer produced per batch of waste (per footprint block)").defineInRange("rich_fertilizer_per_batch_mb", 500, 1, 100_000);
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
        CFG_HEAT_BATTERY_NEW_AGE_MAX_TEMPERATURE = BUILDER.comment(
                "Create: New Age compat. A Heat Battery acts as a New Age heat block whose temperature",
                "rises with its charge, up to this at full. It trades heat with touching New Age heat",
                "blocks towards that temperature: a full one heats their pipes, an empty one soaks heat",
                "up. Default 1000 is enough for a Superheated New Age Heater and far below the 10000",
                "where their parts overheat.")
                .defineInRange("new_age_max_temperature", 1000, 1, 1_000_000);
        CFG_HEAT_BATTERY_NEW_AGE_HEAT_RATE = BUILDER.comment(
                "Heat Battery heat per unit of New Age heat, in both directions. Default 1.0 means a full",
                "battery runs a Superheated New Age Heater (500 heat/s) for a few minutes.")
                .defineInRange("new_age_heat_rate", 1.0, 0.001, 1000.0);
        BUILDER.pop();

        BUILDER.push("kinetic_sprinkler");
        CFG_SPRINKLER_FLUID_PER_CYCLE = BUILDER.comment("mB of fluid consumed per cycle").defineInRange("fluid_per_cycle_mb", 100, 1, 10_000);
        CFG_SPRINKLER_RANGE           = BUILDER.comment("Horizontal radius (blocks) of the watering area").defineInRange("range", 2, 1, 16);
        CFG_SPRINKLER_RICH_EXTRA_RANGE = BUILDER.comment("Extra radius (blocks) while spraying Rich Fertilizer, which also lasts twice as long").defineInRange("rich_fertilizer_extra_range", 1, 0, 16);
        BUILDER.pop();

        BUILDER.push("greenhouse_controller");
        CFG_GREENHOUSE_SCAN_INTERVAL     = BUILDER.comment("Ticks between checks of whether the greenhouse is sealed").defineInRange("scan_interval_ticks", 100, 20, 12_000);
        CFG_GREENHOUSE_MAX_VOLUME        = BUILDER.comment("Most air blocks a greenhouse can hold. A bigger (or leaking) space doesn't count as sealed").defineInRange("max_volume", 8192, 8, 262_144);
        CFG_GREENHOUSE_GROWTH_BOOST      = BUILDER.comment("Extra growth for plants under the glass roof of a sealed greenhouse (1.0 = twice as fast)").defineInRange("growth_boost", 1.0, 0.0, 100.0);
        CFG_GREENHOUSE_POLLINATION_BONUS = BUILDER.comment("Extra growth on top of growth_boost while bees are inside").defineInRange("pollination_bonus", 0.5, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("biofilter");
        CFG_BIOFILTER_SU = BUILDER.comment("Stress units consumed while the Biofilter is running").defineInRange("su", 256, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("streetlamp");
        CFG_STREETLAMP_FE_PER_TICK = BUILDER.comment("FE/t a Streetlamp uses while lit").defineInRange("fe_per_tick", 2, 0, 100_000);
        CFG_STREETLAMP_BUFFER      = BUILDER.comment("FE a Streetlamp stores. The default runs it through a whole night").defineInRange("buffer_capacity", 30_000, 100, 10_000_000);
        CFG_STREETLAMP_MAX_RECEIVE = BUILDER.comment("Max FE per tick a Streetlamp accepts").defineInRange("max_receive", 100, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("grow_lamp");
        CFG_GROW_LAMP_FE_PER_TICK   = BUILDER.comment("FE/t a Grow Lamp uses while lit").defineInRange("fe_per_tick", 8, 0, 100_000);
        CFG_GROW_LAMP_BUFFER        = BUILDER.comment("FE a Grow Lamp stores").defineInRange("buffer_capacity", 10_000, 100, 10_000_000);
        CFG_GROW_LAMP_MAX_RECEIVE   = BUILDER.comment("Max FE per tick a Grow Lamp accepts").defineInRange("max_receive", 200, 1, 1_000_000);
        CFG_GROW_LAMP_RANGE         = BUILDER.comment("How many blocks below a Grow Lamp it reaches plants, in a 3x3 area under it").defineInRange("range", 6, 1, 32);
        CFG_GROW_LAMP_GROWTH_BOOST  = BUILDER.comment("Extra growth for plants under a lit Grow Lamp (1.0 = twice as fast). Stacks with greenhouses and other lamps").defineInRange("growth_boost", 1.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("electrolyzer");
        CFG_ELECTROLYZER_FE_PER_TICK    = BUILDER.comment("FE/t an Electrolyzer uses while running").defineInRange("fe_per_tick", 40, 1, 1_000_000);
        CFG_ELECTROLYZER_WATER_PER_TICK = BUILDER.comment("mB of Water split per tick while running. Makes the same amount of Hydrogen and half as much Oxygen").defineInRange("water_per_tick", 2, 1, 10_000);
        CFG_ELECTROLYZER_BUFFER         = BUILDER.comment("FE an Electrolyzer stores").defineInRange("buffer_capacity", 10_000, 100, 10_000_000);
        CFG_ELECTROLYZER_MAX_RECEIVE    = BUILDER.comment("Max FE per tick an Electrolyzer accepts").defineInRange("max_receive", 400, 1, 1_000_000);
        BUILDER.pop();

        BUILDER.push("solar_plant_bed");
        CFG_SOLAR_PLANT_BED_MORNING_FE   = BUILDER.comment("FE/t a Solar Plant Bed's panel makes at dawn and dusk").defineInRange("morning_fe_per_tick", 2, 0, 100_000);
        CFG_SOLAR_PLANT_BED_NOON_FE      = BUILDER.comment("FE/t a Solar Plant Bed's panel makes at noon (clear weather)").defineInRange("noon_fe_per_tick", 4, 0, 100_000);
        CFG_SOLAR_PLANT_BED_FE_PER_TICK  = BUILDER.comment("FE/t the grow light uses while lit at night").defineInRange("fe_per_tick", 2, 0, 100_000);
        CFG_SOLAR_PLANT_BED_BUFFER       = BUILDER.comment("FE a Solar Plant Bed stores. The default runs the light through a whole night").defineInRange("buffer_capacity", 24_000, 100, 10_000_000);
        CFG_SOLAR_PLANT_BED_GROWTH_BOOST = BUILDER.comment("Extra growth for the plant on a Solar Plant Bed while its light is on (1.0 = twice as fast)").defineInRange("growth_boost", 1.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("wires");
        CFG_WIRE_IRON_RATE       = BUILDER.comment("Max FE per tick through one Iron Wire").defineInRange("iron_fe_per_tick", 256, 1, 1_000_000_000);
        CFG_WIRE_IRON_LENGTH     = BUILDER.comment("Longest Iron Wire, in blocks").defineInRange("iron_max_length", 16, 1, 64);
        CFG_WIRE_COPPER_RATE     = BUILDER.comment("Max FE per tick through one Copper Wire").defineInRange("copper_fe_per_tick", 1024, 1, 1_000_000_000);
        CFG_WIRE_COPPER_LENGTH   = BUILDER.comment("Longest Copper Wire, in blocks").defineInRange("copper_max_length", 24, 1, 64);
        CFG_WIRE_GOLD_RATE       = BUILDER.comment("Max FE per tick through one Gold Wire").defineInRange("gold_fe_per_tick", 4096, 1, 1_000_000_000);
        CFG_WIRE_GOLD_LENGTH     = BUILDER.comment("Longest Gold Wire, in blocks").defineInRange("gold_max_length", 32, 1, 64);
        CFG_WIRE_BLOCKS_PER_WIRE = BUILDER.comment("Blocks of length one wire item covers. A spool holds 4 wires").defineInRange("blocks_per_wire", 8, 1, 64);
        CFG_WIRE_MAX_CONNECTIONS = BUILDER.comment("Most wires one Wire Connector can hold").defineInRange("max_connections", 8, 1, 64);
        BUILDER.pop();

        // Each knob is interpolated by blanket composition (blanketEfficiency: 0 = pure
        // Lithium, 1 = pure Beryllium), the same way blade efficiency scales the turbine.
        BUILDER.push("fusion_reactor");
        CFG_REACTOR_STEAM_PER_LAYER_PER_TICK = BUILDER.comment(
                "mB of steam produced per Blanket module per tick at pure-Beryllium (max output) composition.",
                "Default 4 with the reactor's fixed 42-module Blanket band means ~168 mB/t steam at pure Beryllium.")
                .defineInRange("reactor_steam_per_layer_per_tick", 4, 1, 10_000);
        CFG_REACTOR_LITHIUM_OUTPUT_RATIO = BUILDER.comment(
                "Steam output multiplier at pure-Lithium composition, relative to Beryllium's 1.0.",
                "Default 0.4 means pure Lithium produces 40% of the steam pure Beryllium does per module.")
                .defineInRange("reactor_lithium_output_ratio", 0.4, 0.01, 2.0);
        CFG_REACTOR_FUEL_PER_STEAM_MB = BUILDER.comment(
                "Combined Deuterium + Lithium mB consumed per mB of steam at the pure-Lithium (efficient) extreme.")
                .defineInRange("reactor_fuel_per_steam_mb", 2, 1, 10_000);
        CFG_REACTOR_BERYLLIUM_FUEL_WASTE_MULTIPLIER = BUILDER.comment(
                "Multiplies fuel-per-steam at the pure-Beryllium extreme. Default 3.0 means 6 mB combined",
                "fuel per mB steam there - Beryllium burns fuel faster AND produces more, compounding its",
                "'devour fuel, maximize output' identity.")
                .defineInRange("reactor_beryllium_fuel_waste_multiplier", 3.0, 1.0, 100.0);
        CFG_REACTOR_BREEDER_FLOOR_RATIO = BUILDER.comment(
                "Fraction of drawn Lithium that actually breeds into usable Tritium at pure-Beryllium",
                "composition (1.0 = no waste at pure-Lithium). Default 0.5 widens the Deuterium:Lithium",
                "draw ratio from 1:1 at pure-Lithium to 1:2 at pure-Beryllium.")
                .defineInRange("reactor_breeder_floor_ratio", 0.5, 0.01, 1.0);
        CFG_REACTOR_TRITIUM_CAPSULE_MB = BUILDER.comment(
                "mB of fuel one Tritium Capsule counts as (one Lithium Dust is 250 mB). Tritium burns 1:1",
                "with Deuterium with none of Lithium's breeding waste. Default 32000 is four times the 32",
                "Lithium Dust the Particle Accelerator's Tritium recipe uses.")
                .defineInRange("reactor_tritium_capsule_mb", 32000, 1, 1_000_000);
        CFG_REACTOR_BREEDER_CAPTURE_RATIO = BUILDER.comment(
                "Share of the Tritium the blanket breeds while burning Lithium that's captured into Empty",
                "Fuel Capsules in the reactor, scaled by the share of Lithium Breeder Modules. Default 0.02",
                "is a slow trickle, so the Particle Accelerator stays the main source of Tritium.")
                .defineInRange("reactor_breeder_capture_ratio", 0.02, 0.0, 1.0);
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
        CFG_ELECTROLYZER_TANK                = BUILDER.comment("Electrolyzer: Water, Hydrogen and Oxygen tank capacity (mB, each tank)").defineInRange("electrolyzer_mb", 4000, 100, 1_000_000);
        CFG_FUSION_REACTOR_STEAM_TANK        = BUILDER.comment("Fusion Reactor: steam output tank capacity (mB)").defineInRange("fusion_reactor_steam_mb", 16000, 100, 1_000_000);
        CFG_FUSION_REACTOR_DEUTERIUM_TANK    = BUILDER.comment("Fusion Reactor: Deuterium input tank capacity (mB)").defineInRange("fusion_reactor_deuterium_mb", 8000, 100, 1_000_000);
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
        CFG_LEAF_ABSORPTION_PER_INTERVAL = BUILDER.comment("Pollution absorbed per leaf block (or Moss Stairs/Slab) in a chunk each decay interval. 0 to disable tree absorption.").defineInRange("leaf_absorption_per_interval", 1, 0, 1_000_000);
        CFG_TRELLIS_ABSORPTION_PER_INTERVAL = BUILDER.comment("Pollution absorbed per Trellis with vines on it in a chunk each decay interval, counted near the surface like leaves. 0 to disable.").defineInRange("trellis_absorption_per_interval", 1, 0, 1_000_000);
        CFG_BIOFILTER_ABSORPTION_PER_RPM = BUILDER.comment(
                "Pollution units removed per second, per RPM of rotational speed driving the Biofilter.",
                "Scales with actual speed rather than a flat rate, so a cheap 16 RPM feed and a maxed-out",
                "network don't clean at the same rate for wildly different SU cost. Default 0.625 keeps a",
                "16 RPM Biofilter at the old flat-rate baseline (10/s) while a 256 RPM one reaches 160/s.")
                .defineInRange("biofilter_absorption_per_rpm", 0.625, 0.0, 1_000_000.0);
        CFG_BIOFILTER_RADIUS_BLOCKS = BUILDER.comment("Block radius around a Biofilter in which it reduces pollution (0 = own chunk only).").defineInRange("biofilter_radius_blocks", 32, 0, 512);
        CFG_BIOFILTER_POLLUTION_PER_CARBON = BUILDER.comment(
                "Pollution a Biofilter has to remove to collect one Carbon Dust (0 = no Carbon Dust).",
                "Four Carbon Dust compact into one Biochar. Kept high so polluting on purpose to farm it never pays off.")
                .defineInRange("biofilter_pollution_per_carbon_dust", 1000, 0, Integer.MAX_VALUE);
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
    public static double agrivoltaicsBonus;
    public static int agrivoltaicsRange;
    public static int shingleMorningFe, shingleNoonFe, shingleBuffer, shingleMaxExtract;
    public static int solarGlassMorningFe, solarGlassNoonFe, solarGlassPaneMorningFe, solarGlassPaneNoonFe, solarGlassBuffer, solarGlassMaxExtract;
    public static int geyserCapRpm, geyserCapSu;
    public static boolean geyserEruptions;
    public static int geyserMinIntervalTicks, geyserMaxIntervalTicks, geyserWarningTicks, geyserEruptionTicks, geyserHeight;
    public static float geyserDamage;
    public static int kineticBatteryRpm, kineticBatterySu, kineticBatteryMaxCharge;
    public static double kineticBatteryDischargeRate;
    public static int gasifierRpm, gasifierSu;
    public static int biofuelEngineRpm, biofuelEngineSu;
    public static int motorMaxRpm, motorSu, motorFePerRpm;
    public static int turbineMaxRpm, turbineRpmPerLayer, turbineSuPerLayer, turbineSteamPerLayerPerTick, turbineCondensateRatio;
    public static boolean turbineCrownsSteam;
    public static float turbineCrownsMinVaporQuality;
    public static int turbineSteamTank, turbineCondensateTank;
    public static double solarPowerTowerSaltMultiplier, solarPowerTowerSteamMultiplier;

    public static boolean sereneSeasonsEnabled;
    public static double sereneSeasonsSummerMultiplier, sereneSeasonsWinterMultiplier;

    public static boolean sunAngle;
    public static double spaceSolarMultiplier;
    public static boolean spaceRealSun;

    public static int solarHeaterMeltTicks, solarHeaterEvaporationTicks, solarHeaterWaterPerSalt;
    public static int fireboxBoilerSteamPerTick;
    public static int fermentationTicks, fermentationWaterPerBatch, fermentationBiofuelPerBatch, fermentationRichFertilizerPerBatch;
    public static int sprinklerRichExtraRange;
    public static int gasifierBurnTicks, pelletBurnTicks, pelletBiocharAmount;
    public static int biofuelConsumeMb, biofuelConsumePeriod;
    public static int heatBatteryMaxHeat, heatBatteryHeatPerMb, heatBatteryHeatDecay;
    public static int heatBatteryNewAgeMaxTemperature;
    public static double heatBatteryNewAgeHeatRate;
    public static int sprinklerFluidPerCycle, sprinklerRange;

    public static int reactorSteamPerLayerPerTick, reactorFuelPerSteamMb;
    public static double reactorLithiumOutputRatio, reactorBerylliumFuelWasteMultiplier, reactorBreederFloorRatio;
    public static int reactorTritiumCapsuleMb;
    public static double reactorBreederCaptureRatio;

    public static int heatBatteryTank, biofuelEngineTank;
    public static int fermentationVatTankPerBlock, solarPowerTowerTankPerBlock;
    public static int sprinklerTank, solarHeaterTank, fireboxBoilerTank, crystallizerTank;
    public static int fusionReactorSteamTank, fusionReactorDeuteriumTank;

    public static int fermentationVatMinWidth, fermentationVatMinHeight;
    public static int solarPowerTowerMinWidth, solarPowerTowerMinHeight;

    public static int solarPowerTowerMirrorBaseRadius, solarPowerTowerMirrorRadiusPerHeight;
    public static int solarPowerTowerMirrorMaxRadius, solarPowerTowerMaxTrackedMirrors;

    public static int geyserSpawnChance;
    public static List<? extends String> geyserBiomes;

    public static boolean globalWarmingEnabled;
    public static int pollutionPerSource, pollutionRadiusBlocks, pollutionDecayRate;
    public static int greenhouseScanInterval, greenhouseMaxVolume;
    public static double greenhouseGrowthBoost, greenhousePollinationBonus;
    public static int leafAbsorptionPerInterval, trellisAbsorptionPerInterval, biofilterSu, biofilterRadiusBlocks, biofilterPollutionPerCarbon;
    public static double biofilterAbsorptionPerRpm;
    public static int streetlampFePerTick, streetlampBuffer, streetlampMaxReceive;
    public static int growLampFePerTick, growLampBuffer, growLampMaxReceive, growLampRange;
    public static double growLampGrowthBoost;
    public static int electrolyzerFePerTick, electrolyzerWaterPerTick, electrolyzerBuffer, electrolyzerMaxReceive, electrolyzerTank;
    public static int solarPlantBedMorningFe, solarPlantBedNoonFe, solarPlantBedFePerTick, solarPlantBedBuffer;
    public static double solarPlantBedGrowthBoost;
    public static int wireIronRate, wireIronLength, wireCopperRate, wireCopperLength, wireGoldRate, wireGoldLength;
    public static int wireBlocksPerWire, wireMaxConnections;
    public static int biomeDecayThreshold, biomeDecayInterval, blocksDecayedPerInterval;
    public static String deadBiome;
    public static Map<String, Integer> perBlockPollution = new HashMap<>();
    public static List<? extends String> autoDetectKeywords = new ArrayList<>();
    public static int autoDetectPollution;
    public static int aeronauticsEnginePollution;
    public static Set<String> pollutionBlacklist = new HashSet<>();

    static void onLoad(final ModConfigEvent event) {
        andesiteMorningRpm   = CFG_ANDESITE_MORNING_RPM.get();
        andesiteNoonRpm      = CFG_ANDESITE_NOON_RPM.get();
        andesiteMorningSu    = CFG_ANDESITE_MORNING_SU.get();
        andesiteNoonSu       = CFG_ANDESITE_NOON_SU.get();

        brassMorningFe       = CFG_BRASS_MORNING_FE.get();
        brassNoonFe          = CFG_BRASS_NOON_FE.get();
        brassBuffer          = CFG_BRASS_BUFFER.get();
        agrivoltaicsBonus = CFG_AGRIVOLTAICS_BONUS.get();
        agrivoltaicsRange = CFG_AGRIVOLTAICS_RANGE.get();
        brassMaxExtract      = CFG_BRASS_MAX_EXTRACT.get();
        shingleMorningFe     = CFG_SHINGLE_MORNING_FE.get();
        shingleNoonFe        = CFG_SHINGLE_NOON_FE.get();
        shingleBuffer        = CFG_SHINGLE_BUFFER.get();
        shingleMaxExtract    = CFG_SHINGLE_MAX_EXTRACT.get();
        solarGlassMorningFe     = CFG_SOLAR_GLASS_MORNING_FE.get();
        solarGlassNoonFe        = CFG_SOLAR_GLASS_NOON_FE.get();
        solarGlassPaneMorningFe = CFG_SOLAR_GLASS_PANE_MORNING_FE.get();
        solarGlassPaneNoonFe    = CFG_SOLAR_GLASS_PANE_NOON_FE.get();
        solarGlassBuffer        = CFG_SOLAR_GLASS_BUFFER.get();
        solarGlassMaxExtract    = CFG_SOLAR_GLASS_MAX_EXTRACT.get();

        geyserCapRpm         = CFG_GEYSER_RPM.get();
        geyserCapSu          = CFG_GEYSER_SU.get();

        geyserEruptions        = CFG_GEYSER_ERUPTIONS.get();
        geyserMinIntervalTicks = CFG_GEYSER_MIN_INTERVAL.get() * 20;
        geyserMaxIntervalTicks = Math.max(geyserMinIntervalTicks, CFG_GEYSER_MAX_INTERVAL.get() * 20);
        geyserWarningTicks     = CFG_GEYSER_WARNING.get() * 20;
        geyserEruptionTicks    = CFG_GEYSER_ERUPTION.get() * 20;
        geyserDamage           = CFG_GEYSER_DAMAGE.get().floatValue();
        geyserHeight           = CFG_GEYSER_HEIGHT.get();

        kineticBatteryRpm          = CFG_KINETIC_BATTERY_RPM.get();
        kineticBatterySu           = CFG_KINETIC_BATTERY_SU.get();
        kineticBatteryMaxCharge    = CFG_KINETIC_BATTERY_MAX_CHARGE.get();
        kineticBatteryDischargeRate = CFG_KINETIC_BATTERY_DISCHARGE_RATE.get();

        gasifierRpm          = CFG_GASIFIER_RPM.get();
        gasifierSu           = CFG_GASIFIER_SU.get();

        biofuelEngineRpm     = CFG_BIOFUEL_ENGINE_RPM.get();
        biofuelEngineSu      = CFG_BIOFUEL_ENGINE_SU.get();
        motorMaxRpm          = CFG_MOTOR_MAX_RPM.get();
        motorSu              = CFG_MOTOR_SU.get();
        motorFePerRpm        = CFG_MOTOR_FE_PER_RPM.get();

        turbineMaxRpm               = CFG_TURBINE_MAX_RPM.get();
        turbineRpmPerLayer          = CFG_TURBINE_RPM_PER_LAYER.get();
        turbineSuPerLayer           = CFG_TURBINE_SU_PER_LAYER.get();
        turbineSteamPerLayerPerTick = CFG_TURBINE_STEAM_PER_LAYER_PER_TICK.get();
        turbineCondensateRatio      = CFG_TURBINE_CONDENSATE_RATIO.get();
        turbineCrownsSteam          = CFG_TURBINE_CROWNS_STEAM.get();
        turbineCrownsMinVaporQuality = CFG_TURBINE_CROWNS_MIN_VAPOR_QUALITY.get().floatValue();

        solarPowerTowerSaltMultiplier  = CFG_SOLAR_POWER_TOWER_SALT_MULTIPLIER.get();
        solarPowerTowerSteamMultiplier = CFG_SOLAR_POWER_TOWER_STEAM_MULTIPLIER.get();

        sereneSeasonsEnabled          = CFG_SERENE_SEASONS_ENABLED.get();
        sereneSeasonsSummerMultiplier = CFG_SERENE_SEASONS_SUMMER_MULTIPLIER.get();
        sereneSeasonsWinterMultiplier = CFG_SERENE_SEASONS_WINTER_MULTIPLIER.get();

        sunAngle             = CFG_SUN_ANGLE.get();
        spaceSolarMultiplier = CFG_SPACE_SOLAR_MULTIPLIER.get();
        spaceRealSun         = CFG_SPACE_REAL_SUN.get();

        solarHeaterMeltTicks       = CFG_SOLAR_HEATER_MELT_TICKS.get();
        solarHeaterEvaporationTicks = CFG_SOLAR_HEATER_EVAPORATION_TICKS.get();
        solarHeaterWaterPerSalt    = CFG_SOLAR_HEATER_WATER_PER_SALT.get();

        fireboxBoilerSteamPerTick = CFG_FIREBOX_BOILER_STEAM_PER_TICK.get();

        fermentationTicks          = CFG_FERMENTATION_TICKS.get();
        fermentationWaterPerBatch  = CFG_FERMENTATION_WATER_PER_BATCH.get();
        fermentationBiofuelPerBatch = CFG_FERMENTATION_BIOFUEL_PER_BATCH.get();
        fermentationRichFertilizerPerBatch = CFG_FERMENTATION_RICH_FERTILIZER_PER_BATCH.get();

        gasifierBurnTicks    = CFG_GASIFIER_BURN_TICKS.get();
        pelletBurnTicks      = CFG_PELLET_BURN_TICKS.get();
        pelletBiocharAmount  = CFG_PELLET_BIOCHAR_AMOUNT.get();

        biofuelConsumeMb     = CFG_BIOFUEL_CONSUME_MB.get();
        biofuelConsumePeriod = CFG_BIOFUEL_CONSUME_PERIOD.get();

        heatBatteryMaxHeat   = CFG_HEAT_BATTERY_MAX_HEAT.get();
        heatBatteryHeatPerMb = CFG_HEAT_BATTERY_HEAT_PER_MB.get();
        heatBatteryHeatDecay = CFG_HEAT_BATTERY_HEAT_DECAY.get();
        heatBatteryNewAgeMaxTemperature = CFG_HEAT_BATTERY_NEW_AGE_MAX_TEMPERATURE.get();
        heatBatteryNewAgeHeatRate = CFG_HEAT_BATTERY_NEW_AGE_HEAT_RATE.get();

        sprinklerFluidPerCycle = CFG_SPRINKLER_FLUID_PER_CYCLE.get();
        sprinklerRange         = CFG_SPRINKLER_RANGE.get();
        sprinklerRichExtraRange = CFG_SPRINKLER_RICH_EXTRA_RANGE.get();

        reactorSteamPerLayerPerTick          = CFG_REACTOR_STEAM_PER_LAYER_PER_TICK.get();
        reactorLithiumOutputRatio            = CFG_REACTOR_LITHIUM_OUTPUT_RATIO.get();
        reactorFuelPerSteamMb                = CFG_REACTOR_FUEL_PER_STEAM_MB.get();
        reactorBerylliumFuelWasteMultiplier  = CFG_REACTOR_BERYLLIUM_FUEL_WASTE_MULTIPLIER.get();
        reactorBreederFloorRatio             = CFG_REACTOR_BREEDER_FLOOR_RATIO.get();
        reactorTritiumCapsuleMb              = CFG_REACTOR_TRITIUM_CAPSULE_MB.get();
        reactorBreederCaptureRatio           = CFG_REACTOR_BREEDER_CAPTURE_RATIO.get();

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
        fusionReactorSteamTank       = CFG_FUSION_REACTOR_STEAM_TANK.get();
        fusionReactorDeuteriumTank   = CFG_FUSION_REACTOR_DEUTERIUM_TANK.get();

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
        trellisAbsorptionPerInterval    = CFG_TRELLIS_ABSORPTION_PER_INTERVAL.get();
        biofilterSu                     = CFG_BIOFILTER_SU.get();
        streetlampFePerTick             = CFG_STREETLAMP_FE_PER_TICK.get();
        streetlampBuffer                = CFG_STREETLAMP_BUFFER.get();
        streetlampMaxReceive            = CFG_STREETLAMP_MAX_RECEIVE.get();
        growLampFePerTick               = CFG_GROW_LAMP_FE_PER_TICK.get();
        growLampBuffer                  = CFG_GROW_LAMP_BUFFER.get();
        growLampMaxReceive              = CFG_GROW_LAMP_MAX_RECEIVE.get();
        growLampRange                   = CFG_GROW_LAMP_RANGE.get();
        growLampGrowthBoost             = CFG_GROW_LAMP_GROWTH_BOOST.get();
        electrolyzerFePerTick           = CFG_ELECTROLYZER_FE_PER_TICK.get();
        electrolyzerWaterPerTick        = CFG_ELECTROLYZER_WATER_PER_TICK.get();
        electrolyzerBuffer              = CFG_ELECTROLYZER_BUFFER.get();
        electrolyzerMaxReceive          = CFG_ELECTROLYZER_MAX_RECEIVE.get();
        electrolyzerTank                = CFG_ELECTROLYZER_TANK.get();
        solarPlantBedMorningFe               = CFG_SOLAR_PLANT_BED_MORNING_FE.get();
        solarPlantBedNoonFe                  = CFG_SOLAR_PLANT_BED_NOON_FE.get();
        solarPlantBedFePerTick               = CFG_SOLAR_PLANT_BED_FE_PER_TICK.get();
        solarPlantBedBuffer                  = CFG_SOLAR_PLANT_BED_BUFFER.get();
        solarPlantBedGrowthBoost             = CFG_SOLAR_PLANT_BED_GROWTH_BOOST.get();
        wireIronRate                    = CFG_WIRE_IRON_RATE.get();
        wireIronLength                  = CFG_WIRE_IRON_LENGTH.get();
        wireCopperRate                  = CFG_WIRE_COPPER_RATE.get();
        wireCopperLength                = CFG_WIRE_COPPER_LENGTH.get();
        wireGoldRate                    = CFG_WIRE_GOLD_RATE.get();
        wireGoldLength                  = CFG_WIRE_GOLD_LENGTH.get();
        wireBlocksPerWire               = CFG_WIRE_BLOCKS_PER_WIRE.get();
        wireMaxConnections              = CFG_WIRE_MAX_CONNECTIONS.get();
        greenhouseScanInterval          = CFG_GREENHOUSE_SCAN_INTERVAL.get();
        greenhouseMaxVolume             = CFG_GREENHOUSE_MAX_VOLUME.get();
        greenhouseGrowthBoost           = CFG_GREENHOUSE_GROWTH_BOOST.get();
        greenhousePollinationBonus      = CFG_GREENHOUSE_POLLINATION_BONUS.get();
        biofilterAbsorptionPerRpm       = CFG_BIOFILTER_ABSORPTION_PER_RPM.get();
        biofilterRadiusBlocks           = CFG_BIOFILTER_RADIUS_BLOCKS.get();
        biofilterPollutionPerCarbon     = CFG_BIOFILTER_POLLUTION_PER_CARBON.get();
        biomeDecayThreshold       = CFG_BIOME_DECAY_THRESHOLD.get();
        biomeDecayInterval        = CFG_BIOME_DECAY_INTERVAL.get();
        blocksDecayedPerInterval  = CFG_BLOCKS_DECAYED_PER_INTERVAL.get();
        deadBiome                 = CFG_DEAD_BIOME.get();

        perBlockPollution = new HashMap<>();
        for (String entry : CFG_PER_BLOCK_POLLUTION.get()) {
            int sep = entry.lastIndexOf('=');
            if (sep < 0) continue;
            try {
                perBlockPollution.put(entry.substring(0, sep), Integer.parseInt(entry.substring(sep + 1)));
            } catch (NumberFormatException ignored) {}
        }

        autoDetectKeywords = CFG_AUTO_DETECT_KEYWORDS.get();
        autoDetectPollution = CFG_AUTO_DETECT_POLLUTION.get();
        pollutionBlacklist = new HashSet<>(CFG_POLLUTION_BLACKLIST.get());
        GlobalWarmingHandler.invalidateAutoDetectedCache();

        aeronauticsEnginePollution = CFG_AERONAUTICS_ENGINE_POLLUTION.get();
        for (DyeColor colour : DyeColor.values())
            perBlockPollution.put("simulated:" + colour.getName() + "_portable_engine", aeronauticsEnginePollution);
    }
}
