package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.BiofilterBlockEntity;

public class BiofilterBlock extends KineticBlock implements IBE<BiofilterBlockEntity> {

    public BiofilterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public Class<BiofilterBlockEntity> getBlockEntityClass() {
        return BiofilterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BiofilterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.BIOFILTER.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.BIOFILTER.get())
            return (BlockEntityTicker<T>) (BlockEntityTicker<BiofilterBlockEntity>) (l, p, s, be) -> be.tick();
        return null;
    }
}
