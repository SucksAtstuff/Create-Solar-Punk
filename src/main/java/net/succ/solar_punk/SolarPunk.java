package net.succ.solar_punk;

import net.succ.solar_punk.block.custom.AcceleratorRing;
import com.simibubi.create.api.behaviour.display.DisplaySource;
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
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import net.succ.solar_punk.block.entity.custom.HeatBatteryBlockEntity;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;
import net.succ.solar_punk.datagen.DataGenerators;
import net.succ.solar_punk.display.ModDisplaySources;
import net.succ.solar_punk.fluid.ModFluidTypes;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModCreativeModeTabs;
import net.succ.solar_punk.item.ModDataComponents;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.network.SelectPipeShapePayload;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import net.succ.solar_punk.recipe.ModRecipeTypes;
import net.succ.solar_punk.sound.ModSounds;
import net.succ.solar_punk.worldgen.ModBiomeModifierTypes;
import net.succ.solar_punk.worldgen.ModFeatures;

@Mod(SolarPunk.MODID)
public class SolarPunk {
    public static final String MODID = "solarpunk";

    public SolarPunk(IEventBus modEventBus, ModContainer modContainer) {
        ModFluidTypes.register(modEventBus);
        ModFluids.register(modEventBus);
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModBiomeModifierTypes.register(modEventBus);
        ModTriggers.register(modEventBus);
        ModDisplaySources.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(SolarPunk::registerCapabilities);
        modEventBus.addListener(SolarPunk::commonSetup);
        modEventBus.addListener(SelectPipeShapePayload::register);

        NeoForge.EVENT_BUS.addListener(GlobalWarmingHandler::onLevelTick);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SOLAR_HEATER.get(),
                (be, side) -> be.automationHandler
        );
        // Particle Accelerator Input: items through the cap, the opposite side and the top, so
        // one face can feed it while another pulls results out. Not through the pipe ends or
        // the shaft underneath (null side = internal access).
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get(),
                (be, side) -> {
                    if (side == null || side == Direction.UP) return be.capHandler;
                    Direction cap = AcceleratorRing.capSide(be.getBlockState());
                    return side == cap || side == cap.getOpposite() ? be.capHandler : null;
                }
        );
        // Top face intentionally blocked: a pipe there would block the sky check.
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
        // No sky check here (unlike the Solar Heater), so every face is open.
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CRYSTALLIZER.get(),
                (be, side) -> be.itemHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.CRYSTALLIZER.get(),
                (be, side) -> be.combinedFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FIREBOX_BOILER.get(),
                (be, side) -> be.automationHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FIREBOX_BOILER.get(),
                (be, side) -> be.combinedFluidHandler
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
                (be, side) -> be.automationHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BIOFILTER.get(),
                (be, side) -> be.automationHandler
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
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.TURBINE_CASING.get(),
                (be, side) -> be.getFluidHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FUSION_REACTOR_CORE.get(),
                (be, side) -> be.combinedFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FUSION_REACTOR_CORE.get(),
                (be, side) -> be.itemHandler
        );
        // The Core itself is buried at dead center of the sealed Casing sphere and
        // unreachable by any pipe/hopper once the reactor is built - the direct Core
        // registrations above only ever matter mid-construction, before the shell
        // closes. Every exterior Casing block proxies through to the same tanks and slots.
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FUSION_REACTOR_CASING.get(),
                (be, side) -> be.getFluidHandler()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.FUSION_REACTOR_CASING.get(),
                (be, side) -> be.getItemHandler()
        );
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BoilerHeater.REGISTRY.register(ModBlocks.HEAT_BATTERY.get(), (level, pos, state) -> {
                if (!(level.getBlockEntity(pos) instanceof HeatBatteryBlockEntity be))
                    return BoilerHeater.NO_HEAT;
                return be.getHeatLevel();
            });

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.HEAT_BATTERY.get(), ModDisplaySources.HEAT_BATTERY_CHARGE.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.HEAT_BATTERY.get(), ModDisplaySources.HEAT_BATTERY_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.KINETIC_BATTERY.get(), ModDisplaySources.KINETIC_BATTERY_CHARGE.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.SOLAR_POWER_TOWER.get(), ModDisplaySources.SOLAR_POWER_TOWER_EFFICIENCY.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.SOLAR_POWER_TOWER.get(), ModDisplaySources.SOLAR_POWER_TOWER_MIRROR_COUNT.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.SOLAR_POWER_TOWER.get(), ModDisplaySources.SOLAR_POWER_TOWER_GENERATION_RATE.get());
            // Registered on both the Core and its Casing shell - the Core is buried at dead
            // center and unreachable once the reactor is built (see FusionReactorCasingBlockEntity),
            // so the Casing registration is what actually matters post-construction.
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CORE.get(), ModDisplaySources.FUSION_REACTOR_LITHIUM_BUFFER.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_LITHIUM_BUFFER.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CORE.get(), ModDisplaySources.FUSION_REACTOR_TRITIUM_SUPPLY.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_TRITIUM_SUPPLY.get());
            // The rest are Casing-only (Casing and Casing Glass share this BE type) - read
            // from the outside of the shell, never the buried Core.
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_STEAM_OUTPUT.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_THROTTLE.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_DEUTERIUM.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.FUSION_REACTOR_CASING.get(), ModDisplaySources.FUSION_REACTOR_STEAM_TANK.get());

            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get(), ModDisplaySources.PARTICLE_ACCELERATOR_STATUS.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get(), ModDisplaySources.PARTICLE_ACCELERATOR_RECIPE.get());
            DisplaySource.BY_BLOCK_ENTITY.add(ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get(), ModDisplaySources.PARTICLE_ACCELERATOR_PROGRESS.get());

            // Generators: capacity and generated RPM for stress tooltips
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
                () -> (double) Config.turbineSuPerLayer * TurbineRotorBlockEntity.MAX_LAYERS);
            BlockStressValues.RPM.register(ModBlocks.TURBINE_ROTOR.get(),
                new GeneratedRpm(Config.turbineMaxRpm, false));

            // Consumer: stress impact for tooltip
            BlockStressValues.IMPACTS.register(ModBlocks.BIOFILTER.get(),
                () -> (double) Config.biofilterSu);
        });
    }
}
