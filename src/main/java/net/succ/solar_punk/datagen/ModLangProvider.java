package net.succ.solar_punk.datagen;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.compat.ponder.SolarPunkPonderPlugin;
import net.succ.solar_punk.fluid.ModFluidTypes;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.painting.ModPaintings;
import net.succ.solar_punk.painting.PaintingInfo;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, SolarPunk.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.solar_punk", "Create: Solarpunk");
        add("itemGroup.solar_punk.building_blocks", "Create: Solarpunk Building Blocks");
        add("jei.solarpunk.category.solar_heating", "Solar Heating");
        add("jei.solarpunk.category.solar_power_tower", "Solar Power Tower");
        add("jei.solarpunk.category.fermentation_vat", "Fermentation Vat");
        add("jei.solarpunk.category.crystallizing", "Crystallizer");
        add("jei.solarpunk.category.particle_accelerator", "Particle Accelerator");
        add("jei.solarpunk.particle_accelerator.diameter", "Ring diameter: %s+");
        add("jei.solarpunk.particle_accelerator.stress", "Stress: %s SU (at min diameter)");
        add("jei.solarpunk.particle_accelerator.speed", "Min speed: %s RPM");

        // Sound subtitles - format: subtitles.solarpunk.<sound event path>, referenced by
        // sounds.json. One entry per SoundEvent registered in ModSounds.
        add("subtitles.solarpunk.brass_solar_panel_hum", "Solar panel hums");
        add("subtitles.solarpunk.geyser_puff", "Geyser puffs");
        add("subtitles.solarpunk.particle_accelerator_hum", "Particle Accelerator hums");
        add("subtitles.solarpunk.particle_collision", "Particles collide");
        add("subtitles.solarpunk.biofuel_engine_loop", "Biofuel Engine running");
        add("subtitles.solarpunk.biofilter_loop", "Biofilter filtering");
        add("subtitles.solarpunk.biomass_gasifier_loop", "Biomass Gasifier burning");
        add("subtitles.solarpunk.fermentation_vat_bubbling", "Fermentation Vat bubbles");
        add("subtitles.solarpunk.solar_heater_shimmer", "Solar Heater shimmers");
        add("subtitles.solarpunk.steam_hiss", "Steam hisses");
        add("subtitles.solarpunk.turbine_loop", "Steam Turbine spins");
        add("subtitles.solarpunk.fusion_reactor_startup", "Fusion Reactor powers up");
        add("subtitles.solarpunk.fusion_reactor_shutdown", "Fusion Reactor powers down");
        add("subtitles.solarpunk.reactor_on_loop", "Fusion Reactor hums");

        // Ponder tags and scene text come straight from SolarPunkPonderTags and the scene
        // classes. Ponder normally registers its plugins during client setup, which doesn't
        // run during datagen, so register ours here first (same as Create's datagen).
        PonderIndex.addPlugin(new SolarPunkPonderPlugin());
        PonderIndex.getLangAccess().provideLang(SolarPunk.MODID, this::add);

        add("create.solar_punk.tooltip.biofilter_header", "Biofilter");
        add("create.solar_punk.tooltip.biofilter_status", "Status: ");
        add("create.solar_punk.tooltip.biofilter_pollution", "Chunk Pollution: ");
        add("create.solar_punk.tooltip.biofilter_removing", "Removing: ");
        add("create.solar_punk.tooltip.biofilter_carbon", "Carbon Dust: ");

        add("create.solar_punk.tooltip.geyser_vent_header", "Geyser Vent");
        add("create.solar_punk.tooltip.geyser_vent_next", "Next eruption: ");
        add("create.solar_punk.tooltip.geyser_vent_warning", "Any second now!");
        add("create.solar_punk.tooltip.geyser_vent_erupting", "Erupting!");
        add("create.solar_punk.tooltip.geyser_vent_quiet", "None");

        add("death.attack.solarpunk.scalding", "%1$s was scalded by a geyser");
        add("death.attack.solarpunk.scalding.player", "%1$s was scalded by a geyser while fighting %2$s");

        add("create.solar_punk.tooltip.accelerator_header", "Particle Accelerator");
        add("create.solar_punk.tooltip.accelerator_ring", "Ring: ");
        add("create.solar_punk.tooltip.accelerator_diameter", "Diameter: ");
        add("create.solar_punk.tooltip.accelerator_recipe", "Recipe: ");
        add("create.solar_punk.tooltip.accelerator_progress", "Progress: ");
        add("create.solar_punk.tooltip.accelerator_too_small", "Ring too small: needs %1$s+ wide");
        add("create.solar_punk.tooltip.accelerator_too_slow", "Too slow: needs %1$s RPM");
        add("create.solar_punk.tooltip.accelerator_overstressed", "Overstressed");

        add("create.solar_punk.tooltip.greenhouse_header", "Greenhouse");
        add("create.solar_punk.tooltip.streetlamp_header", "Streetlamp");
        add("create.solar_punk.tooltip.wire_connector_header", "Wire Connector");
        add("create.solar_punk.tooltip.wires", "Wires: ");
        add("create.solar_punk.tooltip.grow_lamp_header", "Grow Lamp");
        add("create.solar_punk.tooltip.grow_lamp_plants", "Plants: ");
        add("create.solar_punk.tooltip.solar_plant_bed_header", "Solar Plant Bed");
        add("create.solar_punk.tooltip.throughput", "Throughput: ");

        add("message.solarpunk.wire.started", "Wire started. Click another Wire Connector to finish it");
        add("message.solarpunk.wire.cancelled", "Wire cancelled");
        add("message.solarpunk.wire.start_missing", "The connector this wire started from is gone");
        add("message.solarpunk.wire.already_connected", "These connectors are already wired together");
        add("message.solarpunk.wire.full", "That connector can't hold any more wires");
        add("message.solarpunk.wire.too_long", "Too long: this wire reaches at most %s blocks");
        add("message.solarpunk.wire.not_enough", "Not enough wire on the spool: needs %s");
        add("item.solarpunk.spool.wires_left", "%s wires left (%s blocks)");
        add("item.solarpunk.spool.stats", "Carries %s, up to %s blocks long");
        add("item.solarpunk.spool.started", "Wire started at %s, %s, %s");
        add("create.solar_punk.tooltip.greenhouse_status", "Status: ");
        add("create.solar_punk.tooltip.greenhouse_sealed", "Sealed");
        add("create.solar_punk.tooltip.greenhouse_not_sealed", "Not sealed, or too big");
        add("create.solar_punk.tooltip.greenhouse_front_blocked", "Front is blocked");
        add("create.solar_punk.tooltip.greenhouse_other_controller", "Another controller runs this greenhouse");
        add("create.solar_punk.tooltip.greenhouse_volume", "Space: ");
        add("create.solar_punk.tooltip.greenhouse_crops", "Plants under glass: ");
        add("create.solar_punk.tooltip.greenhouse_boost", "Growth speed: ");
        add("create.solar_punk.tooltip.greenhouse_bees", "Bees pollinating: ");
        add("create.solar_punk.tooltip.sprinkler_header", "Sprinkler");
        add("create.solar_punk.tooltip.sprinkler_status", "Status: ");

        add("create.solar_punk.tooltip.fe_header", "Generator Stats");
        add("create.solar_punk.tooltip.generating", "Generating: ");
        add("create.solar_punk.tooltip.agrivoltaics", "Cooled by plants below: +%s%%");
        add("create.solar_punk.tooltip.stored", "Stored: ");

        add("create.solar_punk.tooltip.kinetic_battery_header", "Kinetic Battery");
        add("create.solar_punk.tooltip.charge", "Charge:");
        add("create.solar_punk.tooltip.runtime", "Runtime:");

        add("create.solar_punk.tooltip.heat_battery_header", "Heat Battery");
        add("create.solar_punk.tooltip.heat_level", "Level: ");
        add("create.solar_punk.tooltip.heat_stored", "Heat: ");
        add("create.solar_punk.tooltip.new_age_temperature", "New Age Temperature: ");
        add("create.solar_punk.tooltip.molten_salt", "Molten Salt: ");
        add("create.solar_punk.tooltip.steam", "Steam: ");
        add("create.solar_punk.tooltip.tower_mode", "Mode: ");
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
        add("solarpunk.display_source.greenhouse_status", "Greenhouse Status");
        add("solarpunk.display_source.greenhouse_plant_count", "Greenhouse Plant Count");
        add("solarpunk.display_source.greenhouse_growth", "Greenhouse Growth Speed");

        add("create.solar_punk.tooltip.fermentation_vat_header", "Fermentation Vat");
        add("create.solar_punk.tooltip.vat_too_small", "Needs at least 2x2 footprint");
        add("create.solar_punk.tooltip.vat_too_short", "Needs at least 4 blocks tall");
        add("create.solar_punk.tooltip.vat_batch_scale", "Batch size: ");
        add("create.solar_punk.tooltip.vat_biofuel_output", "Output: ");
        add("create.solar_punk.tooltip.fermenting", "Fermenting: ");
        add("create.solar_punk.tooltip.biofuel", "Biofuel: ");

        add("create.solar_punk.tooltip.gasifier_header", "Biomass Gasifier");
        add("create.solar_punk.tooltip.fuel", "Fuel: ");
        add("create.solar_punk.tooltip.burn_time", "Burn time: ");
        add("create.solar_punk.tooltip.biomass_stored", "Biomass: ");
        add("create.solar_punk.tooltip.biochar_stored", "Biochar: ");

        add("create.solar_punk.tooltip.biofuel_engine_header", "Biofuel Engine");
        add("create.solar_punk.tooltip.consumption", "Consumption: ");

        add("create.solar_punk.tooltip.steam_turbine_header", "Steam Turbine");
        add("create.solar_punk.tooltip.turbine_invalid", "Structure invalid - check build");
        add("create.solar_punk.tooltip.turbine_height", "Height: ");
        add("create.solar_punk.tooltip.turbine_blades", "Blades: ");
        add("create.solar_punk.tooltip.turbine_fuel", "Fuel: ");
        add("create.solar_punk.tooltip.steam_supply", "Steam Supply: ");
        add("create.solar_punk.tooltip.condensate_full", "Condensate full - drain water to restart");

        add("create.solar_punk.tooltip.fusion_reactor_core_header", "Fusion Reactor Core");
        add("create.solar_punk.tooltip.reactor_status", "Status: ");
        add("create.solar_punk.tooltip.reactor_blanket", "Blanket: ");
        add("create.solar_punk.tooltip.reactor_steam_output", "Steam Output: ");
        add("create.solar_punk.tooltip.reactor_fuel_draw", "Fuel Draw: ");
        add("create.solar_punk.tooltip.reactor_breeding_efficiency", "Breeding Efficiency: ");

        add("create.solar_punk.tooltip.solar_power_tower_header", "Solar Power Tower");
        add("create.solar_punk.tooltip.mirrors", "Mirrors: ");
        add("create.solar_punk.tooltip.efficiency", "Efficiency: ");

        add("create.solar_punk.tooltip.firebox_boiler_header", "Firebox Boiler");

        add("create.solar_punk.tooltip.heater_header", "Solar Heater");
        add("create.solar_punk.tooltip.melting", "Melting: ");
        add("create.solar_punk.tooltip.progress", "Progress: ");
        add("create.solar_punk.tooltip.water", "Water: ");
        add("create.solar_punk.tooltip.fertilizer", "Fertilizer: ");
        add("create.solar_punk.tooltip.rich_fertilizer", "Rich Fertilizer: ");
        add("create.solar_punk.tooltip.evaporation", "Evaporation: ");
        add("create.solar_punk.tooltip.output_fluid", "Output: ");
        add("create.solar_punk.tooltip.salt_output", "Salt: ");

        add("create.solar_punk.tooltip.crystallizer_header", "Crystallizer");
        add("create.solar_punk.tooltip.crystallizer_progress", "Progress: ");
        add("create.solar_punk.tooltip.crystallizer_input_a", "Input A: ");
        add("create.solar_punk.tooltip.crystallizer_input_b", "Input B: ");
        add("create.solar_punk.tooltip.crystallizer_byproduct", "Byproduct: ");
        add("create.solar_punk.tooltip.crystallizer_item_output", "Output: ");

        // Inventory hover tooltips
        add("block.solarpunk.solar_heater.tooltip.summary", "Uses _sunlight_ to melt items into fluid and passively evaporate water into _Salt_.");
        add("block.solarpunk.solar_heater.tooltip.condition1", "During the day with a clear sky");
        add("block.solarpunk.solar_heater.tooltip.behaviour1", "Melts the loaded item and evaporates stored water. Runs at half speed in _rain_ and stops in _thunderstorms_.");

        add("block.solarpunk.crystallizer.tooltip.summary", "Runs data-driven recipes that turn up to two fluids into an item, plus an optional fluid byproduct. By default, quenches _Molten Salt_ with _Water_ into _Salt_ and some _Steam_ - the fast/bulk counterpart to the Solar Heater's slow evaporation trickle.");
        add("block.solarpunk.crystallizer.tooltip.condition1", "Any time - no sunlight needed");
        add("block.solarpunk.crystallizer.tooltip.behaviour1", "Pipe both input fluids in from any face; collect the result from the block or a hopper below, and any byproduct fluid from any face.");

        add("block.solarpunk.firebox_boiler.tooltip.summary", "Burns furnace fuel to boil water into _Steam_ for the _Steam Turbine_. A cheap way to get a turbine running before building a Solar Power Tower - nowhere near as efficient at scale.");
        add("block.solarpunk.firebox_boiler.tooltip.condition1", "While lit");
        add("block.solarpunk.firebox_boiler.tooltip.behaviour1", "Converts water to Steam at a fixed rate. Insert fuel by hand or hopper; pipe water in and Steam out of any face.");

        add("block.solarpunk.andesite_solar_panel.tooltip.summary", "Generates _Rotational Force_ from sunlight. Output scales with sun angle and weather.");
        add("block.solarpunk.andesite_solar_panel.tooltip.condition1", "During the day with a clear sky");
        add("block.solarpunk.andesite_solar_panel.tooltip.behaviour1", "8 RPM with 1024 SU at dawn and dusk, 16 RPM with 4096 SU at noon. _Rain_ drops it to the dawn rate and it stops at _night_.");
        add("block.solarpunk.andesite_solar_panel.tooltip.condition2", "Raised over plants");
        add("block.solarpunk.andesite_solar_panel.tooltip.behaviour2", "Crops, flowers or saplings up to _4 blocks_ below cool the panel for _10%_ more output, and still grow at normal speed in its shade.");

        add("block.solarpunk.brass_solar_panel.tooltip.summary", "Generates _Forge Energy_ from sunlight. Output scales with sun angle and weather.");
        add("block.solarpunk.brass_solar_panel.tooltip.condition1", "During the day with a clear sky");
        add("block.solarpunk.brass_solar_panel.tooltip.behaviour1", "40 FE/t at dawn and dusk, 80 FE/t at noon. _Rain_ drops it to the dawn rate and it stops at _night_.");
        add("block.solarpunk.brass_solar_panel.tooltip.condition2", "Raised over plants");
        add("block.solarpunk.brass_solar_panel.tooltip.behaviour2", "Crops, flowers or saplings up to _4 blocks_ below cool the panel for _10%_ more output, and still grow at normal speed in its shade.");

        add("block.solarpunk.heat_battery.tooltip.summary", "Stores thermal energy as _Molten Salt_ and supplies heat to a _Boiler_ placed above it.");
        add("block.solarpunk.heat_battery.tooltip.condition1", "When filled with Molten Salt");
        add("block.solarpunk.heat_battery.tooltip.behaviour1", "Heats the boiler above, _superheating_ it while at least a quarter charged. Stored heat slowly fades.");
        add("block.solarpunk.heat_battery.tooltip.condition2", "With Create: New Age");
        add("block.solarpunk.heat_battery.tooltip.behaviour2", "Trades heat with touching _heat pipes_ and machines. A charged battery heats them, and an emptier one soaks up their heat.");

        add("block.solarpunk.kinetic_battery.tooltip.summary", "Stores _Rotational Force_ for later use. Useful for buffering intermittent generators like solar panels.");
        add("block.solarpunk.kinetic_battery.tooltip.condition1", "When connected to a spinning network");
        add("block.solarpunk.kinetic_battery.tooltip.behaviour1", "Charges up while _no Redstone_ signal is applied.");
        add("block.solarpunk.kinetic_battery.tooltip.condition2", "When a Redstone signal is applied");
        add("block.solarpunk.kinetic_battery.tooltip.behaviour2", "Discharges at 16 RPM with 256 SU of stress capacity until empty.");

        add("block.solarpunk.fermentation_vat.tooltip.summary", "Ferments _Biomass_ and water into _Biofuel_, or mob and farm _waste_ into _Rich Fertilizer_. Requires at least a _2x2 footprint_ and _4 blocks tall_ to operate.");
        add("block.solarpunk.fermentation_vat.tooltip.condition1", "Larger footprint");
        add("block.solarpunk.fermentation_vat.tooltip.behaviour1", "Increases batch size and output. A 3x3 footprint processes 9 Biomass per batch. Taller vats produce super-linearly more Biofuel per batch.");

        add("block.solarpunk.biomass_gasifier.tooltip.summary", "Burns _Biomass_ to generate _Rotational Force_. Produces _Biochar_ as a byproduct.");
        add("block.solarpunk.biomass_gasifier.tooltip.condition1", "While burning");
        add("block.solarpunk.biomass_gasifier.tooltip.behaviour1", "Outputs 8 RPM with 2048 SU of stress capacity downward.");

        add("block.solarpunk.biofuel_engine.tooltip.summary", "Burns _Biofuel_ to generate _Rotational Force_. Pipe fuel into any face.");
        add("block.solarpunk.biofuel_engine.tooltip.condition1", "While fuelled");
        add("block.solarpunk.biofuel_engine.tooltip.behaviour1", "Outputs 16 RPM with 8192 SU of stress capacity downward.");

        add("block.solarpunk.geyser_cap.tooltip.summary", "Harnesses the energy of a _Geyser Vent_ for passive, round-the-clock _Rotational Force_.");
        add("block.solarpunk.geyser_cap.tooltip.condition1", "When placed on a Geyser Vent");
        add("block.solarpunk.geyser_cap.tooltip.behaviour1", "Generates 32 RPM with 16384 SU of stress capacity. Works day and night in all weather.");

        add("block.solarpunk.geyser_vent.tooltip.summary", "A rare natural vent. Left uncapped, it _erupts_ every few minutes, scalding and launching anything standing over it.");
        add("block.solarpunk.geyser_vent.tooltip.condition1", "Before an eruption");
        add("block.solarpunk.geyser_vent.tooltip.behaviour1", "Rumbles for a few seconds first. _Goggles_ show the countdown, and a _Comparator_ signal rises as it gets close.");
        add("block.solarpunk.geyser_vent.tooltip.condition2", "When capped");
        add("block.solarpunk.geyser_vent.tooltip.behaviour2", "A _Geyser Cap_ stops the eruptions and turns the vent into steady _Rotational Force_.");

        add("block.solarpunk.solar_power_tower.tooltip.summary", "Multiblock tower that concentrates reflected sunlight to produce _Molten Salt_ or _Steam_ from water. Right-click with a Wrench to switch modes.");
        add("block.solarpunk.solar_power_tower.tooltip.condition1", "At least 3x3 and 3 tall, in clear daylight");
        add("block.solarpunk.solar_power_tower.tooltip.behaviour1", "Output grows with tower height and its _Solar Mirror_ field. Efficiency peaks at an optimal mirror count that grows with height, and falls off past it.");

        add("block.solarpunk.solar_mirror.tooltip.summary", "Place on the ground near a _Solar Power Tower_. Links automatically when it has open sky and a clear line of sight to the tower, then tracks the sun. Too many mirrors lowers the tower's efficiency, down to zero at twice the optimal count.");

        add("block.solarpunk.biofilter.tooltip.summary", "Draws _Rotational Force_ to absorb _pollution_ from nearby chunks, gradually healing blocks and restoring biomes. Cleans faster at higher speed.");
        add("block.solarpunk.biofilter.tooltip.condition1", "While cleaning pollution");
        add("block.solarpunk.biofilter.tooltip.behaviour1", "Collects _Carbon Dust_ from what it removes. Take it out by hand or with a funnel, and _compact_ it into _Biochar_.");

        add("block.solarpunk.salt_lamp.tooltip.summary", "A warm, glowing chunk of _Salt_. Right-click to switch it _on_ or _off_.");
        add("block.solarpunk.garden_light.tooltip.summary", "A small solar light for paths and gardens. Charges while it can _see the sky_ during the day, then glows through the _night_. Place it in the shade and it stays dark.");
        add("block.solarpunk.streetlamp.tooltip.summary", "A tall street light that runs on _FE_. Switches itself _on at dusk_ and off at dawn, as long as it has power stored.");
        add("block.solarpunk.streetlamp.tooltip.condition1", "When powered");
        add("block.solarpunk.streetlamp.tooltip.behaviour1", "Takes _FE_ from cables or from a _Brass Solar Panel_ or _Solar Glass_ touching its base. A full charge lasts a whole night.");
        add("block.solarpunk.grow_lamp.tooltip.summary", "A purple grow light that runs on _FE_. Hang it under a ceiling, or from a wall or another Grow Lamp at either end.");
        add("block.solarpunk.grow_lamp.tooltip.condition1", "When powered");
        add("block.solarpunk.grow_lamp.tooltip.behaviour1", "Lights up so crops grow at _night_ and _underground_, and makes plants in a _3x3_ area below it grow _faster_. A _redstone_ signal switches it off.");
        add("block.solarpunk.solar_plant_bed.tooltip.summary", "A raised bed for _one plant_, for roofs and balconies. Its soil never dries out, so it needs _no water_ nearby.");
        add("block.solarpunk.solar_plant_bed.tooltip.condition1", "During the day");
        add("block.solarpunk.solar_plant_bed.tooltip.behaviour1", "The panel on top charges from _sunlight_. Needs open sky above the panel.");
        add("block.solarpunk.solar_plant_bed.tooltip.condition2", "At night");
        add("block.solarpunk.solar_plant_bed.tooltip.behaviour2", "The grow light runs on the stored charge and makes the plant grow _faster_. A _redstone_ signal switches it off.");
        add("block.solarpunk.wire_connector.tooltip.summary", "Place on any block, then click two connectors with a _Spool_ to string a wire between them. Carries _FE_ from block to block.");
        add("block.solarpunk.wire_connector.tooltip.condition1", "When wired");
        add("block.solarpunk.wire_connector.tooltip.behaviour1", "Power going into a connector comes out into the blocks under the _other connectors_ it's wired to. Generators that don't push power out are drained. _Gold_ wire carries the most and reaches furthest, then _Copper_, then _Iron_.");
        add("block.solarpunk.wire_connector.tooltip.condition2", "Right-click with Shears");
        add("block.solarpunk.wire_connector.tooltip.behaviour2", "Cuts every wire on it and drops the wires.");
        add("block.solarpunk.charred_timber.tooltip.summary", "Planks scorched black with _Biochar_. Charred wood _doesn't burn_.");
        add("block.solarpunk.solar_glass.tooltip.summary", "Glass with solar cells in it that makes a little _FE_. Works as a _skylight_, or a window with _open sky_ above it.");
        add("block.solarpunk.solar_glass.tooltip.condition1", "In sunlight");
        add("block.solarpunk.solar_glass.tooltip.behaviour1", "Each block stores what it makes. Take power from _any_ glass block and the touching glass sends its energy along to it. Connects with _Solar Shingles_ the same way.");
        add("block.solarpunk.solar_glass_pane.tooltip.summary", "A thin pane of _Solar Glass_. Makes less _FE_ than the full block, and needs _open sky_ above it.");
        add("block.solarpunk.solar_glass_pane.tooltip.condition1", "In sunlight");
        add("block.solarpunk.solar_glass_pane.tooltip.behaviour1", "Each pane stores what it makes. Take power from _any_ pane and the touching glass sends its energy along to it. Connects with _Solar Shingles_ the same way.");
        add("block.solarpunk.solar_shingles.tooltip.summary", "Stepped solar roofing that makes a little _FE_. Bends into _corners_ like stairs, and each row placed up a roof switches between a _low_ and a _high_ piece by itself.");
        add("block.solarpunk.solar_shingles.tooltip.condition1", "In sunlight");
        add("block.solarpunk.solar_shingles.tooltip.behaviour1", "Each shingle stores what it makes. Take power from _any_ shingle and the rest of the roof sends its energy along to it. Roofs sloping _toward the sun_ make more, so east-facing roofs do best in the morning and west-facing ones in the evening.");
        add("block.solarpunk.solar_glass_shingles.tooltip.summary", "_Solar Shingles_ made of _Solar Glass_. Make as much _FE_ as a Solar Glass block, and step and bend into corners like normal shingles.");
        add("block.solarpunk.solar_glass_shingles.tooltip.condition1", "In sunlight");
        add("block.solarpunk.solar_glass_shingles.tooltip.behaviour1", "Each shingle stores what it makes and passes it along the roof, like normal shingles. Roofs sloping _toward the sun_ make more.");
        add("block.solarpunk.greenhouse_controller.tooltip.summary", "Turns a closed room with a _glass roof_ into a greenhouse. Place it in a wall facing _inside_. Needs no power.");
        add("block.solarpunk.greenhouse_controller.tooltip.condition1", "When the greenhouse is sealed");
        add("block.solarpunk.greenhouse_controller.tooltip.behaviour1", "Plants under _Greenhouse Glass_ or _Solar Glass_ grow faster, and faster still with _bees_ inside. A sealed greenhouse is also safe from _pollution_ decay. Walls can be glass or any solid block, and an _open door_ breaks the seal.");
        add("block.solarpunk.trellis.tooltip.summary", "A wooden lattice that hangs on walls and can be _climbed_ like a ladder. Right-click with _Vines_ to grow vines on it, then add a _small flower_ or _Glow Berries_ to the vines. _Shears_ trim off the top layer. Vines slowly _climb_ onto bare trellises above and beside them, and soak up _pollution_ like leaves do.");

        add("block.solarpunk.kinetic_sprinkler.tooltip.summary", "Pipe in _water_ to hydrate _farmland_ and speed up _crop growth_ in a 5x5 area below it.");
        add("block.solarpunk.kinetic_sprinkler.tooltip.condition1", "When supplied with Fertilizer instead of water");
        add("block.solarpunk.kinetic_sprinkler.tooltip.behaviour1", "_Bone meals_ every crop in range each second. _Rich Fertilizer_ reaches a block further and lasts twice as long.");

        add("block.solarpunk.turbine_rotor.tooltip.summary", "The core of the _Steam Turbine_ multiblock. Pipe _Steam_ into the casing walls and collect _Rotational Force_ from the cap rotor. Can be built standing up or on its side.");
        add("block.solarpunk.turbine_rotor.tooltip.condition1", "Valid 7x7 structure with Turbine Blades");
        add("block.solarpunk.turbine_rotor.tooltip.behaviour1", "Output grows with height and blade quality. Condensed water must be piped out of any casing face, or the turbine stops when it fills up.");

        for (String railing : new String[]{"brass_filigree_railing", "andesite_filigree_railing", "copper_filigree_railing", "iron_filigree_railing"}) {
            add("block.solarpunk." + railing + ".tooltip.summary", "A decorative railing that _connects_ to its neighbours. One item places every shape: _flat_, _slope_ and _steep slope_.");
            add("block.solarpunk." + railing + ".tooltip.condition1", "Hold the Toolbelt key (Alt)");
            add("block.solarpunk." + railing + ".tooltip.behaviour1", "Opens a menu to pick the _shape_ it places. Steep slopes placed on _stairs_ follow them.");
        }

        add("block.solarpunk.particle_accelerator_pipe.tooltip.summary", "Builds the ring of a _Particle Accelerator_. One pipe places every shape: _straight_, _bends_ and _diagonals_.");
        add("block.solarpunk.particle_accelerator_pipe.tooltip.condition1", "Hold the Toolbelt key (Alt)");
        add("block.solarpunk.particle_accelerator_pipe.tooltip.behaviour1", "Opens a menu to pick the _shape_ it places.");
        add("block.solarpunk.particle_accelerator_pipe.tooltip.condition2", "When placed next to the ring");
        add("block.solarpunk.particle_accelerator_pipe.tooltip.behaviour2", "Turns itself to _connect_ to the pipes around it. A _Wrench_ turns it by hand.");
        add("block.solarpunk.particle_accelerator_input.tooltip.summary", "The heart of a _Particle Accelerator_. Smashes items together in a _closed ring_ of pipes to make new materials.");
        add("block.solarpunk.particle_accelerator_input.tooltip.condition1", "When powered from below");
        add("block.solarpunk.particle_accelerator_input.tooltip.behaviour1", "Crafts whatever its contents match, if the ring is _wide enough_ and spins _fast enough_. Bigger rings draw more _stress_.");
        add("block.solarpunk.particle_accelerator_input.tooltip.condition2", "Items");
        add("block.solarpunk.particle_accelerator_input.tooltip.behaviour2", "Go in and come back out through the _cap_, the _opposite side_ or the _top_, so you can feed one face and empty another. The _filter slot_ on the cap picks which recipe to make.");
        add("block.solarpunk.fusion_reactor_core.tooltip.summary", "Heart of the _Fusion Reactor_. Goes at the center of a sphere of _Fusion Reactor Casing_ with rings of _Blanket Modules_ inside, and turns _Deuterium_ and _Lithium Dust_ or _Tritium Capsules_ into _Steam_.");
        add("block.solarpunk.fusion_reactor_core.tooltip.condition1", "When the structure is complete");
        add("block.solarpunk.fusion_reactor_core.tooltip.behaviour1", "A _redstone signal_ on any Casing block sets the throttle; no signal means idle. Pipe fuel in and Steam out through any Casing block.");
        add("block.solarpunk.fusion_reactor_core.tooltip.condition2", "When given Tritium Capsules");
        add("block.solarpunk.fusion_reactor_core.tooltip.behaviour2", "Burns them before _Lithium_, with no breeding waste, and hands back the _Empty Fuel Capsules_. Breeder Modules slowly refill empty capsules left inside.");

        add("block.solarpunk.lithium_breeder_module.tooltip.summary", "_Fusion Reactor_ blanket module. More Lithium in the blanket means a lower, steadier output that uses fuel more efficiently.");
        add("block.solarpunk.beryllium_reflector_module.tooltip.summary", "_Fusion Reactor_ blanket module. More Beryllium in the blanket burns fuel faster for more _Steam_.");

        AllSolarpunkAdvancements.provideLang(this::add);

        // "Block of ___" naming like vanilla's metal blocks, instead of the "Lithium Block"
        // the loop below would produce. They're excluded from that loop.
        add(ModBlocks.LITHIUM_BLOCK.get(), "Block of Lithium");
        add(ModBlocks.BERYLLIUM_BLOCK.get(), "Block of Beryllium");

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

    private static String toTitleCase(String name) {
        String[] words = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
