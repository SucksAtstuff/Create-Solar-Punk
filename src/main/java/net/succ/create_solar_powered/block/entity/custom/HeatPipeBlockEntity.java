package net.succ.create_solar_powered.block.entity.custom;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.create_solar_powered.block.custom.HeatPipeBlock;

public class HeatPipeBlockEntity extends BlockEntity {

    public HeatPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.getGameTime() % 20 != 0) return;
        // Scan upward through stacked heat pipes to reach the fluid tank above
        BlockPos check = worldPosition.above();
        while (level.getBlockState(check).getBlock() instanceof HeatPipeBlock)
            check = check.above();
        if (!(level.getBlockEntity(check) instanceof FluidTankBlockEntity tank)) return;
        FluidTankBlockEntity controller = tank.getControllerBE();
        if (controller != null && controller.boiler != null)
            controller.boiler.needsHeatLevelUpdate = true;
    }
}