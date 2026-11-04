package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.item.FiligreeRailingItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

// A railing that connects to its neighbours like iron bars (IronBarsBlock handles the four
// connections, neighbour updates and waterlogging), and also to the ends of a
// FiligreeRailingSlopeBlock in line with it: beside it, diagonally below (a gentle slope's high
// end) or diagonally above (a full slope's low end). The centre post shows everywhere except in
// the middle of a straight run (connected on exactly two opposite sides), which uses its own
// straight model instead (see ModBlockStateProvider.filigreeRailingBlock). Where it meets a
// gentle slope's end in a straight line it shows the START or END transition piece instead
// (TRANSITION).
//
// Holding a railing item set to flat and looking at a railing shows Create's placement arrow
// for extending the run in the direction looked at.
public class FiligreeRailingBlock extends IronBarsBlock implements IWrenchable {
    public static final MapCodec<FiligreeRailingBlock> CODEC = simpleCodec(FiligreeRailingBlock::new);
    public static final BooleanProperty POST = BooleanProperty.create("post");
    public static final EnumProperty<RailingTransition> TRANSITION = EnumProperty.create("transition", RailingTransition.class);

    private static final VoxelShape POST_SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    private static final int RUN_HELPER = PlacementHelpers.register(new RunHelper());

    public FiligreeRailingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POST, true).setValue(TRANSITION, RailingTransition.NONE));
    }

    @Override
    public MapCodec<FiligreeRailingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POST, TRANSITION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction.getAxis().isHorizontal() && joinsSlope(level, pos, direction))
            updated = updated.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
        return withPost(withTransition(updated, level, pos));
    }

    // Connections worked out from the neighbours, for placements that start from the default
    // state (Create's placement helpers).
    static BlockState connectedState(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = level.getBlockState(neighborPos);
            boolean connects = ((IronBarsBlock) state.getBlock())
                    .attachsTo(neighbor, neighbor.isFaceSturdy(level, neighborPos, side.getOpposite()))
                    || joinsSlope(level, pos, side);
            state = state.setValue(PROPERTY_BY_DIRECTION.get(side), connects);
        }
        return withPost(withTransition(state, level, pos));
    }

    // Whether a slope on `side` (level with this railing, one block down or one block up) has an
    // end that this railing joins.
    private static boolean joinsSlope(BlockGetter level, BlockPos pos, Direction side) {
        for (int dy = -1; dy <= 1; dy++) {
            if (slopeEndAt(level, pos, side, dy) != null) return true;
        }
        return false;
    }

    // The slope on `side`, `dy` blocks up from this railing, if it has an end joining this railing.
    @Nullable
    private static BlockState slopeEndAt(BlockGetter level, BlockPos pos, Direction side, int dy) {
        BlockState slope = level.getBlockState(pos.relative(side).above(dy));
        if (!(slope.getBlock() instanceof FiligreeRailingSlopeBlock)) return null;
        // The slope's end is `-dy` blocks up from the slope, on the side facing back at us.
        boolean joins = FiligreeRailingSlopeBlock.flatEnds(slope).stream()
                .anyMatch(end -> end.side() == side.getOpposite() && end.dy() == -dy);
        return joins ? slope : null;
    }

    // START beside a gentle slope's low end, END diagonally above its high end, as long as the
    // railing runs straight into it (no connection off to the sides).
    private static BlockState withTransition(BlockState state, BlockGetter level, BlockPos pos) {
        RailingTransition transition = RailingTransition.NONE;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(side.getClockWise()))
                    || state.getValue(PROPERTY_BY_DIRECTION.get(side.getCounterClockWise()))) continue;
            BlockState low = slopeEndAt(level, pos, side, 0);
            if (low != null && low.getValue(FiligreeRailingSlopeBlock.PART) == FiligreeRailingSlopeBlock.Part.LOWER) {
                transition = RailingTransition.of(RailingTransition.Kind.START, side);
                break;
            }
            BlockState high = slopeEndAt(level, pos, side, -1);
            if (high != null && high.getValue(FiligreeRailingSlopeBlock.PART) == FiligreeRailingSlopeBlock.Part.LOWER) {
                transition = RailingTransition.of(RailingTransition.Kind.END, side.getOpposite());
                break;
            }
        }
        return state.setValue(TRANSITION, transition);
    }

    private static BlockState withPost(BlockState state) {
        boolean north = state.getValue(NORTH), east = state.getValue(EAST),
                south = state.getValue(SOUTH), west = state.getValue(WEST);
        boolean straight = (north && south && !east && !west) || (east && west && !north && !south);
        return state.setValue(POST, !straight && state.getValue(TRANSITION) == RailingTransition.NONE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !player.mayBuild() || !(stack.getItem() instanceof BlockItem item))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        IPlacementHelper helper = PlacementHelpers.get(RUN_HELPER);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hit).placeInWorld(level, item, player, hand, hit);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // The bars' thin outline, plus the wider post when it shows. A transition piece has the
    // stepped outline of its slope half instead.
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TRANSITION) != RailingTransition.NONE)
            return FiligreeRailingSlopeBlock.transitionShape(state.getValue(TRANSITION));
        VoxelShape shape = super.getShape(state, level, pos, context);
        return state.getValue(POST) ? Shapes.or(shape, POST_SHAPE) : shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TRANSITION) != RailingTransition.NONE)
            return FiligreeRailingSlopeBlock.transitionShape(state.getValue(TRANSITION));
        VoxelShape shape = super.getCollisionShape(state, level, pos, context);
        return state.getValue(POST) ? Shapes.or(shape, POST_SHAPE) : shape;
    }

    // IronBarsBlock turns the four connections; the transition's climb direction turns with them.
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState rotated = super.rotate(state, rotation);
        RailingTransition t = state.getValue(TRANSITION);
        return t.climb == null ? rotated : rotated.setValue(TRANSITION, t.rotated(rotation.rotate(t.climb)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        RailingTransition t = state.getValue(TRANSITION);
        return t.climb == null ? mirrored : mirrored.setValue(TRANSITION, t.rotated(mirror.mirror(t.climb)));
    }

    // Holding a railing, looking at one: extend the run in the horizontal direction looked
    // at, past any railings already in that line.
    private static class RunHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.getItem() instanceof FiligreeRailingItem && FiligreeRailingItem.getShape(stack) == RailingShape.FLAT;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof FiligreeRailingBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<Direction> dirs = IPlacementHelper.orderedByDistance(pos, ray.getLocation(), d -> d.getAxis().isHorizontal());
            for (Direction dir : dirs) {
                BlockPos target = pos.relative(dir);
                for (int step = 0; step < 16 && level.getBlockState(target).getBlock() instanceof FiligreeRailingBlock; step++)
                    target = target.relative(dir);
                if (level.getBlockState(target).canBeReplaced()) {
                    BlockPos placeAt = target;
                    return PlacementOffset.success(placeAt, s -> connectedState(s, level, placeAt));
                }
            }
            return PlacementOffset.fail();
        }
    }
}
