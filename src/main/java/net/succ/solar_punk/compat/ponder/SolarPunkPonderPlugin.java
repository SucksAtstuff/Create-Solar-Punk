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
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        SolarPunkPonderTags.register(helper);
    }
}