package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.entity.custom.AndesiteSolarPanelBlockEntity;

public class AndesiteSolarPanelRenderer extends KineticBlockEntityRenderer<AndesiteSolarPanelBlockEntity> {

    public AndesiteSolarPanelRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void renderSafe(AndesiteSolarPanelBlockEntity be, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay) {
        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        standardKineticRotationTransform(
            CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), Direction.DOWN), be, light
        ).renderInto(ms, vb);
    }
}
