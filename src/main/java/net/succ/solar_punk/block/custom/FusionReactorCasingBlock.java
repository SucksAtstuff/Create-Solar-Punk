package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;

// Containment shell for the Fusion Reactor. Once the shell is closed the Core is
// unreachable, so the block entity proxies pipes, hoppers, goggles and Display Links
// through to it (see FusionReactorCasingBlockEntity).
public class FusionReactorCasingBlock extends Block implements IWrenchable, EntityBlock {
    public FusionReactorCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionReactorCasingBlockEntity(ModBlockEntities.FUSION_REACTOR_CASING.get(), pos, state);
    }
}
