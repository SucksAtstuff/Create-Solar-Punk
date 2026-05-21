package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

public class SolarPunkPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return SolarPunk.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.BIOMASS_GASIFIER.getId(), ModBlocks.BIOFUEL_ENGINE.getId(), ModBlocks.FERMENTATION_VAT.getId())
                .addStoryBoard("biomass_gasifier/usage",   BiomassGasifierScenes::usage,   SolarPunkPonderTags.BIO_MACHINES)
                .addStoryBoard("biofuel_engine/usage",     BiofuelEngineScenes::usage,     SolarPunkPonderTags.BIO_MACHINES)
                .addStoryBoard("fermentation_vat/usage",   FermentationVatScenes::usage,   SolarPunkPonderTags.BIO_MACHINES)
                .addStoryBoard("fermentation_vat/scaling", FermentationVatScenes::scaling, SolarPunkPonderTags.BIO_MACHINES);

        helper.forComponents(ModBlocks.SOLAR_HEATER.getId())
                .addStoryBoard("solar_heater/usage", SolarHeaterScenes::usage, SolarPunkPonderTags.SOLAR_MACHINES)
                .addStoryBoard("solar_heater/evaporation", SolarHeaterScenes::evaporation, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.addStoryBoard(ModBlocks.ANDESITE_SOLAR_PANEL.getId(), "solar_panel/andesite",
                SolarPanelScenes::andesiteUsage, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.addStoryBoard(ModBlocks.BRASS_SOLAR_PANEL.getId(), "solar_panel/brass",
                SolarPanelScenes::brassUsage, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.forComponents(ModBlocks.HEAT_BATTERY.getId())
                .addStoryBoard("heat_battery/filling", HeatBatteryScenes::filling, SolarPunkPonderTags.SOLAR_MACHINES)
                .addStoryBoard("heat_battery/usage", HeatBatteryScenes::usage, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.addStoryBoard(ModBlocks.KINETIC_BATTERY.getId(), "kinetic_battery/usage",
                KineticBatteryScenes::usage, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.addStoryBoard(ModBlocks.GEYSER_CAP.getId(), "geyser_cap/usage",
                GeyserCapScenes::usage, SolarPunkPonderTags.SOLAR_MACHINES);

        helper.forComponents(ModBlocks.SOLAR_POWER_TOWER.getId(), ModBlocks.SOLAR_MIRROR.getId())
                .addStoryBoard("solar_power_tower/usage",   SolarPowerTowerScenes::usage,   SolarPunkPonderTags.SOLAR_TOWER)
                .addStoryBoard("solar_power_tower/mirrors", SolarPowerTowerScenes::mirrors, SolarPunkPonderTags.SOLAR_TOWER);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        SolarPunkPonderTags.register(helper);
    }
}