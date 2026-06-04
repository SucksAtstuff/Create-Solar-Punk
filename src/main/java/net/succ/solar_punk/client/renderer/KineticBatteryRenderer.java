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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.succ.solar_punk.block.entity.custom.KineticBatteryBlockEntity;

public class KineticBatteryRenderer extends KineticBlockEntityRenderer<KineticBatteryBlockEntity> {

    public KineticBatteryRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected void renderSafe(KineticBatteryBlockEntity be, float partialTicks, PoseStack ms,
                               MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        var vb = buffer.getBuffer(RenderType.cutoutMipped());
        for (Direction.AxisDirection axisDir : Direction.AxisDirection.values()) {
            Direction dir = Direction.get(axisDir, axis);
            standardKineticRotationTransform(
                CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, dir), be, light
            ).renderInto(ms, vb);
        }
    }
}
