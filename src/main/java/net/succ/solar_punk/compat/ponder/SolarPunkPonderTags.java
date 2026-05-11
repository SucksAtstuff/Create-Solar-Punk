package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

public class SolarPunkPonderTags {

    public static final ResourceLocation SOLAR_MACHINES =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_machines");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(SOLAR_MACHINES)
                .addToIndex()
                .item(ModBlocks.SOLAR_HEATER.get(), true, false)
                .title("Solar Machines")
                .description("Machines that harness the power of sunlight")
                .register();

        helper.addToTag(SOLAR_MACHINES)
                .add(ModBlocks.SOLAR_HEATER.getId())
                .add(ModBlocks.ANDESITE_SOLAR_PANEL.getId())
                .add(ModBlocks.BRASS_SOLAR_PANEL.getId())
                .add(ModBlocks.HEAT_BATTERY.getId())
                .add(ModBlocks.KINETIC_BATTERY.getId());
    }
}