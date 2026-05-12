package net.succ.solar_punk.client.model;

import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.block.entity.custom.GeyserCapBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class GeyserCapGeoModel extends DefaultedBlockGeoModel<GeyserCapBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/geyser_cap.png");
    private static final ResourceLocation TEXTURE_LIT =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/geyser_cap_lit.png");

    public GeyserCapGeoModel() {
        super(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geyser_cap"));
    }

    @Override
    public ResourceLocation getTextureResource(GeyserCapBlockEntity animatable) {
        return animatable.getBlockState().getValue(GeyserCapBlock.LIT) ? TEXTURE_LIT : TEXTURE;
    }
}
