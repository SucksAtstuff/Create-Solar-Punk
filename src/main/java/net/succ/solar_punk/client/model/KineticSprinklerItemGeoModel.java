package net.succ.solar_punk.client.model;

import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.KineticSprinklerItem;
import software.bernie.geckolib.model.GeoModel;

public class KineticSprinklerItemGeoModel extends GeoModel<KineticSprinklerItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geo/block/kinetic_sprinkler.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/kinetic_sprinkler/sprinkler_head.png");

    @Override
    public ResourceLocation getModelResource(KineticSprinklerItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KineticSprinklerItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(KineticSprinklerItem animatable) {
        return null;
    }
}