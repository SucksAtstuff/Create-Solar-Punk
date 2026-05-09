package net.succ.create_solar_powered;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;
import net.succ.create_solar_powered.datagen.DataGenerators;
import net.succ.create_solar_powered.fluid.ModFluids;
import net.succ.create_solar_powered.fluid.ModFluidTypes;
import net.succ.create_solar_powered.item.ModCreativeModeTabs;
import net.succ.create_solar_powered.item.ModItems;
import net.succ.create_solar_powered.recipe.ModRecipeTypes;

@Mod(Create_solar_powered.MODID)
public class Create_solar_powered {
    public static final String MODID = "create_solar_powered";

    public Create_solar_powered(IEventBus modEventBus, ModContainer modContainer) {
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipeTypes.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(Create_solar_powered::registerCapabilities);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> be.itemHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> be.fluidTank
        );
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.BRASS_SOLAR_PANEL.get(),
                (be, side) -> be.energyStorage
        );
    }
}
