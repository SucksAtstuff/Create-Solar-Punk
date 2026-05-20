package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;

public class ModSpriteShifts {

    private static final String VAT   = "block/fermentation_vat/";
    private static final String TOWER = "block/solar_power_tower/";

    public static final CTSpriteShiftEntry
        FERMENTATION_VAT       = shift(VAT,   "fermentation_vat"),
        FERMENTATION_VAT_TOP   = shift(VAT,   "fermentation_vat_top"),
        FERMENTATION_VAT_INNER = shift(VAT,   "fermentation_vat_inner"),
        SOLAR_POWER_TOWER       = shift(TOWER, "solar_power_tower"),
        SOLAR_POWER_TOWER_TOP   = shift(TOWER, "solar_power_tower_top"),
        SOLAR_POWER_TOWER_INNER = shift(TOWER, "solar_power_tower_inner");

    private static CTSpriteShiftEntry shift(String folder, String name) {
        return CTSpriteShifter.getCT(
            AllCTTypes.RECTANGLE,
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name),
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, folder + name + "_connected")
        );
    }

    public static void init() {}
}
