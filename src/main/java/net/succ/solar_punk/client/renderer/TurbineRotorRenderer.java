package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;

public class TurbineRotorRenderer extends KineticBlockEntityRenderer<TurbineRotorBlockEntity> {

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void renderSafe(TurbineRotorBlockEntity be, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        for (Direction d : new Direction[]{ Direction.UP, Direction.DOWN }) {
            standardKineticRotationTransform(
                CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, d), be, light
            ).renderInto(ms, vb);
        }
    }
}
