package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

// Draws a spinning half-shaft nub out of the bottom of the block, for machines that
// take or give rotation from below (Andesite Solar Panel, Biofilter, Biofuel Engine,
// Biomass Gasifier; the Electric Motor overrides shaftSide()). Deliberately has no Flywheel visualizer: Create's
// SingleAxisRotatingVisual::shaft renders the full-length shaft, which pokes out of
// both ends of the block.
public class HalfShaftRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {

    public HalfShaftRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        standardKineticRotationTransform(
            CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), shaftSide(be.getBlockState())), be, light
        ).renderInto(ms, vb);
    }

    // The face the nub sticks out of.
    protected Direction shaftSide(BlockState state) {
        return Direction.DOWN;
    }
}
