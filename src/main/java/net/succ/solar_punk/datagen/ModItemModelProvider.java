package net.succ.solar_punk.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.SALT.get());
        basicItem(ModItems.BIOMASS.get());
        basicItem(ModItems.BIOMASS_PELLET.get());
        basicItem(ModItems.BIOCHAR.get());
        basicItem(ModItems.RAW_LITHIUM.get());
        basicItem(ModItems.LITHIUM_INGOT.get());
        basicItem(ModItems.LITHIUM_NUGGET.get());
        basicItem(ModItems.LITHIUM_DUST.get());
        basicItem(ModItems.BERYLLIUM_DUST.get());
        basicItem(ModItems.BERYLLIUM_INGOT.get());
        basicItem(ModItems.BERYLLIUM_NUGGET.get());
        basicItem(ModItems.PHOTOVOLTAIC_CELL.get());
        basicItem(ModItems.INCOMPLETE_PHOTOVOLTAIC_CELL.get());
        // No separate incomplete texture yet, so this reuses the finished block's model
        withExistingParent(ModItems.INCOMPLETE_SOLAR_MIRROR.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_mirror"));
        basicItem(ModItems.ELECTROMAGNET_COIL.get());
        basicItem(ModItems.INCOMPLETE_ELECTROMAGNET_COIL.get());
        basicItem(ModItems.SUPERCONDUCTING_COIL.get());
        basicItem(ModItems.INCOMPLETE_SUPERCONDUCTING_COIL.get());
        basicItem(ModItems.EMPTY_FUEL_CAPSULE.get());
        basicItem(ModItems.TRITIUM_CAPSULE.get());
        basicItem(ModFluids.MOLTEN_SALT_BUCKET.get());
        basicItem(ModFluids.BIOFUEL_BUCKET.get());
        basicItem(ModFluids.FERTILIZER_BUCKET.get());
        basicItem(ModFluids.STEAM_BUCKET.get());
        basicItem(ModFluids.DEUTERIUM_BUCKET.get());
    }
}
