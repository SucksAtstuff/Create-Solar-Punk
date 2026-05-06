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
