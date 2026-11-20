package net.succ.solar_punk.client.model;

import net.minecraft.resources.ResourceLocation;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.GeyserCapBlock;
import net.succ.solar_punk.block.entity.custom.GeyserCapBlockEntity;
import net.succ.solar_punk.util.SeasonalEvent;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class GeyserCapGeoModel extends DefaultedBlockGeoModel<GeyserCapBlockEntity> {

    private static final ResourceLocation TEXTURE = texture("geyser_cap/geyser_cap");
    private static final ResourceLocation TEXTURE_LIT = texture("geyser_cap/geyser_cap_lit");

    // A Santa hat on the lid in December (the item's is in SeasonalModels).
    private static final ResourceLocation CHRISTMAS_MODEL =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geo/block/events/christmas/geyser_cap.geo.json");
    private static final ResourceLocation CHRISTMAS_TEXTURE = texture("events/christmas/geyser_cap");
    private static final ResourceLocation CHRISTMAS_TEXTURE_LIT = texture("events/christmas/geyser_cap_lit");

    // Read when the renderer is built, so it changes on the next resource reload, like the block models.
    private final boolean christmas = SeasonalEvent.CHRISTMAS.isActive();

    public GeyserCapGeoModel() {
        super(ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "geyser_cap"));
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/" + path + ".png");
    }

    @Override
    public ResourceLocation getModelResource(GeyserCapBlockEntity animatable) {
        return christmas ? CHRISTMAS_MODEL : super.getModelResource(animatable);
    }

    @Override
    public ResourceLocation getTextureResource(GeyserCapBlockEntity animatable) {
        boolean lit = animatable.getBlockState().getValue(GeyserCapBlock.LIT);
        if (christmas) return lit ? CHRISTMAS_TEXTURE_LIT : CHRISTMAS_TEXTURE;
        return lit ? TEXTURE_LIT : TEXTURE;
    }
}
