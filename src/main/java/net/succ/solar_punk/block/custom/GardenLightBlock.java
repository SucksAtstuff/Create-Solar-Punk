package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.util.SolarHelper;

// A small solar garden light. It charges when it sees the sky during the day and glows
// through the following night, so a light kept in the shade (or placed after dark) stays off
// until it has had a day in the sun. Checks on a scheduled tick, with a random delay so a
// row of lights doesn't all switch on the same tick.
public class GardenLightBlock extends Block {
    public static final MapCodec<GardenLightBlock> CODEC = simpleCodec(GardenLightBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");

    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 8, 11);

    public GardenLightBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(CHARGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, CHARGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) scheduleCheck(level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState next;
        if (SolarHelper.isDaytime(level, pos)) {
            // Still lit means the night just ended, which used up its charge.
            boolean charged = state.getValue(CHARGED) && !state.getValue(LIT);
            next = state.setValue(LIT, false).setValue(CHARGED, charged || SolarHelper.hasSkyAccess(level, pos));
        } else {
            next = state.setValue(LIT, state.getValue(CHARGED));
        }
        if (next != state) level.setBlock(pos, next, Block.UPDATE_ALL);
        scheduleCheck(level, pos);
    }

    private void scheduleCheck(Level level, BlockPos pos) {
        level.scheduleTick(pos, this, 40 + level.random.nextInt(40));
    }
}
