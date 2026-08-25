package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.succ.solar_punk.block.entity.custom.BiofuelEngineBlockEntity;

// Same "no Flywheel visualizer, just a CPU-side half-shaft nub" pattern as
// AndesiteSolarPanelRenderer/BiomassGasifierRenderer - the Engine used to rely on a
// static Axis element baked into its own block model instead (no animation at all,
// same shape the Gasifier's model briefly had before it started z-fighting with a
// dynamic shaft - see biomass_gasifier.json's git history). Giving it this renderer
// instead means the shaft actually spins with the Engine's kinetic speed.
public class BiofuelEngineRenderer extends KineticBlockEntityRenderer<BiofuelEngineBlockEntity> {

    public BiofuelEngineRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void renderSafe(BiofuelEngineBlockEntity be, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay) {
        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        standardKineticRotationTransform(
            CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), Direction.DOWN), be, light
        ).renderInto(ms, vb);
    }
}
