package net.succ.solar_punk.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
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
        add("jei.solarpunk.category.solar_heating", "Solar Heating");
        add("jei.solarpunk.category.solar_power_tower", "Solar Power Tower");
        add("jei.solarpunk.category.fermentation_vat", "Fermentation Vat");
        add("jei.solarpunk.category.crystallizing", "Crystallizer");

        // Ponder tag - format: <namespace>.ponder.tag.<path>
        add("solarpunk.ponder.tag.solar_machines", "Solar Machines");
        add("solarpunk.ponder.tag.solar_machines.description", "Machines that harness the power of sunlight");
        add("solarpunk.ponder.tag.bio_machines", "Bio Machines");
        add("solarpunk.ponder.tag.bio_machines.description", "Machines for producing and burning Biofuel");
        add("solarpunk.ponder.tag.solar_tower", "Solar Power Tower");
        add("solarpunk.ponder.tag.solar_tower.description", "Concentrated solar power for producing Molten Salt");
        add("solarpunk.ponder.tag.steam_turbine", "Steam Turbine");
        add("solarpunk.ponder.tag.steam_turbine.description", "High-throughput steam-powered rotational force generator");
        add("solarpunk.ponder.tag.fusion_reactor", "Fusion Reactor");
        add("solarpunk.ponder.tag.fusion_reactor.description", "Endgame direct-steam fusion power, fed by Deuterium and Lithium");

        // Ponder scene text - format: <namespace>.ponder.<sceneId>.header / .text_N
        add("solarpunk.ponder.biomass_gasifier_usage.header", "Using the Biomass Gasifier");
        add("solarpunk.ponder.biomass_gasifier_usage.text_1", "The Biomass Gasifier burns Biomass to generate Rotational Force");
        add("solarpunk.ponder.biomass_gasifier_usage.text_2", "Add Biomass into the top slot - each piece burns for 15 seconds");
        add("solarpunk.ponder.biomass_gasifier_usage.text_3", "While burning, it outputs 8 RPM with 2048 SU of stress capacity");
        add("solarpunk.ponder.biomass_gasifier_usage.text_4", "Rotation exits downward - connect a shaft or machine directly below");
        add("solarpunk.ponder.biomass_gasifier_usage.text_5", "Output is intermittent - use a Kinetic Battery to buffer power between refills");

        add("solarpunk.ponder.biofuel_engine_usage.header", "Using the Biofuel Engine");
        add("solarpunk.ponder.biofuel_engine_usage.text_1", "The Biofuel Engine burns Biofuel to generate Rotational Force");
        add("solarpunk.ponder.biofuel_engine_usage.text_2", "Pipe Biofuel into any face to fill the internal tank");
        add("solarpunk.ponder.biofuel_engine_usage.text_3", "With Biofuel present, it outputs 16 RPM with 8192 SU of stress capacity");
        add("solarpunk.ponder.biofuel_engine_usage.text_4", "Rotation exits downward - connect a shaft or machine directly below");

        add("solarpunk.ponder.solar_heater_usage.header", "Using the Solar Heater");
        add("solarpunk.ponder.solar_heater_usage.text_1", "The Solar Heater melts items into fluids using sunlight");
        add("solarpunk.ponder.solar_heater_usage.text_2", "Right-click to place an item into the input slot");
        add("solarpunk.ponder.solar_heater_usage.text_3", "During the day with clear skies, the heater slowly processes the item");
        add("solarpunk.ponder.solar_heater_usage.text_4", "Rain halves the processing speed");
        add("solarpunk.ponder.solar_heater_usage.text_5", "The resulting fluid fills the output tank - drain it with a pipe or bucket");

        add("solarpunk.ponder.crystallizer_usage.header", "Using the Crystallizer");
        add("solarpunk.ponder.crystallizer_usage.text_1", "The Crystallizer runs data-driven recipes: two fluids in, an item out, plus an optional fluid byproduct");
        add("solarpunk.ponder.crystallizer_usage.text_2", "By default it quenches Molten Salt with Water to produce Salt");
        add("solarpunk.ponder.crystallizer_usage.text_3", "Unlike the Solar Heater, it works day or night - cooling doesn't need sunlight");
        add("solarpunk.ponder.crystallizer_usage.text_4", "The result collects in the block's output slot - grab it by hand or pull it out with a hopper");
        add("solarpunk.ponder.crystallizer_usage.text_5", "Quenching Molten Salt also flashes off some Steam as a byproduct, ready for a Steam Turbine");

        add("solarpunk.ponder.solar_heater_evaporation.header", "Evaporating Salt");
        add("solarpunk.ponder.solar_heater_evaporation.text_1", "The Solar Heater can also evaporate water into Salt");
        add("solarpunk.ponder.solar_heater_evaporation.text_2", "Fill the water tank by right-clicking with a water bucket, or pipe water in from the side");
        add("solarpunk.ponder.solar_heater_evaporation.text_3", "With sunlight present, water slowly evaporates and Salt accumulates in the second slot");
        add("solarpunk.ponder.solar_heater_evaporation.text_4", "Salt can be smelted into Molten Salt directly in the Solar Heater");

        add("solarpunk.ponder.andesite_panel_usage.header", "Andesite Solar Panel");
        add("solarpunk.ponder.andesite_panel_usage.text_1", "The Andesite Solar Panel generates Rotational Force from sunlight");
        add("solarpunk.ponder.andesite_panel_usage.text_2", "It outputs rotation downwards - connect a shaft or machine directly below");
        add("solarpunk.ponder.andesite_panel_usage.text_3", "At dawn and dusk: 8 RPM with 1024 SU of stress capacity");
        add("solarpunk.ponder.andesite_panel_usage.text_4", "At noon in clear weather: 16 RPM with 4096 SU of stress capacity");
        add("solarpunk.ponder.andesite_panel_usage.text_5", "Rain reduces output to dawn levels - night stops generation entirely");
        add("solarpunk.ponder.andesite_panel_usage.text_6", "The panel must have a clear view of the sky directly above to function");

        add("solarpunk.ponder.brass_panel_usage.header", "Brass Solar Panel");
        add("solarpunk.ponder.brass_panel_usage.text_1", "The Brass Solar Panel generates Forge Energy (FE) directly from sunlight");
        add("solarpunk.ponder.brass_panel_usage.text_2", "Connect cables or conduits to any face to transfer the generated power");
        add("solarpunk.ponder.brass_panel_usage.text_3", "At dawn and dusk: 40 FE/t - at noon in clear weather: 80 FE/t");
        add("solarpunk.ponder.brass_panel_usage.text_4", "Rain reduces output to dawn levels - the panel must have clear sky access above it");

        add("solarpunk.ponder.heat_battery_filling.header", "Filling the Heat Battery");
        add("solarpunk.ponder.heat_battery_filling.text_1", "The Heat Battery stores thermal energy in the form of Molten Salt");
        add("solarpunk.ponder.heat_battery_filling.text_2", "Fill it by right-clicking with a Molten Salt bucket, or piping fluid into any face");
        add("solarpunk.ponder.heat_battery_filling.text_3", "Fill it to capacity for maximum heat output");
        add("solarpunk.ponder.heat_battery_filling.text_4", "The block's glow indicates heat level - brighter means hotter");

        add("solarpunk.ponder.heat_battery_usage.header", "Using the Heat Battery");
        add("solarpunk.ponder.heat_battery_usage.text_1", "A filled Heat Battery acts as a heat source for Create's steam engines");
        add("solarpunk.ponder.heat_battery_usage.text_2", "Place it directly below a Boiler to supply heat - no fuel required");
        add("solarpunk.ponder.heat_battery_usage.text_3", "The heat level depends on how much Molten Salt is stored");
        add("solarpunk.ponder.heat_battery_usage.text_4", "Stored heat dissipates slowly over time - keep it topped up with the Solar Heater");

        add("solarpunk.ponder.kinetic_battery_usage.header", "Using the Kinetic Battery");
        add("solarpunk.ponder.kinetic_battery_usage.text_1", "The Kinetic Battery stores Rotational Force for later use");
        add("solarpunk.ponder.kinetic_battery_usage.text_2", "When connected to a spinning network without a Redstone signal, it charges up");
        add("solarpunk.ponder.kinetic_battery_usage.text_3", "Apply a Redstone signal to discharge - it outputs rotation along its axis");
        add("solarpunk.ponder.kinetic_battery_usage.text_4", "Discharge output: 16 RPM with 256 SU of stress capacity");
        add("solarpunk.ponder.kinetic_battery_usage.text_5", "The battery discharges at a constant speed until empty, then stops");
        add("solarpunk.ponder.kinetic_battery_usage.text_6", "Use it to buffer power from intermittent sources like solar panels");

        add("solarpunk.ponder.geyser_cap_usage.header", "Geyser Cap");
        add("solarpunk.ponder.geyser_cap_usage.text_1", "Geyser Vents naturally generate in hot, arid biomes");
        add("solarpunk.ponder.geyser_cap_usage.text_2", "Place a Geyser Cap directly on top of a Geyser Vent to harness its energy");
        add("solarpunk.ponder.geyser_cap_usage.text_3", "While active, the cap generates 32 RPM with 16384 SU of stress capacity");
        add("solarpunk.ponder.geyser_cap_usage.text_4", "Rotation is output along the cap's sides - connect shafts or machines to collect the power");

        add("solarpunk.ponder.solar_power_tower_usage.header", "Using the Solar Power Tower");
        add("solarpunk.ponder.solar_power_tower_usage.text_1", "Stack Solar Power Tower blocks vertically - they merge into a single multiblock");
        add("solarpunk.ponder.solar_power_tower_usage.text_2", "The tower requires at least a 3x3 footprint and 3 blocks tall to produce anything");
        add("solarpunk.ponder.solar_power_tower_usage.text_3", "Pipe water in through any side face to fill the water tank");
        add("solarpunk.ponder.solar_power_tower_usage.text_4", "During the day with a clear sky, the tower converts water into Molten Salt - rain and night stop production");
        add("solarpunk.ponder.solar_power_tower_usage.text_5", "Solar Mirrors placed nearby link to the tower automatically and boost output - see the Solar Mirrors scene for details");
        add("solarpunk.ponder.solar_power_tower_usage.text_6", "Drain the Molten Salt from the output and pipe it to a Heat Battery");
        add("solarpunk.ponder.solar_power_tower_usage.text_7", "Right-click the tower with a Wrench to switch it to Steam mode - the stored fluid is cleared on switch");
        add("solarpunk.ponder.solar_power_tower_usage.text_8", "In Steam mode the tower produces Steam directly - pipe it to a Steam Turbine or other consumer");

        add("solarpunk.ponder.solar_power_tower_mirrors.header", "Solar Mirrors");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_1", "Solar Mirrors are freestanding - place them on the ground, they don't attach to the tower");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_2", "A mirror links automatically if it has open sky above it and a clear line of sight to the tower");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_3", "The search radius scales with the tower's height, and taller towers can track more mirrors at once");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_4", "Each linked mirror's post turns to face the tower, and its plate tilts to bisect the sun and the tower - tracking the sun all day");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_5", "Efficiency follows a triangle curve peaking at an optimal mirror count for the field - doubling past that drops it back to zero");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_6", "Check a linked tower with Goggles to see its mirror count and current efficiency");

        add("solarpunk.ponder.fermentation_vat_usage.header", "Using the Fermentation Vat");
        add("solarpunk.ponder.fermentation_vat_usage.text_1", "Stack Fermentation Vat blocks - they merge into a single multiblock");
        add("solarpunk.ponder.fermentation_vat_usage.text_2", "The vat needs at least a 2x2 footprint to ferment anything");
        add("solarpunk.ponder.fermentation_vat_usage.text_3", "Pipe water in through any face to fill the water tank");
        add("solarpunk.ponder.fermentation_vat_usage.text_4", "Insert Biomass into the input from the top");
        add("solarpunk.ponder.fermentation_vat_usage.text_5", "The vat ferments Biomass with water into Biofuel - a 2x2 vat consumes 4 Biomass per batch");
        add("solarpunk.ponder.fermentation_vat_usage.text_6", "Drain Biofuel from any face and pipe it to a Biofuel Engine");

        add("solarpunk.ponder.turbine_structure.header", "Building the Steam Turbine");
        add("solarpunk.ponder.turbine_structure.text_1", "Start with a sealed floor - fill the entire 7x7 footprint with Turbine Casing, including the center");
        add("solarpunk.ponder.turbine_structure.text_2", "Above the floor, build ring-only layers with a Rotor at center and Turbine Blades in the plus pattern");
        add("solarpunk.ponder.turbine_structure.text_3", "Two blades per arm, four arms - eight blades total per layer. Andesite is cheaper; Brass is more efficient");
        add("solarpunk.ponder.turbine_structure.text_4", "Stack as many blade layers as you want - each adds more throughput");
        add("solarpunk.ponder.turbine_structure.text_5", "Seal the top the same way as the floor - full 7x7 of Turbine Casing with the Rotor at center");
        add("solarpunk.ponder.turbine_structure.text_6", "Pipe Steam into any face of the outer casing wall - condensate water drains from any casing face too");
        add("solarpunk.ponder.turbine_structure.text_7", "Rotational power exits from the top face of the cap rotor - connect a shaft directly above");

        add("solarpunk.ponder.turbine_condensate.header", "Steam Condensation");
        add("solarpunk.ponder.turbine_condensate.text_1", "As steam drives the rotor it condenses back into water - this condensate collects inside the turbine");
        add("solarpunk.ponder.turbine_condensate.text_2", "Check the condensate water level at any time using Engineer's Goggles on any Rotor block");
        add("solarpunk.ponder.turbine_condensate.text_3", "When the condensate tank is full the turbine shuts down automatically - no more steam is consumed until the water is drained");
        add("solarpunk.ponder.turbine_condensate.text_4", "Pipe the condensate water out of any casing face to drain it - the turbine restarts as soon as there is room");

        add("solarpunk.ponder.turbine_max.header", "Maximum Efficiency Turbine");
        add("solarpunk.ponder.turbine_max.text_1", "The sealed floor is a full 7x7 of Turbine Casing with no rotor - it anchors the structure from below");
        add("solarpunk.ponder.turbine_max.text_2", "A taller turbine consumes more Steam per tick but produces proportionally more SU - height is the main throughput lever");
        add("solarpunk.ponder.turbine_max.text_3", "Fill every blade slot with Brass Blades for the best Steam-to-SU efficiency");
        add("solarpunk.ponder.turbine_max.text_4", "Cap the top identically to the floor - full 7x7 Casing with the Rotor at center. Power exits from the top of this rotor");
        add("solarpunk.ponder.turbine_max.text_5", "Replace any casing with Turbine Casing Glass anywhere in the structure to see inside - still valid");
        add("solarpunk.ponder.turbine_max.text_6", "The maximum is 20 blade layers - at full brass and max height it can power an entire base");

        add("solarpunk.ponder.turbine_horizontal.header", "Building On Its Side");
        add("solarpunk.ponder.turbine_horizontal.text_1", "The Steam Turbine doesn't have to stand up - the same shell works lying on the East/West or North/South axis");
        add("solarpunk.ponder.turbine_horizontal.text_2", "Place the first Rotor against the face pointing the way you want it to grow - the whole structure follows that axis");
        add("solarpunk.ponder.turbine_horizontal.text_3", "It works exactly the same way from there - pipe in Steam, and power exits from the far Rotor");

        add("solarpunk.ponder.fermentation_vat_scaling.header", "Scaling the Fermentation Vat");
        add("solarpunk.ponder.fermentation_vat_scaling.text_1", "A larger footprint increases the batch size - a 2x2 vat processes 4 Biomass at once, a 3x3 processes 9");
        add("solarpunk.ponder.fermentation_vat_scaling.text_2", "Water consumed and Biofuel produced per batch scale with the footprint area");
        add("solarpunk.ponder.fermentation_vat_scaling.text_3", "Taller vats produce super-linearly more Biofuel per batch - the same way a taller Solar Power Tower produces more. Tank capacity also grows with each block added");


        add("solarpunk.ponder.fusion_reactor_structure.header", "Building the Fusion Reactor");
        add("solarpunk.ponder.fusion_reactor_structure.text_1", "The Fusion Reactor Core sits at the exact center of a fixed spherical shell - it doesn't grow taller like the Steam Turbine, it's always this one size");
        add("solarpunk.ponder.fusion_reactor_structure.text_2", "Fill the inner ring band with Blanket Modules - it's an open gyroscope skeleton, not a sealed ball, so the Core stays visible through the gaps. Lithium Breeder for efficient fuel use, Beryllium Reflector for raw output, mixed in whatever ratio you want");
        add("solarpunk.ponder.fusion_reactor_structure.text_3", "Enclose the whole shell in Fusion Reactor Casing - swap in Casing Glass anywhere for a clear view straight through to the open Blanket band underneath");
        add("solarpunk.ponder.fusion_reactor_structure.text_4", "Once the shell and blanket band are complete, the reactor lights up - check it with Goggles to see its status and blanket mix");
        add("solarpunk.ponder.fusion_reactor_structure.text_5", "The Core itself disappears once the reactor is fully formed - all that's left floating here is the glowing rings and crackling energy");
        add("solarpunk.ponder.fusion_reactor_structure.text_6", "More Beryllium in the blanket burns fuel faster for more steam output; more Lithium breeds Tritium more efficiently for a lower, steadier output");
        add("solarpunk.ponder.fusion_reactor_structure.text_7", "Feed it with Deuterium and Lithium - a Deuterium Extractor and Lithium Brine Extractor farm keeps it running");

        add("create.solar_punk.tooltip.biofilter_header", "Biofilter");
        add("create.solar_punk.tooltip.biofilter_status", "Status: ");
        add("create.solar_punk.tooltip.biofilter_pollution", "Chunk Pollution: ");
        add("create.solar_punk.tooltip.biofilter_removing", "Removing: ");

        add("create.solar_punk.tooltip.sprinkler_header", "Sprinkler");
        add("create.solar_punk.tooltip.sprinkler_status", "Status: ");

        add("create.solar_punk.tooltip.fe_header", "Generator Stats");
        add("create.solar_punk.tooltip.generating", "Generating: ");
        add("create.solar_punk.tooltip.stored", "Stored: ");

        add("create.solar_punk.tooltip.kinetic_battery_header", "Kinetic Battery");
        add("create.solar_punk.tooltip.charge", "Charge:");
        add("create.solar_punk.tooltip.runtime", "Runtime:");

        add("create.solar_punk.tooltip.heat_battery_header", "Heat Battery");
        add("create.solar_punk.tooltip.heat_level", "Level: ");
        add("create.solar_punk.tooltip.heat_stored", "Heat: ");
        add("create.solar_punk.tooltip.molten_salt", "Molten Salt: ");
        add("create.solar_punk.tooltip.steam", "Steam: ");
        add("create.solar_punk.tooltip.tower_mode", "Mode: ");
        add("solarpunk.tooltip.tower_mode_salt", "Molten Salt");
        add("solarpunk.tooltip.tower_mode_steam", "Steam");

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
        add("create.solar_punk.tooltip.turbine_not_master", "Not the base rotor");
        add("create.solar_punk.tooltip.turbine_height", "Height: ");
        add("create.solar_punk.tooltip.turbine_blades", "Blades: ");
        add("create.solar_punk.tooltip.condensate_full", "Condensate full - drain water to restart");

        add("create.solar_punk.tooltip.fusion_reactor_core_header", "Fusion Reactor Core");
        add("create.solar_punk.tooltip.reactor_status", "Status: ");
        add("create.solar_punk.tooltip.reactor_blanket", "Blanket: ");
        add("create.solar_punk.tooltip.reactor_steam_output", "Steam Output: ");
        add("create.solar_punk.tooltip.reactor_fuel_draw", "Fuel Draw: ");
        add("create.solar_punk.tooltip.reactor_breeding_efficiency", "Breeding Efficiency: ");
        add("solarpunk.message.reactor_core_obstructed", "Warning: %s positions in the reactor's shell are blocked by unbreakable or out-of-world terrain");

        add("create.solar_punk.tooltip.solar_power_tower_header", "Solar Power Tower");
        add("create.solar_punk.tooltip.mirrors", "Mirrors: ");
        add("create.solar_punk.tooltip.efficiency", "Efficiency: ");

        add("create.solar_punk.tooltip.firebox_boiler_header", "Firebox Boiler");

        add("create.solar_punk.tooltip.heater_header", "Solar Heater");
        add("create.solar_punk.tooltip.melting", "Melting: ");
        add("create.solar_punk.tooltip.progress", "Progress: ");
        add("create.solar_punk.tooltip.water", "Water: ");
        add("create.solar_punk.tooltip.fertilizer", "Fertilizer: ");
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
        add("block.solarpunk.solar_heater.tooltip.behaviour1", "Processes the loaded item. Slows in _rain_.");

        add("block.solarpunk.crystallizer.tooltip.summary", "Runs data-driven recipes that turn up to two fluids into an item, plus an optional fluid byproduct. By default, quenches _Molten Salt_ with _Water_ into _Salt_ and some _Steam_ - the fast/bulk counterpart to the Solar Heater's slow evaporation trickle.");
        add("block.solarpunk.crystallizer.tooltip.condition1", "Any time - no sunlight needed");
        add("block.solarpunk.crystallizer.tooltip.behaviour1", "Pipe both input fluids in from any face; collect the result from the block or a hopper below, and any byproduct fluid from any face.");

        add("block.solarpunk.firebox_boiler.tooltip.summary", "Burns furnace fuel to boil water into _Steam_ for the _Steam Turbine_. A cheap way to get a turbine running before building a Solar Power Tower - nowhere near as efficient at scale.");
        add("block.solarpunk.firebox_boiler.tooltip.condition1", "While lit");
        add("block.solarpunk.firebox_boiler.tooltip.behaviour1", "Converts water to Steam at a fixed rate. Insert fuel by hand or hopper; pipe water in and Steam out of any face.");

        add("block.solarpunk.andesite_solar_panel.tooltip.summary", "Generates _Rotational Force_ from sunlight. Output scales with sun angle and weather.");
        add("block.solarpunk.andesite_solar_panel.tooltip.condition1", "During the day with a clear sky");
        add("block.solarpunk.andesite_solar_panel.tooltip.behaviour1", "8-16 RPM with 1024-4096 SU of stress capacity. Stops in _rain_ and at _night_.");

        add("block.solarpunk.brass_solar_panel.tooltip.summary", "Generates _Forge Energy_ from sunlight. Output scales with sun angle and weather.");
        add("block.solarpunk.brass_solar_panel.tooltip.condition1", "During the day with a clear sky");
        add("block.solarpunk.brass_solar_panel.tooltip.behaviour1", "40-80 FE/t. Stops in _rain_ and at _night_.");

        add("block.solarpunk.heat_battery.tooltip.summary", "Stores thermal energy as _Molten Salt_ and supplies heat to a _Boiler_ placed above it.");
        add("block.solarpunk.heat_battery.tooltip.condition1", "When filled with Molten Salt");
        add("block.solarpunk.heat_battery.tooltip.behaviour1", "Passively heats the boiler above. Heat level fades over time.");

        add("block.solarpunk.kinetic_battery.tooltip.summary", "Stores _Rotational Force_ for later use. Useful for buffering intermittent generators like solar panels.");
        add("block.solarpunk.kinetic_battery.tooltip.condition1", "When connected to a spinning network");
        add("block.solarpunk.kinetic_battery.tooltip.behaviour1", "Charges up while _no Redstone_ signal is applied.");
        add("block.solarpunk.kinetic_battery.tooltip.condition2", "When a Redstone signal is applied");
        add("block.solarpunk.kinetic_battery.tooltip.behaviour2", "Discharges at 16 RPM with 256 SU of stress capacity until empty.");

        add("block.solarpunk.fermentation_vat.tooltip.summary", "Ferments _Biomass_ and water into _Biofuel_. Requires at least a _2x2 footprint_ and _4 blocks tall_ to operate.");
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

        add("block.solarpunk.solar_power_tower.tooltip.summary", "Multiblock tower that concentrates reflected sunlight to produce _Molten Salt_ or _Steam_ from water. Right-click with a Wrench to switch modes.");
        add("block.solarpunk.solar_power_tower.tooltip.condition1", "Minimum 3x3 footprint with Solar Mirrors");
        add("block.solarpunk.solar_power_tower.tooltip.behaviour1", "Output scales with tower size and mirror count. Efficiency peaks at _half_ the available wall faces covered.");

        add("block.solarpunk.solar_mirror.tooltip.summary", "Reflects sunlight toward an adjacent _Solar Power Tower_. Over-mirroring past twice the optimal count reduces output to zero.");

        add("block.solarpunk.biofilter.tooltip.summary", "Draws _Rotational Force_ to absorb _pollution_ from nearby chunks, gradually healing blocks and restoring biomes.");

        add("block.solarpunk.kinetic_sprinkler.tooltip.summary", "Hydrates _farmland_ and accelerates _crop growth_ in a 5x5 area below it.");
        add("block.solarpunk.kinetic_sprinkler.tooltip.condition1", "When supplied with Fertilizer instead of water");
        add("block.solarpunk.kinetic_sprinkler.tooltip.behaviour1", "Forces _instant growth_ on every crop in range each cycle.");

        add("block.solarpunk.turbine_rotor.tooltip.summary", "The power output block of the _Steam Turbine_ multiblock. Pipe _Steam_ into the casing walls and collect _Rotational Force_ from the top face.");
        add("block.solarpunk.turbine_rotor.tooltip.condition1", "Valid 7x7 structure with Turbine Blades");
        add("block.solarpunk.turbine_rotor.tooltip.behaviour1", "Generates power proportional to height and blade efficiency. Drains condensate water from the bottom.");

        AllSolarpunkAdvancements.provideLang(this::add);

        // "Block of ___" naming, matching vanilla's own metal storage blocks
        // (minecraft:iron_block -> "Block of Iron") rather than the generic
        // title-cased "Lithium Block" the auto-generation loop below would produce -
        // excluded from that loop so it doesn't collide with these.
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
