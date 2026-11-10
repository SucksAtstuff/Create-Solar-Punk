package net.succ.solar_punk.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;
import net.succ.solar_punk.block.entity.custom.ElectricMotorBlockEntity;

// The spinning half-shaft out of the motor's front (FACING).
public class ElectricMotorRenderer extends HalfShaftRenderer<ElectricMotorBlockEntity> {

    public ElectricMotorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    protected Direction shaftSide(BlockState state) {
        return state.getValue(ElectricMotorBlock.FACING);
    }
}
