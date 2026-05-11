package net.succ.create_solar_powered.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.item.ModItems;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput output) {
        super(output, Create_solar_powered.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.create_solar_powered", "Create: Solar Powered");
        add("fluid_type.create_solar_powered.molten_salt", "Molten Salt");

        add("create.create_solar_powered.tooltip.fe_header", "Generator Stats");
        add("create.create_solar_powered.tooltip.generating", "Generating: ");
        add("create.create_solar_powered.tooltip.stored", "Stored: ");

        add("create.create_solar_powered.tooltip.heater_header", "Solar Heater");
        add("create.create_solar_powered.tooltip.melting", "Melting: ");
        add("create.create_solar_powered.tooltip.progress", "Progress: ");
        add("create.create_solar_powered.tooltip.water", "Water: ");
        add("create.create_solar_powered.tooltip.evaporation", "Evaporation: ");
        add("create.create_solar_powered.tooltip.output_fluid", "Output: ");
        add("create.create_solar_powered.tooltip.salt_output", "Salt: ");

        ModBlocks.BLOCKS.getEntries().forEach(entry ->
                add(entry.get(), toTitleCase(entry.getId().getPath())));

        ModItems.ITEMS.getEntries().stream()
                .filter(entry -> !(entry.get() instanceof BlockItem))
                .forEach(entry -> add(entry.get(), toTitleCase(entry.getId().getPath())));
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
