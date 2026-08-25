package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.FusionReactorCasingBlock;
import net.succ.solar_punk.block.custom.FusionReactorCasingGlassBlock;

public class FusionReactorCasingCTBehaviour extends SimpleCTBehaviour {

    public FusionReactorCasingCTBehaviour(CTSpriteShiftEntry shift) {
        super(shift);
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                               BlockPos pos, BlockPos otherPos, Direction face) {
        return other.getBlock() instanceof FusionReactorCasingBlock
            || other.getBlock() instanceof FusionReactorCasingGlassBlock;
    }
}
