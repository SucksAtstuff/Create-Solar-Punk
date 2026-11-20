package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;

public class ModSpriteShifts {

    private static final String VAT             = "block/fermentation_vat/";
    private static final String TOWER           = "block/solar_power_tower/";
    private static final String BIOREACTOR      = "block/bioreactor/";
    private static final String CASING          = "block/industrial_iron_casing/";
    private static final String REACTOR_CASING  = "block/fusion_reactor_casing/";

    public static final CTSpriteShiftEntry
        FERMENTATION_VAT       = shift(VAT,    "fermentation_vat"),
        FERMENTATION_VAT_TOP   = shift(VAT,    "fermentation_vat_top"),
        FERMENTATION_VAT_INNER = shift(VAT,    "fermentation_vat_inner"),
        BIOREACTOR_TANK         = shift(BIOREACTOR, "bioreactor"),
        BIOREACTOR_TANK_TOP     = shift(BIOREACTOR, "bioreactor_top"),
        BIOREACTOR_TANK_INNER   = shift(BIOREACTOR, "bioreactor_inner"),
        BIOREACTOR_BASE         = shift(BIOREACTOR, "bioreactor_base"),
        BIOREACTOR_BASE_TOP     = shift(BIOREACTOR, "bioreactor_base_top"),
        BIOREACTOR_BASE_BOTTOM  = shift(BIOREACTOR, "bioreactor_base_bottom"),
        SOLAR_POWER_TOWER       = shift(TOWER,  "solar_power_tower"),
        SOLAR_POWER_TOWER_TOP   = shift(TOWER,  "solar_power_tower_top"),
        SOLAR_POWER_TOWER_INNER = shift(TOWER,  "solar_power_tower_inner"),
        TURBINE_CASING          = shiftOmni(CASING, "industrial_iron_casing"),
        TURBINE_CASING_GLASS    = shiftOmni(CASING, "industrial_iron_glass_casing"),
        FUSION_REACTOR_CASING       = shiftOmni(REACTOR_CASING, "fusion_reactor_casing"),
        FUSION_REACTOR_CASING_GLASS = shiftOmni(REACTOR_CASING, "fusion_reactor_casing_glass");

    private static CTSpriteShiftEntry shift(String folder, String name) {
        return CTSpriteShifter.getCT(
            AllCTTypes.RECTANGLE,
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name),
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name + "_connected")
        );
    }

    private static CTSpriteShiftEntry shiftOmni(String folder, String name) {
        return CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name),
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name + "_connected")
        );
    }

    public static void init() {}
}
