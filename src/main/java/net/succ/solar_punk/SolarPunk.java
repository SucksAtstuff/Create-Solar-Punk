package net.succ.solar_punk;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.api.stress.BlockStressValues.GeneratedRpm;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;
import net.succ.solar_punk.datagen.DataGenerators;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.fluid.ModFluidTypes;
import net.succ.solar_punk.item.ModCreativeModeTabs;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.ModRecipeTypes;
import net.succ.solar_punk.sound.ModSounds;
import net.succ.solar_punk.worldgen.ModFeatures;

@Mod(SolarPunk.MODID)
public class SolarPunk {
    public static final String MODID = "solarpunk";

    public SolarPunk(IEventBus modEventBus, ModContainer modContainer) {
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModFeatures.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(SolarPunk::registerCapabilities);
        modEventBus.addListener(SolarPunk::commonSetup);

        NeoForge.EVENT_BUS.addListener(GlobalWarmingHandler::onLevelTick);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> be.itemHandler
        );
        // Top face intentionally blocked — a pipe there would occlude the sky check.
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> side == Direction.UP ? null : be.combinedFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BRASS_SOLAR_PANEL.get(),
                (be, side) -> be.energyStorage
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.HEAT_BATTERY.get(),
                (be, side) -> be.fluidTank
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FERMENTATION_VAT.get(),
                (be, side) -> {
                    FermentationVatBlockEntity ctrl = be.getControllerBE();
                    return ctrl != null ? ctrl.itemHandler : null;
                }
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FERMENTATION_VAT.get(),
                (be, side) -> be.combinedFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BIOMASS_GASIFIER.get(),
                (be, side) -> be.itemHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.BIOFUEL_ENGINE.get(),
                (be, side) -> be.biofuelTank
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.SOLAR_POWER_TOWER.get(),
                (be, side) -> be.combinedFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.KINETIC_SPRINKLER.get(),
                (be, side) -> be.fluidTank
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.TURBINE_ROTOR.get(),
                (be, side) -> {
                    if (!be.isMaster) return null;
                    if (side == Direction.UP) return null; // shaft face
                    return be.combinedFluidHandler;
                }
        );
        // Pipes on any outer casing face proxy to the master rotor's fluid handler.
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> {
                    for (int dy = -23; dy <= 23; dy++)
                        for (int dx = -3; dx <= 3; dx++)
                            for (int dz = -3; dz <= 3; dz++)
                                if (level.getBlockEntity(pos.offset(dx, dy, dz)) instanceof TurbineRotorBlockEntity r && r.isMaster)
                                    return r.combinedFluidHandler;
                    return null;
                },
                ModBlocks.TURBINE_CASING.get(), ModBlocks.TURBINE_CASING_GLASS.get());
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BoilerHeater.REGISTRY.register(ModBlocks.HEAT_BATTERY.get(), (level, pos, state) -> {
                if (!(level.getBlockEntity(pos) instanceof HeatBatteryBlockEntity be))
                    return BoilerHeater.NO_HEAT;
                return be.getHeatLevel();
            });

            // Generators — capacity (per RPM) and generated RPM for stress tooltips
            BlockStressValues.CAPACITIES.register(ModBlocks.ANDESITE_SOLAR_PANEL.get(),
                () -> (double) Config.andesiteNoonSu);
            BlockStressValues.RPM.register(ModBlocks.ANDESITE_SOLAR_PANEL.get(),
                new GeneratedRpm(Config.andesiteNoonRpm, true));

            BlockStressValues.CAPACITIES.register(ModBlocks.BIOMASS_GASIFIER.get(),
                () -> (double) Config.gasifierSu);
            BlockStressValues.RPM.register(ModBlocks.BIOMASS_GASIFIER.get(),
                new GeneratedRpm(Config.gasifierRpm, false));

            BlockStressValues.CAPACITIES.register(ModBlocks.BIOFUEL_ENGINE.get(),
                () -> (double) Config.biofuelEngineSu);
            BlockStressValues.RPM.register(ModBlocks.BIOFUEL_ENGINE.get(),
                new GeneratedRpm(Config.biofuelEngineRpm, false));

            BlockStressValues.CAPACITIES.register(ModBlocks.GEYSER_CAP.get(),
                () -> (double) Config.geyserCapSu);
            BlockStressValues.RPM.register(ModBlocks.GEYSER_CAP.get(),
                new GeneratedRpm(Config.geyserCapRpm, false));

            BlockStressValues.CAPACITIES.register(ModBlocks.KINETIC_BATTERY.get(),
                () -> (double) Config.kineticBatterySu);
            BlockStressValues.RPM.register(ModBlocks.KINETIC_BATTERY.get(),
                new GeneratedRpm(Config.kineticBatteryRpm, false));

            BlockStressValues.CAPACITIES.register(ModBlocks.TURBINE_ROTOR.get(),
                () -> (double) Config.turbineSuPerLayer * 20);
            BlockStressValues.RPM.register(ModBlocks.TURBINE_ROTOR.get(),
                new GeneratedRpm(Config.turbineMaxRpm, false));

            // Consumer — stress impact for tooltip
            BlockStressValues.IMPACTS.register(ModBlocks.BIOFILTER.get(),
                () -> (double) Config.biofilterSu);
        });
    }
}
