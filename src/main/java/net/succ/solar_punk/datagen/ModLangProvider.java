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

        // Ponder tag — format: <namespace>.ponder.tag.<path>
        add("solarpunk.ponder.tag.solar_machines", "Solar Machines");
        add("solarpunk.ponder.tag.solar_machines.description", "Machines that harness the power of sunlight");
        add("solarpunk.ponder.tag.bio_machines", "Bio Machines");
        add("solarpunk.ponder.tag.bio_machines.description", "Machines for producing and burning Biofuel");
        add("solarpunk.ponder.tag.solar_tower", "Solar Power Tower");
        add("solarpunk.ponder.tag.solar_tower.description", "Concentrated solar power for producing Molten Salt");

        // Ponder scene text — format: <namespace>.ponder.<sceneId>.header / .text_N
        add("solarpunk.ponder.biomass_gasifier_usage.header", "Using the Biomass Gasifier");
        add("solarpunk.ponder.biomass_gasifier_usage.text_1", "The Biomass Gasifier burns Biomass to generate Rotational Force");
        add("solarpunk.ponder.biomass_gasifier_usage.text_2", "Add Biomass into the top slot — each piece burns for 15 seconds");
        add("solarpunk.ponder.biomass_gasifier_usage.text_3", "While burning, it outputs 8 RPM with 2048 SU of stress capacity");
        add("solarpunk.ponder.biomass_gasifier_usage.text_4", "Rotation exits downward — connect a shaft or machine directly below");
        add("solarpunk.ponder.biomass_gasifier_usage.text_5", "Output is intermittent — use a Kinetic Battery to buffer power between refills");

        add("solarpunk.ponder.biofuel_engine_usage.header", "Using the Biofuel Engine");
        add("solarpunk.ponder.biofuel_engine_usage.text_1", "The Biofuel Engine burns Biofuel to generate Rotational Force");
        add("solarpunk.ponder.biofuel_engine_usage.text_2", "Pipe Biofuel into any face to fill the internal tank");
        add("solarpunk.ponder.biofuel_engine_usage.text_3", "With Biofuel present, it outputs 16 RPM with 8192 SU of stress capacity");
        add("solarpunk.ponder.biofuel_engine_usage.text_4", "Rotation exits downward — connect a shaft or machine directly below");

        add("solarpunk.ponder.solar_heater_usage.header", "Using the Solar Heater");
        add("solarpunk.ponder.solar_heater_usage.text_1", "The Solar Heater melts items into fluids using sunlight");
        add("solarpunk.ponder.solar_heater_usage.text_2", "Right-click to place an item into the input slot");
        add("solarpunk.ponder.solar_heater_usage.text_3", "During the day with clear skies, the heater slowly processes the item");
        add("solarpunk.ponder.solar_heater_usage.text_4", "Rain halves the processing speed");
        add("solarpunk.ponder.solar_heater_usage.text_5", "The resulting fluid fills the output tank — drain it with a pipe or bucket");

        add("solarpunk.ponder.solar_heater_evaporation.header", "Evaporating Salt");
        add("solarpunk.ponder.solar_heater_evaporation.text_1", "The Solar Heater can also evaporate water into Salt");
        add("solarpunk.ponder.solar_heater_evaporation.text_2", "Fill the water tank by right-clicking with a water bucket, or pipe water in from the side");
        add("solarpunk.ponder.solar_heater_evaporation.text_3", "With sunlight present, water slowly evaporates and Salt accumulates in the second slot");
        add("solarpunk.ponder.solar_heater_evaporation.text_4", "Salt can be smelted into Molten Salt directly in the Solar Heater");

        add("solarpunk.ponder.andesite_panel_usage.header", "Andesite Solar Panel");
        add("solarpunk.ponder.andesite_panel_usage.text_1", "The Andesite Solar Panel generates Rotational Force from sunlight");
        add("solarpunk.ponder.andesite_panel_usage.text_2", "It outputs rotation downwards — connect a shaft or machine directly below");
        add("solarpunk.ponder.andesite_panel_usage.text_3", "At dawn and dusk: 8 RPM with 1024 SU of stress capacity");
        add("solarpunk.ponder.andesite_panel_usage.text_4", "At noon in clear weather: 16 RPM with 4096 SU of stress capacity");
        add("solarpunk.ponder.andesite_panel_usage.text_5", "Rain reduces output to dawn levels — night stops generation entirely");
        add("solarpunk.ponder.andesite_panel_usage.text_6", "The panel must have a clear view of the sky directly above to function");

        add("solarpunk.ponder.brass_panel_usage.header", "Brass Solar Panel");
        add("solarpunk.ponder.brass_panel_usage.text_1", "The Brass Solar Panel generates Forge Energy (FE) directly from sunlight");
        add("solarpunk.ponder.brass_panel_usage.text_2", "Connect cables or conduits to any face to transfer the generated power");
        add("solarpunk.ponder.brass_panel_usage.text_3", "At dawn and dusk: 40 FE/t — at noon in clear weather: 80 FE/t");
        add("solarpunk.ponder.brass_panel_usage.text_4", "Rain reduces output to dawn levels — the panel must have clear sky access above it");

        add("solarpunk.ponder.heat_battery_filling.header", "Filling the Heat Battery");
        add("solarpunk.ponder.heat_battery_filling.text_1", "The Heat Battery stores thermal energy in the form of Molten Salt");
        add("solarpunk.ponder.heat_battery_filling.text_2", "Fill it by right-clicking with a Molten Salt bucket, or piping fluid into any face");
        add("solarpunk.ponder.heat_battery_filling.text_3", "Fill it to capacity for maximum heat output");
        add("solarpunk.ponder.heat_battery_filling.text_4", "The block's glow indicates heat level — brighter means hotter");

        add("solarpunk.ponder.heat_battery_usage.header", "Using the Heat Battery");
        add("solarpunk.ponder.heat_battery_usage.text_1", "A filled Heat Battery acts as a heat source for Create's steam engines");
        add("solarpunk.ponder.heat_battery_usage.text_2", "Place it directly below a Boiler to supply heat — no fuel required");
        add("solarpunk.ponder.heat_battery_usage.text_3", "The heat level depends on how much Molten Salt is stored");
        add("solarpunk.ponder.heat_battery_usage.text_4", "Stored heat dissipates slowly over time — keep it topped up with the Solar Heater");

        add("solarpunk.ponder.kinetic_battery_usage.header", "Using the Kinetic Battery");
        add("solarpunk.ponder.kinetic_battery_usage.text_1", "The Kinetic Battery stores Rotational Force for later use");
        add("solarpunk.ponder.kinetic_battery_usage.text_2", "When connected to a spinning network without a Redstone signal, it charges up");
        add("solarpunk.ponder.kinetic_battery_usage.text_3", "Apply a Redstone signal to discharge — it outputs rotation along its axis");
        add("solarpunk.ponder.kinetic_battery_usage.text_4", "Discharge output: 16 RPM with 256 SU of stress capacity");
        add("solarpunk.ponder.kinetic_battery_usage.text_5", "The battery discharges at a constant speed until empty, then stops");
        add("solarpunk.ponder.kinetic_battery_usage.text_6", "Use it to buffer power from intermittent sources like solar panels");

        add("solarpunk.ponder.geyser_cap_usage.header", "Geyser Cap");
        add("solarpunk.ponder.geyser_cap_usage.text_1", "Geyser Vents naturally generate in hot, arid biomes");
        add("solarpunk.ponder.geyser_cap_usage.text_2", "Place a Geyser Cap directly on top of a Geyser Vent to harness its energy");
        add("solarpunk.ponder.geyser_cap_usage.text_3", "While active, the cap generates 32 RPM with 16384 SU of stress capacity");
        add("solarpunk.ponder.geyser_cap_usage.text_4", "Rotation is output along the cap's sides — connect shafts or machines to collect the power");

        add("solarpunk.ponder.solar_power_tower_usage.header", "Using the Solar Power Tower");
        add("solarpunk.ponder.solar_power_tower_usage.text_1", "Stack Solar Power Tower blocks vertically — they merge into a single multiblock");
        add("solarpunk.ponder.solar_power_tower_usage.text_2", "The tower requires at least a 3x3 footprint and 3 blocks tall to produce anything");
        add("solarpunk.ponder.solar_power_tower_usage.text_3", "Pipe water in through any side face to fill the water tank");
        add("solarpunk.ponder.solar_power_tower_usage.text_4", "During the day with a clear sky, the tower converts water into Molten Salt — rain and night stop production");
        add("solarpunk.ponder.solar_power_tower_usage.text_5", "Place Solar Mirrors on the tower's side faces to boost output — see the Solar Mirrors scene for details");
        add("solarpunk.ponder.solar_power_tower_usage.text_6", "Drain the Molten Salt from the output and pipe it to a Heat Battery");

        add("solarpunk.ponder.solar_power_tower_mirrors.header", "Solar Mirrors");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_1", "Place Solar Mirrors directly against the tower's side faces to increase output");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_2", "Mirror efficiency follows a triangle curve — it peaks when roughly half the available side faces are covered");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_3", "Adding mirrors past twice the optimal count reduces efficiency to zero — do not over-mirror");
        add("solarpunk.ponder.solar_power_tower_mirrors.text_4", "Taller and wider towers have more wall space for mirrors and a higher maximum output rate");

        add("solarpunk.ponder.fermentation_vat_usage.header", "Using the Fermentation Vat");
        add("solarpunk.ponder.fermentation_vat_usage.text_1", "Stack Fermentation Vat blocks — they merge into a single multiblock");
        add("solarpunk.ponder.fermentation_vat_usage.text_2", "The vat needs at least a 2x2 footprint to ferment anything");
        add("solarpunk.ponder.fermentation_vat_usage.text_3", "Pipe water in through any face to fill the water tank");
        add("solarpunk.ponder.fermentation_vat_usage.text_4", "Insert Biomass into the input from the top");
        add("solarpunk.ponder.fermentation_vat_usage.text_5", "The vat ferments Biomass with water into Biofuel — a 2x2 vat consumes 4 Biomass per batch");
        add("solarpunk.ponder.fermentation_vat_usage.text_6", "Drain Biofuel from any face and pipe it to a Biofuel Engine");

        add("solarpunk.ponder.fermentation_vat_scaling.header", "Scaling the Fermentation Vat");
        add("solarpunk.ponder.fermentation_vat_scaling.text_1", "A larger footprint increases the batch size — a 2x2 vat processes 4 Biomass at once, a 3x3 processes 9");
        add("solarpunk.ponder.fermentation_vat_scaling.text_2", "Water consumed and Biofuel produced per batch scale with the footprint area");
        add("solarpunk.ponder.fermentation_vat_scaling.text_3", "Tank capacity grows with each block added — taller vats hold more fluid");

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

        add("create.solar_punk.tooltip.fermentation_vat_header", "Fermentation Vat");
        add("create.solar_punk.tooltip.vat_too_small", "Needs at least 2x2 footprint");
        add("create.solar_punk.tooltip.vat_batch_scale", "Batch size: ");
        add("create.solar_punk.tooltip.fermenting", "Fermenting: ");
        add("create.solar_punk.tooltip.biofuel", "Biofuel: ");

        add("create.solar_punk.tooltip.gasifier_header", "Biomass Gasifier");
        add("create.solar_punk.tooltip.fuel", "Fuel: ");
        add("create.solar_punk.tooltip.burn_time", "Burn time: ");
        add("create.solar_punk.tooltip.biomass_stored", "Biomass: ");
        add("create.solar_punk.tooltip.biochar_stored", "Biochar: ");

        add("create.solar_punk.tooltip.biofuel_engine_header", "Biofuel Engine");
        add("create.solar_punk.tooltip.consumption", "Consumption: ");

        add("create.solar_punk.tooltip.solar_power_tower_header", "Solar Power Tower");
        add("create.solar_punk.tooltip.mirrors", "Mirrors: ");
        add("create.solar_punk.tooltip.efficiency", "Efficiency: ");

        add("create.solar_punk.tooltip.heater_header", "Solar Heater");
        add("create.solar_punk.tooltip.melting", "Melting: ");
        add("create.solar_punk.tooltip.progress", "Progress: ");
        add("create.solar_punk.tooltip.water", "Water: ");
        add("create.solar_punk.tooltip.evaporation", "Evaporation: ");
        add("create.solar_punk.tooltip.output_fluid", "Output: ");
        add("create.solar_punk.tooltip.salt_output", "Salt: ");

        ModBlocks.BLOCKS.getEntries().forEach(entry ->
                add(entry.get(), toTitleCase(entry.getId().getPath())));

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
