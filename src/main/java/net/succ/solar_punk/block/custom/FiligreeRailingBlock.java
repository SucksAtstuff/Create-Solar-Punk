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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.item.FiligreeRailingItem;

import java.util.List;
import java.util.function.Predicate;

// A railing that connects to its neighbours like iron bars (IronBarsBlock handles the four
// connections, neighbour updates and waterlogging), and also to the ends of a
// FiligreeRailingSlopeBlock in line with it, including a full slope's low end reaching down
// from the block diagonally above. The centre post shows everywhere except in the
// middle of a straight run (connected on exactly two opposite sides), which uses its own
// straight model instead (see ModBlockStateProvider.filigreeRailingBlock).
//
// Holding a railing item set to flat and looking at a railing shows Create's placement arrow
// for extending the run in the direction looked at.
public class FiligreeRailingBlock extends IronBarsBlock implements IWrenchable {
    public static final MapCodec<FiligreeRailingBlock> CODEC = simpleCodec(FiligreeRailingBlock::new);
    public static final BooleanProperty POST = BooleanProperty.create("post");

    private static final VoxelShape POST_SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    private static final int RUN_HELPER = PlacementHelpers.register(new RunHelper());

    public FiligreeRailingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POST, true));
    }

    @Override
    public MapCodec<FiligreeRailingBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction.getAxis().isHorizontal()
                && (joinsSlopeEnd(direction, neighborState) || joinsSlopeBelowEnd(level, pos, direction)))
            updated = updated.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
        return withPost(updated);
    }

    // Connections worked out from the neighbours, for placements that start from the default
    // state (Create's placement helpers).
    static BlockState connectedState(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = level.getBlockState(neighborPos);
            boolean connects = ((IronBarsBlock) state.getBlock())
                    .attachsTo(neighbor, neighbor.isFaceSturdy(level, neighborPos, side.getOpposite()))
                    || joinsSlopeEnd(side, neighbor) || joinsSlopeBelowEnd(level, pos, side);
            state = state.setValue(PROPERTY_BY_DIRECTION.get(side), connects);
        }
        return withPost(state);
    }

    // Whether the slope on `side` has an end a flat railing joins, on this level, facing this way.
    private static boolean joinsSlopeEnd(Direction side, BlockState neighbor) {
        return neighbor.getBlock() instanceof FiligreeRailingSlopeBlock
                && FiligreeRailingSlopeBlock.flatEnds(neighbor).stream()
                        .anyMatch(end -> end.dy() == 0 && end.side() == side.getOpposite());
    }

    // Whether a full slope one block up on `side` reaches down to this railing with its low end.
    private static boolean joinsSlopeBelowEnd(BlockGetter level, BlockPos pos, Direction side) {
        BlockState above = level.getBlockState(pos.relative(side).above());
        return above.getBlock() instanceof FiligreeRailingSlopeBlock
                && FiligreeRailingSlopeBlock.flatEnds(above).stream()
                        .anyMatch(end -> end.dy() == -1 && end.side() == side.getOpposite());
    }

    private static BlockState withPost(BlockState state) {
        boolean north = state.getValue(NORTH), east = state.getValue(EAST),
                south = state.getValue(SOUTH), west = state.getValue(WEST);
        boolean straight = (north && south && !east && !west) || (east && west && !north && !south);
        return state.setValue(POST, !straight);
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

    // The bars' thin outline, plus the wider post when it shows.
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getShape(state, level, pos, context);
        return state.getValue(POST) ? Shapes.or(shape, POST_SHAPE) : shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getCollisionShape(state, level, pos, context);
        return state.getValue(POST) ? Shapes.or(shape, POST_SHAPE) : shape;
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
