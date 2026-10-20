package net.succ.solar_punk.client.renderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.MultiBlockFluidBE;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;

public class SolarPowerTowerRenderer extends TankGaugeRenderer<SolarPowerTowerBlockEntity> {

    public static final PartialModel GAUGE = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_power_tower/gauge"));
    public static final PartialModel GAUGE_DIAL = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/solar_power_tower/gauge_dial"));

    public SolarPowerTowerRenderer(BlockEntityRendererProvider.Context context) {
        super(GAUGE, GAUGE_DIAL);
    }

    // Only shown on a full-width (3x3) tower.
    @Override
    protected int minWidth() {
        return MultiBlockFluidBE.MAX_WIDTH;
    }

    @Override
    protected FluidTank waterTank(SolarPowerTowerBlockEntity be) {
        return be.waterTank;
    }
}
