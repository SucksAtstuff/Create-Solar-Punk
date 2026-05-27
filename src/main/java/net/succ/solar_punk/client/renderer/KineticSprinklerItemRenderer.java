package net.succ.solar_punk.client.renderer;

import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import net.succ.solar_punk.client.model.KineticSprinklerItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class KineticSprinklerItemRenderer extends GeoItemRenderer<KineticSprinklerItem> {

    public KineticSprinklerItemRenderer() {
        super(new KineticSprinklerItemGeoModel());
    }
}