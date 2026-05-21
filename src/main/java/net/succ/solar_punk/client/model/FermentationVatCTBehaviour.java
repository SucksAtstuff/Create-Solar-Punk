package net.succ.solar_punk.client.model;

import com.simibubi.create.content.fluids.tank.FluidTankCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FermentationVatCTBehaviour extends FluidTankCTBehaviour {

    public FermentationVatCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift,
                                      CTSpriteShiftEntry innerShift) {
        super(layerShift, topShift, innerShift);
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                               BlockPos pos, BlockPos otherPos, Direction face) {
        return state.getBlock() == other.getBlock();
    }
}
