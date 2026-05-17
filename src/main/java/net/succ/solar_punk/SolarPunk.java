package net.succ.solar_punk;

import com.simibubi.create.api.boiler.BoilerHeater;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.datagen.DataGenerators;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.fluid.ModFluidTypes;
import net.succ.solar_punk.item.ModCreativeModeTabs;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.recipe.ModRecipeTypes;
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
        ModFeatures.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(SolarPunk::registerCapabilities);
        modEventBus.addListener(SolarPunk::commonSetup);

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
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BoilerHeater.REGISTRY.register(ModBlocks.HEAT_BATTERY.get(), (level, pos, state) -> {
                if (!(level.getBlockEntity(pos) instanceof HeatBatteryBlockEntity be))
                    return BoilerHeater.NO_HEAT;
                return be.getHeatLevel();
            });

        });
    }
}
