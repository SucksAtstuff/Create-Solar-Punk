package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.block.entity.custom.MultiBlockFluidBE;

// Draws a water gauge on all four sides of a formed multiblock tank, with a needle
// that goes from horizontal (empty) to straight up (full).
public abstract class TankGaugeRenderer<T extends MultiBlockFluidBE<T>> extends SafeBlockEntityRenderer<T> {

    // Needle pivot: bottom-left corner of the gauge face, in block units.
    private static final float PIVOT_X = 16.1f / 16f, PIVOT_Y = 5.5f / 16f, PIVOT_Z = 6.05f / 16f;

    private final PartialModel gauge;
    private final PartialModel dial;

    protected TankGaugeRenderer(PartialModel gauge, PartialModel dial) {
        this.gauge = gauge;
        this.dial = dial;
    }

    // The gauge only shows once the multiblock is wide enough to work.
    protected abstract int minWidth();

    protected abstract FluidTank waterTank(T be);

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        if (!be.isController() || be.getWidth() < minWidth()) return;

        BlockState blockState = be.getBlockState();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());

        FluidTank tank = waterTank(be);
        float fill = tank.getCapacity() > 0 ? tank.getFluidAmount() / (float) tank.getCapacity() : 0f;
        float needleAngle = Mth.lerp(fill, 0f, -90f);
        float edgeOffset = be.getWidth() / 2f - 6 / 16f;

        ms.pushPose();
        TransformStack.of(ms).translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

        for (Direction d : Iterate.horizontalDirections) {
            float yRot = -d.toYRot() - 90;

            CachedBuffers.partial(gauge, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(edgeOffset, 0, 0)
                    .light(light)
                    .renderInto(ms, vb);

            // Transforms apply to vertices in reverse call order: the last three move the
            // pivot to the origin, rotate the needle and move it back, then the first three
            // place it on the gauge exactly like the body above.
            CachedBuffers.partial(dial, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(edgeOffset, 0, 0)
                    .translate(PIVOT_X, PIVOT_Y, PIVOT_Z)
                    .rotateXDegrees(needleAngle)
                    .translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z)
                    .light(light)
                    .renderInto(ms, vb);
        }

        ms.popPose();
    }
}
