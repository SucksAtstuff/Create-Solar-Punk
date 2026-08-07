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

        Direction.Axis axis = state.getValue(TurbineRotorBlock.AXIS);
        // "Positive" direction of the growth axis - matches TurbineRotorBlockEntity's
        // own growthPositive(), i.e. which way layer index (dy) increases in the real
        // structure.
        Direction growthPositive = switch (axis) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
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

                // The blade model is authored thin along (local) Y, extending along
                // (local) X, and everything below is built and proven around that
                // (spin around Y, layers stack along Y). Rather than hand-composing
                // separate X/Y rotations to reorient it for other growth axes (fragile -
                // a fluent PoseStack-style chain applies rotations in the reverse of
                // their call order, which is easy to get backwards), wrap the whole
                // thing in a single rotateTo mapping native "up" onto the real growth
                // axis. rotateTo pivots on the CURRENT origin, which at this point in
                // the chain is still the block's corner, not its center - rotating the
                // already-offset blade position around the wrong pivot is what swung it
                // a whole block off, so the reorientation is sandwiched between a
                // translate to the block center and back. That's an identity for
                // axis=Y (unchanged behaviour) and, for other axes, guarantees the
                // per-layer spacing lands exactly on the growth axis with no leftover
                // offset.
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