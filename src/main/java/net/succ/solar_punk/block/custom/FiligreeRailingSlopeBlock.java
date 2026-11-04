package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.item.FiligreeRailingItem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// A railing that climbs toward FACING. Three shapes (PART):
//  - LOWER and UPPER (the "up" and "down" models) are the pieces of a gentle climb, half a
//    block up per block, sitting a quarter block higher than a flat railing. UPPER goes one
//    block further and one block higher than LOWER, and the next LOWER one block further on
//    the same level, so a ramp repeats lower, upper, lower. A gentle run starts and ends on a
//    LOWER: the flat railings at its ends show the START and END transition pieces
//    (RailingTransition), which take up the quarter block. So a run is
//    flat, START, LOWER, (UPPER, LOWER)..., END, flat.
//  - FULL climbs a whole block in one, like a staircase. Its low end reaches down into the
//    block below. Only the steep slope block (FiligreeRailingSteepSlopeBlock) uses it.
// Flat railings join the ends listed by flatEnds(): beside LOWER's low end (START), in front
// of and one block above LOWER's high end (END), and beside FULL's high end or diagonally
// below its low end.
//
// Slopes have no item of their own: the material's FiligreeRailingItem places them, set to a
// slope shape in its radial menu. Holding it set to a slope and looking at the same kind of
// slope shows Create's placement arrow for the next piece uphill or downhill; set to flat, it
// shows one for continuing flat off an end.
public class FiligreeRailingSlopeBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, IWrenchable {
    public static final MapCodec<FiligreeRailingSlopeBlock> CODEC = simpleCodec(FiligreeRailingSlopeBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Outline and collision: stepped slices that follow each shape's rails, per facing.
    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = new EnumMap<>(Part.class);
    static {
        for (Part part : Part.values()) {
            Map<Direction, VoxelShape> byFacing = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Plane.HORIZONTAL)
                byFacing.put(facing, slopeShape(part, facing));
            SHAPES.put(part, byFacing);
        }
    }

    private static final int SLOPE_HELPER = PlacementHelpers.register(new SlopeHelper());
    private static final int FLAT_HELPER = PlacementHelpers.register(new FlatOffEndHelper());

