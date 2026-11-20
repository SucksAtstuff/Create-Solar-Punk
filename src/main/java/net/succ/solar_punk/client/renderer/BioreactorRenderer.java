package net.succ.solar_punk.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.block.entity.custom.BioreactorBlockEntity;

// The Bioreactor's contents behind the glass: Algae Culture at the bottom, Water above it.
public class BioreactorRenderer extends SafeBlockEntityRenderer<BioreactorBlockEntity> {

    private static final float MIN_HEIGHT = 1 / 64f;

    public BioreactorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(BioreactorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (!be.isController()) return;
        float hull = BioreactorBlockEntity.HULL;
        float max = be.getWidth() - hull;
        float y = hull;
        y = layer(be.cultureTank.getFluid(), be.fillHeight(be.cultureTank.getFluidAmount()), y, hull, max, ms, buffer, light);
        layer(be.waterTank.getFluid(), be.fillHeight(be.waterTank.getFluidAmount()), y, hull, max, ms, buffer, light);
    }

    private static float layer(FluidStack fluid, float height, float y, float min, float max,
                               PoseStack ms, MultiBufferSource buffer, int light) {
        if (fluid.isEmpty() || height < MIN_HEIGHT) return y;
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, min, y, min, max, y + height, max,
                buffer, ms, light, false, true);
        return y + height;
    }

    @Override
    public boolean shouldRenderOffScreen(BioreactorBlockEntity be) {
        return be.isController();
    }

    @Override
    public AABB getRenderBoundingBox(BioreactorBlockEntity be) {
        return be.renderBox();
    }
}
