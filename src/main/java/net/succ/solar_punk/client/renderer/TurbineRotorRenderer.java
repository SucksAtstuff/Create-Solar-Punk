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
        if (!be.isMaster || !be.structureValid || be.turbineHeight < 2) return;
        if (!state.getValue(TurbineRotorBlock.ACTIVE)) return;

        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        Direction.Axis axis = state.getValue(TurbineRotorBlock.AXIS);
        Direction growthPositive = TurbineRotorBlockEntity.growthPositive(axis);
        float angle = getAngleForBe(be, be.getBlockPos(), axis);
        float angleDeg = (float) Math.toDegrees(angle);
        int bladeLayers = be.turbineHeight - 1;

        BlockState andesiteState = ModBlocks.ANDESITE_TURBINE_BLADE.get().defaultBlockState();
        BlockState brassState = ModBlocks.BRASS_TURBINE_BLADE.get().defaultBlockState();

        for (int dy = 0; dy < bladeLayers; dy++) {
            int mask     = (be.layerBladeMask.length > dy)     ? be.layerBladeMask[dy]     : 0xF;
            int typeMask = (be.layerBladeTypeMask.length > dy) ? be.layerBladeTypeMask[dy] : 0;
            for (int arm = 0; arm < 4; arm++) {
                if ((mask & (1 << arm)) == 0) continue;
                float totalAngleDeg = angleDeg + arm * 90f;
                BlockState bladeState = (typeMask & (1 << arm)) != 0 ? brassState : andesiteState;

                // The blade model is thin along Y and extends along X, and the spin and
                // layer stacking below are built for a vertical turbine. For other axes
                // the whole thing is wrapped in one rotateTo mapping UP onto the growth
                // axis, pivoted on the block center (rotateTo pivots on the current
                // origin, which is still the block corner at that point in the chain).
                CachedBuffers.block(KINETIC_BLOCK, bladeState)
                        .translate(0.5f, 0.5f, 0.5f)
                        .rotateTo(Direction.UP, growthPositive)
                        .translate(-0.5f, -0.5f, -0.5f)
                        .translate(0.5f, 0f, 0.5f)
                        .rotateYDegrees(totalAngleDeg)
                        .translate(0.5f, (float) dy, -0.5f)
                        .light(light)
                        .renderInto(ms, vb);
            }
        }
    }
}