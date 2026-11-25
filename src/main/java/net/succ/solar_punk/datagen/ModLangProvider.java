package net.succ.solar_punk.datagen;

import net.succ.offbyonelib.datagen.AddonLangProvider;
import net.succ.offbyonelib.datagen.PonderLang;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;
import net.succ.solar_punk.fluid.ModFluidTypes;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.painting.ModPaintings;
import net.succ.solar_punk.painting.PaintingInfo;

public class ModLangProvider extends AddonLangProvider {
    public ModLangProvider(PackOutput output) {
        super(output, SolarPunk.MODID, SolarPunk.GOGGLES);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.solar_punk", "Create: Solarpunk");
        add("itemGroup.solar_punk.building_blocks", "Create: Solarpunk Building Blocks");
        jei("category.solar_heating", "Solar Heating");
        jei("category.solar_power_tower", "Solar Power Tower");
        jei("solar_power_tower.water_note", "On a %sx%s tower. Each mB made costs %s mB of Water per footprint block");
        jei("category.fermentation_vat", "Fermentation Vat");
        jei("category.bioreactor", "Bioreactor");
        jei("bioreactor.seed_note", "Used once to start the culture");
        jei("bioreactor.optional", "Optional: speeds it up");
        jei("bioreactor.culture_note", "Grows from the Water in sunlight; can be piped out to start another Bioreactor");
        jei("bioreactor.rate", "%s/min per top block in sun");
        jei("bioreactor.boost", "Fertilizer x%s, Rich x%s");
        jei("category.crystallizing", "Crystallizer");
        jei("category.particle_accelerator", "Particle Accelerator");
        jei("category.electrolysis", "Electrolysis");
        jei("category.fuel_cell", "Fuel Cell");
        jei("energy_used", "Uses %s");
        jei("energy_made", "Makes %s");
        jei("particle_accelerator.diameter", "Ring diameter: %s+");
        jei("particle_accelerator.stress", "Stress: %s SU (at min diameter)");
        jei("particle_accelerator.speed", "Min speed: %s RPM");

        // Sound subtitles - format: subtitles.solarpunk.<sound event path>, referenced by
        // sounds.json. One entry per SoundEvent registered in ModSounds.
        subtitle("brass_solar_panel_hum", "Solar panel hums");
        subtitle("geyser_puff", "Geyser puffs");
        subtitle("particle_accelerator_hum", "Particle Accelerator hums");
        subtitle("particle_collision", "Particles collide");
        subtitle("biofuel_engine_loop", "Biofuel Engine running");
        subtitle("biofilter_loop", "Biofilter filtering");
        subtitle("biomass_gasifier_loop", "Biomass Gasifier burning");
        subtitle("fermentation_vat_bubbling", "Fermentation Vat bubbles");
        subtitle("bioreactor_hum", "Bioreactor hums");
        subtitle("bioreactor_bubbling", "Bioreactor bubbles");
        subtitle("bioreactor_vent", "Bioreactor vents");
        subtitle("solar_heater_shimmer", "Solar Heater shimmers");
        subtitle("steam_hiss", "Steam hisses");
        subtitle("turbine_loop", "Steam Turbine spins");
        subtitle("fusion_reactor_startup", "Fusion Reactor powers up");
        subtitle("fusion_reactor_shutdown", "Fusion Reactor powers down");
        subtitle("reactor_on_loop", "Fusion Reactor hums");

        // Ponder tags and scene text come straight from SolarPunkPonderTags and the scene
        // classes. Ponder normally registers its plugins during client setup, which doesn't
        // run during datagen, so register ours here first (same as Create's datagen).
        PonderLang.provide(new SolarPunkPonderPlugin(), SolarPunk.MODID, this::add);

        goggle("biofilter_header", "Biofilter");
        goggle("biofilter_status", "Status: ");
        goggle("biofilter_pollution", "Chunk Pollution: ");
        goggle("biofilter_removing", "Removing: ");
        goggle("biofilter_carbon", "Carbon Dust: ");

        goggle("geyser_vent_header", "Geyser Vent");
        goggle("geyser_vent_strength", "Strength: ");
        goggle("geyser_vent_strength_weak", "Weak");
        goggle("geyser_vent_strength_normal", "Normal");
        goggle("geyser_vent_strength_strong", "Strong");
        goggle("geyser_well_header", "Geyser Well");
        goggle("geyser_well_no_vent", "Not on a Geyser Vent");
        goggle("geyser_vent_next", "Next eruption: ");
        goggle("geyser_vent_warning", "Any second now!");
        goggle("geyser_vent_erupting", "Erupting!");
        goggle("geyser_vent_quiet", "None");

        add("death.attack.solarpunk.scalding", "%1$s was scalded by a geyser");
        add("death.attack.solarpunk.scalding.player", "%1$s was scalded by a geyser while fighting %2$s");

        goggle("accelerator_header", "Particle Accelerator");
        goggle("accelerator_ring", "Ring: ");
        goggle("accelerator_diameter", "Diameter: ");
        goggle("accelerator_recipe", "Recipe: ");
        goggle("accelerator_progress", "Progress: ");
        goggle("accelerator_too_small", "Ring too small: needs %1$s+ wide");
        goggle("accelerator_too_slow", "Too slow: needs %1$s RPM");
        goggle("accelerator_overstressed", "Overstressed");

        goggle("greenhouse_header", "Greenhouse");
        goggle("streetlamp_header", "Streetlamp");
        goggle("wire_connector_header", "Wire Connector");
        goggle("wires", "Wires: ");
        goggle("wire_switch_header", "Wire Switch");
        goggle("electric_motor_header", "Electric Motor");
        goggle("fe_input", "Input: ");
        goggle("electric_motor_sustains", "Not enough FE: holds up to %1$s RPM");
        add("solarpunk.display_source.electric_motor_consumption", "Electric Motor FE Use");
        add("solarpunk.display_source.wire_connector_throughput", "Wire Throughput");
        goggle("electrolyzer_header", "Electrolyzer");
        goggle("hydrogen", "Hydrogen: ");
        goggle("oxygen", "Oxygen: ");
        goggle("oxygen_boost", "Oxygen boost: ");
        goggle("fuel_cell_header", "Fuel Cell Stack");
        goggle("fuel_cell_cells", "Cells: ");
        goggle("output", "Output: ");
        goggle("fuel_cell_no_cells", "No Fuel Cells behind this End Plate");
        goggle("fuel_cell_no_plate", "Needs an End Plate at the other end");
        goggle("fuel_cell_too_long", "Too long: at most %1$s Fuel Cells");
        goggle("redstone_wire_relay_header", "Redstone Wire Relay");
        goggle("switch_state", "State: ");
        goggle("switch_closed", "Closed");
        goggle("switch_open", "Open");
        goggle("relay_closes", "Closed while: ");
        goggle("relay_powered", "Powered");
        goggle("relay_unpowered", "Unpowered");
        goggle("grow_lamp_header", "Grow Lamp");
        goggle("grow_lamp_plants", "Plants: ");
        goggle("solar_plant_bed_header", "Solar Plant Bed");
        goggle("throughput", "Throughput: ");

        add("message.solarpunk.wire.started", "Wire started. Click another Wire Connector or switch post to finish it");
        add("message.solarpunk.wire.cancelled", "Wire cancelled");
        add("message.solarpunk.wire.start_missing", "The connector this wire started from is gone");
        add("message.solarpunk.wire.already_connected", "These are already wired together");
        add("message.solarpunk.wire.full", "That can't hold any more wires");
        add("message.solarpunk.wire.too_long", "Too long: this wire reaches at most %s blocks");
        add("message.solarpunk.wire.not_enough", "Not enough wire on the spool: needs %s");
        add("item.solarpunk.spool.wires_left", "%s wires left (%s blocks)");
        add("item.solarpunk.spool.stats", "Carries %s, up to %s blocks long");
        add("item.solarpunk.spool.started", "Wire started at %s, %s, %s");
        goggle("greenhouse_status", "Status: ");
        goggle("greenhouse_sealed", "Sealed");
        goggle("greenhouse_not_sealed", "Not sealed, or too big");
        goggle("greenhouse_front_blocked", "Front is blocked");
        goggle("greenhouse_other_controller", "Another controller runs this greenhouse");
        goggle("greenhouse_volume", "Space: ");
        goggle("greenhouse_crops", "Plants under glass: ");
        goggle("greenhouse_boost", "Growth speed: ");
        goggle("greenhouse_bees", "Bees pollinating: ");
        goggle("sprinkler_header", "Sprinkler");
        goggle("sprinkler_status", "Status: ");

        goggle("fe_header", "Generator Stats");
        goggle("generating", "Generating: ");
        goggle("agrivoltaics", "Cooled by plants below: +%s%%");
        goggle("stored", "Stored: ");

        goggle("kinetic_battery_header", "Kinetic Battery");
        goggle("charge", "Charge:");
        goggle("runtime", "Runtime:");

        goggle("heat_battery_header", "Heat Battery");
        goggle("heat_level", "Level: ");
        goggle("heat_stored", "Heat: ");
        goggle("new_age_temperature", "New Age Temperature: ");
        goggle("molten_salt", "Molten Salt: ");
        goggle("steam", "Steam: ");
        goggle("tower_mode", "Mode: ");
        add("solarpunk.tooltip.tower_mode_salt", "Molten Salt");
        add("solarpunk.tooltip.tower_mode_steam", "Steam");

        add("solarpunk.display_source.heat_battery_charge", "Heat Battery Charge");
        add("solarpunk.display_source.heat_battery_status", "Heat Battery Status");
        add("solarpunk.display_source.kinetic_battery_charge", "Kinetic Battery Charge");
        add("solarpunk.display_source.solar_power_tower_efficiency", "Solar Power Tower Efficiency");
        add("solarpunk.display_source.solar_power_tower_mirror_count", "Solar Power Tower Mirror Count");
        add("solarpunk.display_source.solar_power_tower_generation_rate", "Solar Power Tower Generation Rate");
        add("solarpunk.display_source.fusion_reactor_lithium_buffer", "Fusion Reactor Lithium Supply");
        add("solarpunk.display_source.fusion_reactor_status", "Fusion Reactor Status");
        add("solarpunk.display_source.fusion_reactor_steam_output", "Fusion Reactor Steam Output");
        add("solarpunk.display_source.fusion_reactor_throttle", "Fusion Reactor Throttle");
        add("solarpunk.display_source.fusion_reactor_deuterium", "Fusion Reactor Deuterium");
        add("solarpunk.display_source.fusion_reactor_steam_tank", "Fusion Reactor Steam Tank");
        add("solarpunk.display_source.fusion_reactor_tritium_supply", "Fusion Reactor Tritium Supply");
        add("solarpunk.display_source.solar_panel_sun_angle", "Sun Angle");
        add("solarpunk.display_source.solar_panel_sun_offset", "Sun Offset");
        add("solarpunk.display_source.solar_panel_sun_exposure", "Sun Exposure");
        add("solarpunk.display_source.biofilter_pollution", "Chunk Pollution");
        add("solarpunk.display_source.biofilter_pollution_level", "Pollution Level");
        add("solarpunk.display_source.particle_accelerator_status", "Particle Accelerator Status");
        add("solarpunk.display_source.particle_accelerator_recipe", "Particle Accelerator Recipe");
        add("solarpunk.display_source.particle_accelerator_progress", "Particle Accelerator Progress");
        add("solarpunk.display_source.bioreactor_biomass_rate", "Biomass per Minute");
        add("solarpunk.display_source.bioreactor_light", "Light Level");
        add("solarpunk.display_source.bioreactor_culture", "Culture Level");
        add("solarpunk.display_source.greenhouse_status", "Greenhouse Status");
        add("solarpunk.display_source.greenhouse_plant_count", "Greenhouse Plant Count");
        add("solarpunk.display_source.greenhouse_growth", "Greenhouse Growth Speed");

        goggle("bioreactor_header", "Bioreactor");
        goggle("bioreactor_size", "Size: ");
        goggle("bioreactor_no_base", "Needs Bioreactor Base blocks under the whole tank");
        goggle("bioreactor_no_sun", "No light: needs open sky or Grow Lamps");
        goggle("bioreactor_sunlight", "Sunlight: ");
        goggle("bioreactor_lamplight", "Grow Lamp light: ");
        goggle("bioreactor_grow_lamps", "Grow Lamps: ");
        goggle("bioreactor_no_culture", "Add Kelp or Seagrass to start a culture");
        goggle("bioreactor_biomass_rate", "Biomass: ");
        goggle("bioreactor_full", "Biomass full: take it out of the Base");
        goggle("bioreactor_venting", "Oxygen tank full: venting the extra");
        goggle("bioreactor_absorbing", "Absorbing pollution: ");
        goggle("algae_culture", "Algae Culture: ");
        goggle("nutrients", "Nutrients: ");
        goggle("fermentation_vat_header", "Fermentation Vat");
        goggle("vat_too_small", "Needs at least 2x2 footprint");
        goggle("vat_too_short", "Needs at least 4 blocks tall");
        goggle("vat_batch_scale", "Batch size: ");
        goggle("vat_biofuel_output", "Output: ");
        goggle("vat_aerobic", "Aerobic: ");
        goggle("fermenting", "Fermenting: ");
        goggle("biofuel", "Biofuel: ");

        goggle("gasifier_header", "Biomass Gasifier");
        goggle("fuel", "Fuel: ");
        goggle("burn_time", "Burn time: ");
        goggle("biomass_stored", "Biomass: ");
        goggle("biochar_stored", "Biochar: ");

        goggle("biofuel_engine_header", "Biofuel Engine");
        goggle("consumption", "Consumption: ");

        goggle("steam_turbine_header", "Steam Turbine");
        goggle("turbine_invalid", "Structure invalid - check build");
        goggle("turbine_height", "Height: ");
        goggle("turbine_blades", "Blades: ");
        goggle("turbine_fuel", "Fuel: ");
        goggle("steam_supply", "Steam Supply: ");
        goggle("condensate_full", "Condensate full - drain water to restart");

        goggle("fusion_reactor_core_header", "Fusion Reactor Core");
        goggle("reactor_status", "Status: ");
        goggle("reactor_blanket", "Blanket: ");
        goggle("reactor_steam_output", "Steam Output: ");
        goggle("reactor_fuel_draw", "Fuel Draw: ");
        goggle("reactor_breeding_efficiency", "Breeding Efficiency: ");

        goggle("solar_power_tower_header", "Solar Power Tower");
        goggle("mirrors", "Mirrors: ");
        goggle("efficiency", "Efficiency: ");

        goggle("firebox_boiler_header", "Firebox Boiler");

        goggle("heater_header", "Solar Heater");
        goggle("melting", "Melting: ");
        goggle("progress", "Progress: ");
        goggle("water", "Water: ");
        goggle("fertilizer", "Fertilizer: ");
        goggle("rich_fertilizer", "Rich Fertilizer: ");
        goggle("evaporation", "Evaporation: ");
        goggle("output_fluid", "Output: ");
        goggle("salt_output", "Salt: ");

        goggle("crystallizer_header", "Crystallizer");
        goggle("crystallizer_progress", "Progress: ");
        goggle("crystallizer_input_a", "Input A: ");
        goggle("crystallizer_input_b", "Input B: ");
        goggle("crystallizer_byproduct", "Byproduct: ");
        goggle("crystallizer_item_output", "Output: ");

        // Inventory hover tooltips
        tooltip(ModBlocks.SOLAR_HEATER, "Uses _sunlight_ to melt items into fluid and passively evaporate water into _Salt_.",
                "During the day with a clear sky", "Melts the loaded item and evaporates stored water. Runs at half speed in _rain_ and stops in _thunderstorms_.");

        tooltip(ModBlocks.CRYSTALLIZER, "Runs data-driven recipes that turn up to two fluids into an item, plus an optional fluid byproduct. By default, quenches _Molten Salt_ with _Water_ into _Salt_ and some _Steam_ - the fast/bulk counterpart to the Solar Heater's slow evaporation trickle.",
                "Any time - no sunlight needed", "Pipe both input fluids in from any face; collect the result from the block or a hopper below, and any byproduct fluid from any face.");

        tooltip(ModBlocks.FIREBOX_BOILER, "Burns furnace fuel to boil water into _Steam_ for the _Steam Turbine_. A cheap way to get a turbine running before building a Solar Power Tower - nowhere near as efficient at scale.",
                "While lit", "Converts water to Steam at a fixed rate. Insert fuel by hand or hopper; pipe water in and Steam out of any face.");

        tooltip(ModBlocks.ANDESITE_SOLAR_PANEL, "Generates _Rotational Force_ from sunlight. Output scales with sun angle and weather.",
                "During the day with a clear sky", "8 RPM with 1024 SU at dawn and dusk, 16 RPM with 4096 SU at noon. _Rain_ drops it to the dawn rate and it stops at _night_.",
                "Raised over plants", "Crops, flowers or saplings up to _4 blocks_ below cool the panel for _10%_ more output, and still grow at normal speed in its shade.");

        tooltip(ModBlocks.BRASS_SOLAR_PANEL, "Generates _Forge Energy_ from sunlight. Output scales with sun angle and weather.",
                "During the day with a clear sky", "40 FE/t at dawn and dusk, 80 FE/t at noon. _Rain_ drops it to the dawn rate and it stops at _night_.",
                "Raised over plants", "Crops, flowers or saplings up to _4 blocks_ below cool the panel for _10%_ more output, and still grow at normal speed in its shade.");

        tooltip(ModBlocks.HEAT_BATTERY, "Stores thermal energy as _Molten Salt_ and supplies heat to a _Boiler_ placed above it.",
                "When filled with Molten Salt", "Heats the boiler above, _superheating_ it while at least a quarter charged. Stored heat slowly fades.",
                "With Create: New Age", "Trades heat with touching _heat pipes_ and machines. A charged battery heats them, and an emptier one soaks up their heat.");

        tooltip(ModBlocks.KINETIC_BATTERY, "Stores _Rotational Force_ for later use. Useful for buffering intermittent generators like solar panels.",
                "When connected to a spinning network", "Charges up while _no Redstone_ signal is applied, using _Stress_ like a machine. Faster networks charge it faster.",
                "When a Redstone signal is applied", "Discharges at 16 RPM with 256 SU of stress capacity until empty.");

        tooltip(ModBlocks.FERMENTATION_VAT, "Ferments _Biomass_ and water into _Biofuel_, or mob and farm _waste_ into _Rich Fertilizer_. Requires at least a _2x2 footprint_ and _4 blocks tall_ to operate.",
                "Larger footprint", "Increases batch size and output. A 3x3 footprint processes 9 Biomass per batch. Taller vats produce super-linearly more Biofuel per batch.",
                "With Oxygen piped in", "_Rich Fertilizer_ batches run _twice as fast_, since composting needs air. _Biofuel_ is made without air, so the Vat only takes Oxygen while it holds _waste_.");

        tooltip(ModBlocks.BIOREACTOR, "A glass tank of _Algae Culture_ that turns _Water_ and _sunlight_ into _Biomass_ and _Oxygen_. Up to _3x3_ wide and _8_ tall, standing on _Bioreactor Base_ blocks.",
                "To start", "Fill it with _Water_ and add _Kelp_ or _Seagrass_. The culture spreads through the Water in sunlight. Wider tanks have more surface, so they make more.",
                "With Fertilizer", "_Fertilizer_ or _Rich Fertilizer_ piped into the Base speeds it up, and lit _Grow Lamps_ beside or above the tank keep it going at night. While running it also absorbs _pollution_ in its chunk.");
        tooltip(ModBlocks.BIOREACTOR_BASE, "The aerator layer under a _Bioreactor_, with the same footprint as the tank. Takes _Water_, _Fertilizer_ and _Kelp_, and gives _Biomass_ and _Oxygen_ out of any side.");

        tooltip(ModBlocks.BIOMASS_GASIFIER, "Burns _Biomass_ to generate _Rotational Force_. Produces _Biochar_ as a byproduct.",
                "While burning", "Outputs 8 RPM with 2048 SU of stress capacity downward.",
                "With Oxygen piped in", "Burns hotter for _50% more stress capacity_, using a little _Oxygen_ every second.");

        tooltip(ModBlocks.BIOFUEL_ENGINE, "Burns _Biofuel_ to generate _Rotational Force_. Pipe fuel into any face.",
                "While fuelled", "Outputs 16 RPM with 8192 SU of stress capacity downward.",
                "With Oxygen piped in", "Burns hotter for _50% more stress capacity_, using a little _Oxygen_ every second.");

        tooltip(ModBlocks.GEYSER_CAP, "Harnesses the energy of a _Geyser Vent_ for passive, round-the-clock _Rotational Force_.",
                "When placed on a Geyser Vent", "Generates 32 RPM with 16384 SU of stress capacity on a normal vent. Works day and night in all weather.",
                "Vent strength", "A _weak_ vent gives half the stress capacity, a _strong_ one twice as much, and a _Nether_ vent twice again. RPM stays the same.");

        tooltip(ModBlocks.GEYSER_WELL, "Sits on a natural _Geyser Vent_ like a Geyser Cap, but boils _Water_ into _Steam_ for a _Steam Turbine_ instead of turning a shaft. More power than a Cap, if you build the turbine.",
                "When placed on a Geyser Vent", "Pipe Water into either _port_ and pump Steam out of either. Makes _4 mB_ of Steam per tick on a normal vent, half on a weak one, twice on a strong one, and twice again on a Nether vent. Stops the vent erupting.",
                "Gauges", "Both gauges show how _strong_ the vent below is.");

        tooltip(ModBlocks.RUSTED_GEYSER_CAP, "An old _Geyser Cap_ seized up with rust, left on some Geyser Vents. It makes no power, but still stops the vent erupting.",
                "When scraped with an Axe", "The rust comes off and it turns back into a working _Geyser Cap_.");

        tooltip(ModBlocks.GEYSER_VENT, "A rare natural vent. Left uncapped, it _erupts_ every few minutes, scalding and launching anything standing over it.",
                "Before an eruption", "Rumbles for a few seconds first. _Goggles_ show the countdown, and a _Comparator_ signal rises as it gets close.",
                "When capped", "A _Geyser Cap_ stops the eruptions and turns the vent into steady _Rotational Force_.",
                "Strength", "Each vent is _weak_, _normal_ or _strong_, which _Goggles_ show. Strong vents puff more steam and give a Cap or Well twice the output.");

        tooltip(ModBlocks.NETHER_GEYSER_VENT, "A rarer, hotter vent found in _Basalt Deltas_. Its eruptions _set you on fire_ as well as launching you.",
                "Before an eruption", "Rumbles for a few seconds first. _Goggles_ show the countdown, and a _Comparator_ signal rises as it gets close.",
                "When capped", "A _Geyser Cap_ on it gives _twice_ the stress capacity of an Overworld vent.");

        tooltip(ModBlocks.SOLAR_POWER_TOWER, "Multiblock tower that concentrates reflected sunlight to produce _Molten Salt_ or _Steam_ from water. Right-click with a Wrench to switch modes.",
                "At least 3x3 and 3 tall, in clear daylight", "Output grows with tower height and its _Solar Mirror_ field. Efficiency peaks at an optimal mirror count that grows with height, and falls off past it.");

        tooltip(ModBlocks.SOLAR_MIRROR, "Place on the ground near a _Solar Power Tower_. Links automatically when it has open sky and a clear line of sight to the tower, then tracks the sun. Too many mirrors lowers the tower's efficiency, down to zero at twice the optimal count.");

        tooltip(ModBlocks.BIOFILTER, "Draws _Rotational Force_ to absorb _pollution_ from nearby chunks, gradually healing blocks and restoring biomes. Cleans faster at higher speed.",
                "While cleaning pollution", "Collects _Carbon Dust_ from what it removes. Take it out by hand or with a funnel, and _compact_ it into _Biochar_.");

        tooltip(ModBlocks.SALT_LAMP, "A warm, glowing chunk of _Salt_. Right-click to switch it _on_ or _off_.");
        tooltip(ModBlocks.GARDEN_LIGHT, "A small solar light for paths and gardens. Charges while it can _see the sky_ during the day, then glows through the _night_. Place it in the shade and it stays dark.");
        tooltip(ModBlocks.STREETLAMP, "A tall street light that runs on _FE_. Switches itself _on at dusk_ and off at dawn, as long as it has power stored.",
                "When powered", "Takes _FE_ from cables or from a _Brass Solar Panel_ or _Solar Glass_ touching its base. A full charge lasts a whole night.");
        tooltip(ModBlocks.GROW_LAMP, "A purple grow light that runs on _FE_. Hang it under a ceiling, or from a wall or another Grow Lamp at either end.",
                "When powered", "Lights up so crops grow at _night_ and _underground_, and makes plants in a _3x3_ area below it grow _faster_. A _redstone_ signal switches it off.");
        tooltip(ModBlocks.SOLAR_PLANT_BED, "A raised bed for _one plant_ (one block tall: no stems, Hemp or tall flowers), for roofs and balconies. Its soil never dries out, so it needs _no water_ nearby.",
                "During the day", "The panel on top charges from _sunlight_. Needs open sky above the panel.",
                "At night", "The grow light runs on the stored charge and makes the plant grow _faster_. A _redstone_ signal switches it off.");
        tooltip(ModBlocks.WIRE_CONNECTOR, "Place on any block, then click two connectors with a _Spool_ to string a wire between them. Carries _FE_ from block to block.",
                "When wired", "Power going into a connector comes out into the blocks under the _other connectors_ it's wired to. Generators that don't push power out are drained. _Gold_ wire carries the most and reaches furthest, then _Copper_, then _Iron_.",
                "Right-click with Shears", "Cuts every wire on it and drops the wires.");
        tooltip(ModBlocks.WIRE_SWITCH, "Wire both _posts_ with a _Spool_. While _closed_, FE passes from one post's wires to the other's; while _open_, the network is cut in two.",
                "Right-click", "Opens or closes the switch. A _Comparator_ reads 15 while it's closed.",
                "Right-click with Shears", "Cuts every wire on it and drops the wires.");
        tooltip(ModBlocks.FUEL_CELL, "Burns _Hydrogen_ and _Oxygen_ back into _FE_. Line them up in a row or column with an _End Plate_ at each end to make a stack.",
                "In a stack", "Each cell adds output and fuel use, up to _16_ cells. Its only waste is _Water_. Take _FE_ from any cell with a _Wire Connector_.");
        tooltip(ModBlocks.END_PLATE, "Clamps the end of a _Fuel Cell_ stack. Place one facing out at each end.",
                "Pipes", "The port with arrows pointing in takes _Hydrogen_ or _Oxygen_: pipe one gas into each End Plate. The other port gives out _Water_. Take _FE_ from any side, with a _Wire Connector_ or a machine touching it.",
                "When powered", "A _Redstone_ signal on either plate pauses the stack, saving fuel.",
                "When Wrenched", "Turns the plate a quarter turn, so the ports point the way your pipes come in.");
        tooltip(ModBlocks.ELECTRIC_MOTOR, "Turns _FE_ into _rotational force_. Feed it from a _Brass Solar Panel_ or a _Wire Connector_ on any side.",
                "When powered", "Spins at the speed you set on it, up to _64 RPM_ either way. It uses FE for that speed whatever the load, so set it _slower_ to save power.",
                "Out of FE", "Stops, and starts again once it has half a second of FE stored. If it keeps stopping, _Goggles_ show the fastest speed your supply can hold.");
        tooltip(ModBlocks.ELECTROLYZER, "Splits _Water_ into _Hydrogen_ and _Oxygen_ with _FE_, making twice as much Hydrogen as Oxygen. A way to store solar power made at noon.",
                "When powered", "Pipe _Water_ into the top, front, back or bottom. Each gas comes out of the _copper flange_ beside its tube: _Hydrogen_ on the left, _Oxygen_ on the right, seen from the front.",
                "When a gas tank is full", "Stops until that gas is piped away, so pipe out _both_ gases.");
        tooltip(ModBlocks.REDSTONE_WIRE_RELAY, "A _Wire Switch_ thrown by _redstone_, for lights that turn on at dusk or machines that only run while the panels are strong.",
                "When powered", "Closes, joining the wires on its two posts. A _Comparator_ reads 15 while it's closed.",
                "When Wrenched", "Inverts it: _open_ while powered instead.");
        tooltip(ModBlocks.CHARRED_TIMBER, "Planks scorched black with _Biochar_. Charred wood _doesn't burn_.");
        tooltip(ModBlocks.SOLAR_GLASS, "Glass with solar cells in it that makes a little _FE_. Works as a _skylight_, or a window with _open sky_ above it.",
                "In sunlight", "Each block stores what it makes. Take power from _any_ glass block and the touching glass sends its energy along to it. Connects with _Solar Shingles_ the same way.");
        tooltip(ModBlocks.SOLAR_GLASS_PANE, "A thin pane of _Solar Glass_. Makes less _FE_ than the full block, and needs _open sky_ above it.",
                "In sunlight", "Each pane stores what it makes. Take power from _any_ pane and the touching glass sends its energy along to it. Connects with _Solar Shingles_ the same way.");
        tooltip(ModBlocks.SOLAR_SHINGLES, "Stepped solar roofing that makes a little _FE_. Bends into _corners_ like stairs, and each row placed up a roof switches between a _low_ and a _high_ piece by itself.",
                "In sunlight", "Each shingle stores what it makes. Take power from _any_ shingle and the rest of the roof sends its energy along to it. Roofs sloping _toward the sun_ make more, so east-facing roofs do best in the morning and west-facing ones in the evening.");
        tooltip(ModBlocks.SOLAR_GLASS_SHINGLES, "_Solar Shingles_ made of _Solar Glass_. Make as much _FE_ as a Solar Glass block, and step and bend into corners like normal shingles.",
                "In sunlight", "Each shingle stores what it makes and passes it along the roof, like normal shingles. Roofs sloping _toward the sun_ make more.");
        tooltip(ModBlocks.GREENHOUSE_CONTROLLER, "Turns a closed room with a _glass roof_ into a greenhouse. Place it in a wall facing _inside_. Needs no power.",
                "When the greenhouse is sealed", "Plants under _Greenhouse Glass_ or _Solar Glass_ grow faster, and faster still with _bees_ inside. A sealed greenhouse is also safe from _pollution_ decay. Walls can be glass or any solid block, and an _open door_ breaks the seal.");
        tooltip(ModBlocks.TRELLIS, "A wooden lattice that hangs on walls and can be _climbed_ like a ladder. Right-click with _Vines_ to grow vines on it, then add a _small flower_ or _Glow Berries_ to the vines. _Shears_ trim off the top layer. Vines slowly _climb_ onto bare trellises above and beside them, and soak up _pollution_ like leaves do.");

        tooltip(ModBlocks.KINETIC_SPRINKLER, "Pipe in _water_ to hydrate _farmland_ and speed up _crop growth_ in a 5x5 area below it.",
                "When supplied with Fertilizer instead of water", "_Bone meals_ every crop in range each second. _Rich Fertilizer_ reaches a block further and lasts twice as long.");

        tooltip(ModBlocks.TURBINE_ROTOR, "The core of the _Steam Turbine_ multiblock. Pipe _Steam_ into the casing walls and collect _Rotational Force_ from the cap rotor. Can be built standing up or on its side.",
                "Valid 7x7 structure with Turbine Blades", "Output grows with height and blade quality. Condensed water must be piped out of any casing face, or the turbine stops when it fills up.");

        for (String railing : new String[]{"brass_filigree_railing", "andesite_filigree_railing", "copper_filigree_railing", "iron_filigree_railing"}) {
            tooltip(railing, "A decorative railing that _connects_ to its neighbours. One item places every shape: _flat_, _slope_ and _steep slope_.",
                    "Hold the Toolbelt key (Alt)", "Opens a menu to pick the _shape_ it places. Steep slopes placed on _stairs_ follow them.");
        }

        tooltip(ModBlocks.PARTICLE_ACCELERATOR_PIPE, "Builds the ring of a _Particle Accelerator_. One pipe places every shape: _straight_, _bends_ and _diagonals_.",
                "Hold the Toolbelt key (Alt)", "Opens a menu to pick the _shape_ it places.",
                "When placed next to the ring", "Turns itself to _connect_ to the pipes around it. A _Wrench_ turns it by hand.");
        tooltip(ModBlocks.PARTICLE_ACCELERATOR_INPUT, "The heart of a _Particle Accelerator_. Smashes items together in a _closed ring_ of pipes to make new materials.",
                "When powered from below", "Crafts whatever its contents match, if the ring is _wide enough_ and spins _fast enough_. Bigger rings draw more _stress_.",
                "Items", "Go in and come back out through the _cap_, the _opposite side_ or the _top_, so you can feed one face and empty another. The _filter slot_ on the cap picks which recipe to make.");
        tooltip(ModBlocks.FUSION_REACTOR_CORE, "Heart of the _Fusion Reactor_. Goes at the center of a sphere of _Fusion Reactor Casing_ with rings of _Blanket Modules_ inside, and turns _Deuterium_ and _Lithium Dust_ or _Tritium Capsules_ into _Steam_.",
                "When the structure is complete", "A _redstone signal_ on any Casing block sets the throttle; no signal means idle. Pipe fuel in and Steam out through any Casing block.",
                "When given Tritium Capsules", "Burns them before _Lithium_, with no breeding waste, and hands back the _Empty Fuel Capsules_. Breeder Modules slowly refill empty capsules left inside.");

        tooltip(ModBlocks.LITHIUM_BREEDER_MODULE, "_Fusion Reactor_ blanket module. More Lithium in the blanket means a lower, steadier output that uses fuel more efficiently.");
        tooltip(ModBlocks.BERYLLIUM_REFLECTOR_MODULE, "_Fusion Reactor_ blanket module. More Beryllium in the blanket burns fuel faster for more _Steam_.");

        AllSolarpunkAdvancements.provideLang(this::add);

        // "Block of ___" naming like vanilla's metal blocks, instead of the "Lithium Block"
        // the loop below would produce. They're excluded from that loop.
        add(ModBlocks.LITHIUM_BLOCK.get(), "Block of Lithium");
        add(ModBlocks.BERYLLIUM_BLOCK.get(), "Block of Beryllium");

        // A BlockItem with its own name (the crop block is "Hemp"), so the item loop skips it.
        add(ModItems.HEMP_SEEDS.get(), "Hemp Seeds");

        ModBlocks.BLOCKS.getEntries().stream()
                .filter(entry -> entry.get() != ModBlocks.LITHIUM_BLOCK.get()
                        && entry.get() != ModBlocks.BERYLLIUM_BLOCK.get())
                .forEach(entry -> add(entry.get(), toTitleCase(entry.getId().getPath())));

        ModItems.ITEMS.getEntries().stream()
                .filter(entry -> !(entry.get() instanceof BlockItem))
                .forEach(entry -> add(entry.get(), toTitleCase(entry.getId().getPath())));

        ModFluidTypes.FLUID_TYPES.getEntries().forEach(entry ->
                add("fluid_type." + entry.getId().getNamespace() + "." + entry.getId().getPath(),
                        toTitleCase(entry.getId().getPath())));

        for (PaintingInfo info : ModPaintings.PAINTING_INFOS) {
            String base = "painting." + SolarPunk.MODID + "." + info.id();
            add(base + ".title", info.title());
            add(base + ".author", info.author());
        }
    }
}
