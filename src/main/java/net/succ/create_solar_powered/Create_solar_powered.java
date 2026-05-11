package net.succ.create_solar_powered;

import com.simibubi.create.api.boiler.BoilerHeater;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.block.entity.custom.HeatBatteryBlockEntity;
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
        modEventBus.addListener(Create_solar_powered::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> be.itemHandler
        );
        // Top face intentionally blocked — a pipe there would occlude the sky check.
        // Water fill and molten salt drain both route through the combined handler on all other faces.
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
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
            BoilerHeater.REGISTRY.register(ModBlocks.HEAT_BATTERY.get(), (level, pos, state) -> {
                if (!(level.getBlockEntity(pos) instanceof HeatBatteryBlockEntity be))
                    return BoilerHeater.NO_HEAT;
                return be.getHeatLevel();
            })
        );
    }
}
