package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.SolarShinglesBlockEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/**
 * A stepped roof piece that bends into corners like stairs. A roof rises one block every two:
 * HALF bottom is the half-height piece (steps at 4 and 8), HALF top the full-height one (12 and
 * 16), and rows alternate between them. FACING points uphill. Only shingles of the same HALF
 * join into corners, using vanilla's stair rules, so vanilla stairs never bend into them.
 * Each shingle generates FE; see {@link SolarShinglesBlockEntity} for how a roof shares it.
 */
public class SolarShinglesBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock {
    public static final MapCodec<SolarShinglesBlock> CODEC = simpleCodec(SolarShinglesBlock::new);
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public SolarShinglesBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, false));
        for (BlockState state : stateDefinition.getPossibleStates()) shapes.put(state, buildShape(state));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    // The whole block up to the low step, plus the high step on the quadrants that are raised.
    private static VoxelShape buildShape(BlockState state) {
        boolean top = state.getValue(HALF) == Half.TOP;
        double low = top ? 12 : 4;
        double high = top ? 16 : 8;
        VoxelShape shape = Block.box(0, 0, 0, 16, low, 16);
        for (Direction xSide : new Direction[]{Direction.WEST, Direction.EAST}) {
            for (Direction zSide : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                if (!isRaised(state, xSide, zSide)) continue;
                double x = xSide == Direction.WEST ? 0 : 8;
                double z = zSide == Direction.NORTH ? 0 : 8;
                shape = Shapes.or(shape, Block.box(x, low, z, x + 8, high, z + 8));
            }
        }
        return shape.optimize();
    }

    // Whether the quadrant on the given sides has the high step, matching the corner models.
    private static boolean isRaised(BlockState state, Direction xSide, Direction zSide) {
        Direction facing = state.getValue(FACING);
        boolean uphill = xSide == facing || zSide == facing;
        return switch (state.getValue(SHAPE)) {
            case STRAIGHT -> uphill;
            case OUTER_RIGHT -> uphill && on(facing.getClockWise(), xSide, zSide);
            case OUTER_LEFT -> uphill && on(facing.getCounterClockWise(), xSide, zSide);
            case INNER_RIGHT -> !(on(facing.getOpposite(), xSide, zSide) && on(facing.getCounterClockWise(), xSide, zSide));
            case INNER_LEFT -> !(on(facing.getOpposite(), xSide, zSide) && on(facing.getClockWise(), xSide, zSide));
        };
    }

    private static boolean on(Direction side, Direction xSide, Direction zSide) {
        return side == xSide || side == zSide;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();
        boolean water = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(HALF, placementHalf(context, facing))
                .setValue(WATERLOGGED, water);
        return state.setValue(SHAPE, shapeFor(state, context.getLevel(), pos));
    }

    // Continues a roof: the row above a bottom piece is a top piece and the other way round.
    // With no shingle to follow, picks by where the player clicked, like stairs.
    private Half placementHalf(BlockPlaceContext context, Direction facing) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos downhill = pos.relative(facing.getOpposite());
        BlockPos uphill = pos.relative(facing);

        if (isShingle(level.getBlockState(downhill), facing, Half.BOTTOM)) return Half.TOP;
        if (isShingle(level.getBlockState(downhill.below()), facing, Half.TOP)) return Half.BOTTOM;
        if (isShingle(level.getBlockState(uphill), facing, Half.TOP)) return Half.BOTTOM;
        if (isShingle(level.getBlockState(uphill.above()), facing, Half.BOTTOM)) return Half.TOP;

        Direction face = context.getClickedFace();
        boolean upper = face == Direction.DOWN
                || (face != Direction.UP && context.getClickLocation().y - pos.getY() > 0.5);
        return upper ? Half.TOP : Half.BOTTOM;
    }

    private boolean isShingle(BlockState state, Direction facing, Half half) {
        return state.is(this) && state.getValue(FACING) == facing && state.getValue(HALF) == half;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return direction.getAxis().isHorizontal()
                ? state.setValue(SHAPE, shapeFor(state, level, pos))
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // Vanilla's stair corner rules (StairBlock.getStairsShape), limited to shingles of the same HALF.
    private StairsShape shapeFor(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);

        BlockState front = level.getBlockState(pos.relative(facing));
        if (sameRow(state, front)) {
            Direction frontFacing = front.getValue(FACING);
            if (frontFacing.getAxis() != facing.getAxis() && canTakeShape(state, level, pos, frontFacing.getOpposite())) {
                return frontFacing == facing.getCounterClockWise() ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }

        BlockState back = level.getBlockState(pos.relative(facing.getOpposite()));
        if (sameRow(state, back)) {
            Direction backFacing = back.getValue(FACING);
            if (backFacing.getAxis() != facing.getAxis() && canTakeShape(state, level, pos, backFacing)) {
                return backFacing == facing.getCounterClockWise() ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private boolean sameRow(BlockState state, BlockState other) {
        return other.is(this) && other.getValue(HALF) == state.getValue(HALF);
    }

    private boolean canTakeShape(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        return !sameRow(state, neighbor) || neighbor.getValue(FACING) != state.getValue(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarShinglesBlockEntity(ModBlockEntities.SOLAR_SHINGLES.get(), pos, state);
    }

    // Server only: generation and sharing are server logic, and there's no sound.
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SOLAR_SHINGLES.get()) return null;
        return (l, p, s, be) -> ((SolarShinglesBlockEntity) be).tick();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // Same as StairBlock.mirror: mirroring swaps left and right corners.
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = state.getValue(FACING);
        StairsShape shape = state.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                if (facing.getAxis() == Direction.Axis.Z) {
                    return switch (shape) {
                        case INNER_LEFT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case INNER_RIGHT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case OUTER_LEFT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        default -> state.rotate(Rotation.CLOCKWISE_180);
                    };
                }
                break;
            case FRONT_BACK:
                if (facing.getAxis() == Direction.Axis.X) {
                    return switch (shape) {
                        case INNER_LEFT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case INNER_RIGHT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case OUTER_LEFT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT -> state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        case STRAIGHT -> state.rotate(Rotation.CLOCKWISE_180);
                    };
                }
                break;
            default:
                break;
        }
        return super.mirror(state, mirror);
    }
}
