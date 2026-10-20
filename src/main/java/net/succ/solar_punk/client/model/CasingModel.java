package net.succ.solar_punk.client.model;

import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

// Connected-texture model for multiblock casings (Turbine Casing, Fusion Reactor Casing
// and their glass variants). connectsTo decides which neighbours count as the same
// casing, so the solid and glass variants of one casing join up with each other.
public class CasingModel extends CTModel {

    public CasingModel(BakedModel originalModel, CTSpriteShiftEntry shift, Predicate<Block> connectsTo) {
        super(originalModel, new SimpleCTBehaviour(shift) {
            @Override
            public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                                      BlockPos pos, BlockPos otherPos, Direction face) {
                return connectsTo.test(other.getBlock());
            }
        });
    }
}
