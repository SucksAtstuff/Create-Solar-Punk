package net.succ.create_solar_powered.block.entity.custom;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SolarHeaterBlockEntity extends BlockEntity {

    public SolarHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.getGameTime() % 20 != 0) return;

        for (Direction dir : new Direction[]{Direction.UP, Direction.DOWN}) {
            if (!(level.getBlockEntity(worldPosition.relative(dir)) instanceof FluidTankBlockEntity tank)) continue;
            FluidTankBlockEntity controller = tank.getControllerBE();
            if (controller != null && controller.boiler != null)
                controller.boiler.needsHeatLevelUpdate = true;
        }
    }
}