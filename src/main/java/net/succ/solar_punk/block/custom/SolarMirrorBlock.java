package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class SolarMirrorBlock extends Block implements IWrenchable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public SolarMirrorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    private static final Direction[] FACING_CYCLE = {
        Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN
    };

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction current = state.getValue(FACING);
        Direction next = FACING_CYCLE[0];
        for (int i = 0; i < FACING_CYCLE.length; i++) {
            if (FACING_CYCLE[i] == current) {
                next = FACING_CYCLE[(i + 1) % FACING_CYCLE.length];
                break;
            }
        }
        level.setBlock(pos, state.setValue(FACING, next), 3);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }
}
