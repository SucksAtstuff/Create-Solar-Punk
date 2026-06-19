package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.TurbineRotorBlock;
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

        if (!be.isMaster || !be.structureValid || be.turbineHeight < 2) return;
        if (!state.getValue(TurbineRotorBlock.ACTIVE)) return;

        float angle = getAngleForBe(be, be.getBlockPos(), Direction.Axis.Y);
        float angleDeg = (float) Math.toDegrees(angle);
        int bladeLayers = be.turbineHeight - 1;

        BlockState bladeState = ModBlocks.ANDESITE_TURBINE_BLADE.get().defaultBlockState();

        for (int dy = 0; dy < bladeLayers; dy++) {
            int mask = (be.layerBladeMask.length > dy) ? be.layerBladeMask[dy] : 0xF;
            for (int arm = 0; arm < 4; arm++) {
                if ((mask & (1 << arm)) == 0) continue;
                float totalAngleDeg = angleDeg + arm * 90f;

                CachedBuffers.block(KINETIC_BLOCK, bladeState)
                        .translate(0.5f, 0f, 0.5f)
                        .rotateYDegrees(totalAngleDeg)
                        .translate(0.5f, (float) dy, -0.5f)
                        .light(light)
                        .renderInto(ms, vb);
            }
        }
    }
}