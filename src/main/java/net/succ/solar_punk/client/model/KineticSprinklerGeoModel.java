package net.succ.solar_punk.client.model;

import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class KineticSprinklerGeoModel extends DefaultedBlockGeoModel<KineticSprinklerBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/kinetic_sprinkler/sprinkler_head.png");

    public KineticSprinklerGeoModel() {
        super(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "kinetic_sprinkler"));
    }

    @Override
    public ResourceLocation getTextureResource(KineticSprinklerBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(KineticSprinklerBlockEntity animatable) {
        return null;
    }
}
