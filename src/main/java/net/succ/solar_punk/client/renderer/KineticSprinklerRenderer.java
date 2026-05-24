package net.succ.solar_punk.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity;
import net.succ.solar_punk.client.model.KineticSprinklerGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class KineticSprinklerRenderer extends GeoBlockRenderer<KineticSprinklerBlockEntity> {
    public KineticSprinklerRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new KineticSprinklerGeoModel());
    }
}
