package net.succ.solar_punk.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;

public class FermentationVatRenderer extends TankGaugeRenderer<FermentationVatBlockEntity> {

    public static final PartialModel GAUGE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/fermentation_vat/gauge"));
    public static final PartialModel GAUGE_DIAL = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/fermentation_vat/gauge_dial"));

    public FermentationVatRenderer(BlockEntityRendererProvider.Context context) {
        super(GAUGE, GAUGE_DIAL);
    }

    @Override
    protected int minWidth() {
        return Config.fermentationVatMinWidth;
    }

    @Override
    protected FluidTank waterTank(FermentationVatBlockEntity be) {
        return be.waterTank;
    }
}
