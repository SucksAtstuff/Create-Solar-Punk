package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.entity.custom.GeyserCapBlockEntity;
import net.succ.solar_punk.client.model.GeyserCapGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GeyserCapRenderer extends GeoBlockRenderer<GeyserCapBlockEntity> {
    public GeyserCapRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new GeyserCapGeoModel());
    }

    @Override
    public void render(GeyserCapBlockEntity be, float partialTick, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
        super.render(be, partialTick, ms, buffers, light, overlay);
        var vb = buffers.getBuffer(RenderType.cutoutMipped());
        for (Direction d : new Direction[]{ Direction.EAST, Direction.WEST }) {
            KineticBlockEntityRenderer.standardKineticRotationTransform(
                CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), d), be, light
            ).renderInto(ms, vb);
        }
    }
}