    public FiligreeRailingSlopeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.LOWER).setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WATERLOGGED);
    }

    // Placed by hand: the start of a gentle climb, rising away from the player.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !player.mayBuild() || !(stack.getItem() instanceof BlockItem item))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        for (int id : new int[]{SLOPE_HELPER, FLAT_HELPER}) {
            IPlacementHelper helper = PlacementHelpers.get(id);
            if (helper.matchesItem(stack))
                return helper.getOffset(player, level, state, pos, hit).placeInWorld(level, item, player, hand, hit);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Where the next slope piece goes going `dir` (uphill or downhill) from this one.
    static BlockPos nextPos(BlockState state, BlockPos pos, Direction dir) {
        boolean uphill = dir == state.getValue(FACING);
        BlockPos next = pos.relative(dir);
        return switch (state.getValue(PART)) {
            case LOWER -> uphill ? next.above() : next;
            case UPPER -> uphill ? next : next.below();
            case FULL -> uphill ? next.above() : next.below();
        };
    }

    static Part nextPart(BlockState state) {
        return switch (state.getValue(PART)) {
            case LOWER -> Part.UPPER;
            case UPPER -> Part.LOWER;
            case FULL -> Part.FULL;
        };
    }

    // An end a flat railing joins: the side it's on, and its height relative to the slope.
    record FlatEnd(Direction side, int dy) {
        BlockPos at(BlockPos slopePos) {
            return slopePos.relative(side).above(dy);
        }
    }

    static List<FlatEnd> flatEnds(BlockState state) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(PART)) {
            case LOWER -> List.of(new FlatEnd(facing.getOpposite(), 0), new FlatEnd(facing, 1));
            case UPPER -> List.of();
            case FULL -> List.of(new FlatEnd(facing, 0), new FlatEnd(facing.getOpposite(), -1));
        };
    }

    // A flat railing diagonally above or below an end (a gentle slope's END, a full slope's low
    // end) never hears about the slope through normal neighbour updates, so refresh it here.
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        refreshDiagonalFlat(level, state, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        refreshDiagonalFlat(level, state, pos);
    }

    private static void refreshDiagonalFlat(Level level, BlockState state, BlockPos pos) {
        if (level.isClientSide) return;
        Direction facing = state.getValue(FACING);
        // Both diagonals, whatever this slope is (or was): a removed or rotated slope has to
        // clear the transition it gave its old neighbour too.
        for (Direction side : new Direction[]{facing, facing.getOpposite()}) {
            for (int dy : new int[]{1, -1}) {
                BlockPos flatPos = pos.relative(side).above(dy);
                BlockState flat = level.getBlockState(flatPos);
                if (flat.getBlock() instanceof FiligreeRailingBlock) {
                    BlockState updated = FiligreeRailingBlock.connectedState(flat, level, flatPos);
                    if (updated != flat) level.setBlock(flatPos, updated, Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(PART)).get(state.getValue(FACING));
    }

    // Height of the bottom rail, in pixels, at distance z from the north (uphill) edge of a slope
    // authored climbing north. The panel is about 15 pixels tall above it, like a flat railing.
    private static float bottomRail(Part part, float z) {
        return switch (part) {
            case LOWER -> (16 - z) / 2 + 4;   // 4 at the south edge, 12 at the north edge
            case UPPER -> -z / 2 + 4;         // -4 at the south edge, 4 at the north edge
            case FULL -> -z;                  // -16 at the south edge, 0 at the north edge
        };
    }

    // The transition pieces, authored climbing north: START is flat in its south half and rises
    // to 4 pixels at the north edge, END dips to -4 at the south edge and is flat in its north half.
    private static float transitionBottomRail(RailingTransition.Kind kind, float z) {
        return switch (kind) {
            case START -> z < 8 ? (8 - z) / 2 : 0;
            case END -> z > 8 ? -(z - 8) / 2 : 0;
        };
    }

    // Outline and collision of a flat railing showing a transition piece.
    private static final Map<RailingTransition, VoxelShape> TRANSITION_SHAPES = new EnumMap<>(RailingTransition.class);
    static {
        for (RailingTransition t : RailingTransition.values()) {
            if (t.kind == null) continue;
            TRANSITION_SHAPES.put(t, slicedShape(z -> transitionBottomRail(t.kind, z), t.climb));
        }
    }

    static VoxelShape transitionShape(RailingTransition transition) {
        return TRANSITION_SHAPES.get(transition);
    }

    private static VoxelShape slopeShape(Part part, Direction facing) {
        return slicedShape(z -> bottomRail(part, z), facing);
    }

    // The rails, sliced every 2 pixels along the climb and clipped to the block, then turned from
    // north to `facing` the way the blockstate turns the model.
    private static VoxelShape slicedShape(java.util.function.Function<Float, Float> bottom, Direction facing) {
        VoxelShape shape = Shapes.empty();
        for (int z = 0; z < 16; z += 2) {
            float a = bottom.apply((float) z), b = bottom.apply((float) z + 2);
            float low = Math.max(0, Math.min(a, b));
            float high = Math.min(16, Math.max(a, b) + 15);
            if (high <= low) continue;
            shape = Shapes.or(shape, rotated(7, low, z, 9, high, z + 2, facing));
        }
        return shape.optimize();
    }

    private static VoxelShape rotated(float x1, float y1, float z1, float x2, float y2, float z2, Direction facing) {
        return switch (facing) {
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // Whether the player holds a railing item set to place this block.
    private static boolean holds(Player player, Block block) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof FiligreeRailingItem item && item.blockFor(stack) == block)
                return true;
        }
        return false;
    }

    private static boolean isSlopeItem(ItemStack stack) {
        return stack.getItem() instanceof FiligreeRailingItem && FiligreeRailingItem.getShape(stack) != RailingShape.FLAT;
    }

    // There's no slope item: pick-block gives the railing item set to this shape.
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        RailingShape shape = this instanceof FiligreeRailingSteepSlopeBlock ? RailingShape.STEEP_SLOPE : RailingShape.SLOPE;
        return FiligreeRailingItem.stackOf(asItem(), shape);
    }

    public enum Part implements StringRepresentable {
        LOWER("lower"), UPPER("upper"), FULL("full");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // Holding a slope, looking at a slope: the next piece uphill or downhill, whichever side
    // is looked at, skipping along pieces that are already there.
    private static class SlopeHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return FiligreeRailingSlopeBlock::isSlopeItem;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof FiligreeRailingSlopeBlock;
        }

        // Keep the slope ghost set above instead of the held item's (flat) block.
        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray,
                                         ItemStack heldItem) {
            return getOffset(player, level, state, pos, ray);
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
            // Gentle and steep slopes don't continue each other.
            if (player == null || !holds(player, state.getBlock())) return PlacementOffset.fail();
            Direction facing = state.getValue(FACING);
            List<Direction> dirs = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), facing.getAxis());
            for (Direction dir : dirs) {
                BlockState current = state;
                BlockPos currentPos = pos;
                for (int step = 0; step < 16; step++) {
                    BlockPos next = nextPos(current, currentPos, dir);
                    Part part = nextPart(current);
                    BlockState there = level.getBlockState(next);
                    if (there.canBeReplaced()) {
                        // The held item's own block is the flat railing, so place this block instead.
                        BlockState placed = state.getBlock().defaultBlockState().setValue(FACING, facing).setValue(PART, part);
                        return PlacementOffset.success(next, s -> placed).withGhostState(placed);
                    }
                    // Follow a slope that already continues this way.
                    if (!(there.getBlock() instanceof FiligreeRailingSlopeBlock)
                            || there.getValue(FACING) != facing || there.getValue(PART) != part)
                        break;
                    current = there;
                    currentPos = next;
                }
            }
            return PlacementOffset.fail();
        }
    }

    // Holding a flat railing, looking at a slope: a flat railing off the end looked at.
    private static class FlatOffEndHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return stack -> stack.getItem() instanceof FiligreeRailingItem && FiligreeRailingItem.getShape(stack) == RailingShape.FLAT;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.getBlock() instanceof FiligreeRailingSlopeBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level level, BlockState state, BlockPos pos, BlockHitResult ray) {
            List<FlatEnd> ends = flatEnds(state);
            for (Direction dir : IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), state.getValue(FACING).getAxis())) {
                for (FlatEnd end : ends) {
                    if (end.side() != dir) continue;
                    BlockPos target = end.at(pos);
                    if (level.getBlockState(target).canBeReplaced())
                        return PlacementOffset.success(target, s -> FiligreeRailingBlock.connectedState(s, level, target));
                }
            }
            return PlacementOffset.fail();
        }
    }
}
