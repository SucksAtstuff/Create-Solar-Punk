package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity;
import org.jetbrains.annotations.Nullable;

public class TurbineRotorBlock extends KineticBlock implements IBE<TurbineRotorBlockEntity>, IWrenchable {

    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    public TurbineRotorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TurbineRotorBlockEntity be)
                be.invalidateStructure();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<TurbineRotorBlockEntity> getBlockEntityClass() {
        return TurbineRotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TurbineRotorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.TURBINE_ROTOR.get();
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ModBlockEntities.TURBINE_ROTOR.get())
            return (BlockEntityTicker<T>) (BlockEntityTicker<TurbineRotorBlockEntity>) (l, p, s, be) -> be.tick();
        return null;
    }
}
