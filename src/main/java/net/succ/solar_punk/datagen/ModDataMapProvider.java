package net.succ.solar_punk.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.succ.solar_punk.datamap.ModDataMaps;
import net.succ.solar_punk.datamap.ModDataMaps.EngineFuel;
import net.succ.solar_punk.datamap.ModDataMaps.GasifierFuel;
import net.succ.solar_punk.datamap.ModDataMaps.HeatBatteryFluid;
import net.succ.solar_punk.datamap.ModDataMaps.TurbineFuel;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

import java.util.concurrent.CompletableFuture;

import static net.succ.offbyonelib.datagen.DatagenUtil.itemTag;

/** Default values for ModDataMaps, the same ones the machines had before they were data maps. */
public class ModDataMapProvider extends DataMapProvider {

    public ModDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(ModDataMaps.TURBINE_FUELS)
                .add(ModFluids.STEAM_SOURCE.get().builtInRegistryHolder(), new TurbineFuel(1f), false)
                .add(ResourceLocation.fromNamespaceAndPath("northstar", "liquid_hydrogen"), new TurbineFuel(1f), false,
                        new ModLoadedCondition("northstar"));

        // Later entries win, and the file's keys are written sorted: "#c:bio_fuels" sorts before
        // "solarpunk:biomass_pellet", so the Pellet's own entry beats the tag (which holds it too)
        builder(ModDataMaps.GASIFIER_FUELS)
                .add(itemTag("c", "bio_fuels"), new GasifierFuel(200, 1), false)
                .add(ModItems.BIOMASS_PELLET.get().builtInRegistryHolder(), new GasifierFuel(400, 2), false);

        // 50 mB every 2 seconds
        builder(ModDataMaps.ENGINE_FUELS)
                .add(ModFluids.BIOFUEL_SOURCE.get().builtInRegistryHolder(), new EngineFuel(800), false);

        builder(ModDataMaps.HEAT_BATTERY_FLUIDS)
                .add(ModFluids.MOLTEN_SALT_SOURCE.get().builtInRegistryHolder(), new HeatBatteryFluid(10), false);
    }
}
