package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;

public class ModSpriteShifts {

    private static final String VAT = "block/fermentation_vat/";

    public static final CTSpriteShiftEntry
        FERMENTATION_VAT       = getCT("fermentation_vat"),
        FERMENTATION_VAT_TOP   = getCT("fermentation_vat_top"),
        FERMENTATION_VAT_INNER = getCT("fermentation_vat_inner");

    private static CTSpriteShiftEntry getCT(String name) {
        return CTSpriteShifter.getCT(
            AllCTTypes.RECTANGLE,
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, VAT + name),
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, VAT + name + "_connected")
        );
    }

    public static void init() {}
}
