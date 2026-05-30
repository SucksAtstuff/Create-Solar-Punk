package net.succ.solar_punk.compat.ponder;

import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

public class SolarPunkPonderTags {

    public static final ResourceLocation SOLAR_MACHINES =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_machines");

    public static final ResourceLocation BIO_MACHINES =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "bio_machines");

    public static final ResourceLocation SOLAR_TOWER =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "solar_tower");

    public static final ResourceLocation STEAM_TURBINE =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "steam_turbine");

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
                .add(ModBlocks.KINETIC_BATTERY.getId())
                .add(ModBlocks.GEYSER_CAP.getId());

        helper.registerTag(BIO_MACHINES)
                .addToIndex()
                .item(ModBlocks.BIOMASS_GASIFIER.get(), true, false)
                .title("Bio Machines")
                .description("Machines for producing and burning Biofuel")
                .register();

        helper.addToTag(BIO_MACHINES)
                .add(ModBlocks.BIOMASS_GASIFIER.getId())
                .add(ModBlocks.BIOFUEL_ENGINE.getId())
                .add(ModBlocks.FERMENTATION_VAT.getId());

        helper.registerTag(SOLAR_TOWER)
                .addToIndex()
                .item(ModBlocks.SOLAR_POWER_TOWER.get(), true, false)
                .title("Solar Power Tower")
                .description("Concentrated solar power for producing Molten Salt")
                .register();

        helper.addToTag(SOLAR_TOWER)
                .add(ModBlocks.SOLAR_POWER_TOWER.getId())
                .add(ModBlocks.SOLAR_MIRROR.getId());

        helper.registerTag(STEAM_TURBINE)
                .addToIndex()
                .item(ModBlocks.TURBINE_ROTOR.get(), true, false)
                .title("Steam Turbine")
                .description("High-throughput steam-powered rotational force generator")
                .register();

        helper.addToTag(STEAM_TURBINE)
                .add(ModBlocks.TURBINE_ROTOR.getId())
                .add(ModBlocks.TURBINE_CASING.getId())
                .add(ModBlocks.TURBINE_CASING_GLASS.getId())
                .add(ModBlocks.ANDESITE_TURBINE_BLADE.getId())
                .add(ModBlocks.BRASS_TURBINE_BLADE.getId());
    }
}