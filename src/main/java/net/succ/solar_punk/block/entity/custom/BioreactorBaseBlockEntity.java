package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds nothing. Create's pipes and pumps only connect to blocks with a block entity, so the
 * Bioreactor Base needs one for its fluid capability to be seen. Everything goes to the tank
 * above ({@link BioreactorBlockEntity#above}).
 */
public class BioreactorBaseBlockEntity extends BlockEntity {

    public BioreactorBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
