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
import net.succ.solar_punk.block.custom.GeyserCapBlock;
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
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(GeyserCapBlock.FACING);
        Direction[] shaftDirs = (facing.getAxis() == Direction.Axis.Z)
                ? new Direction[]{ Direction.EAST, Direction.WEST }
                : new Direction[]{ Direction.NORTH, Direction.SOUTH };
        var vb = buffers.getBuffer(RenderType.cutoutMipped());
        for (Direction d : shaftDirs) {
            KineticBlockEntityRenderer.standardKineticRotationTransform(
                CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, d), be, light
            ).renderInto(ms, vb);
        }
    }
}