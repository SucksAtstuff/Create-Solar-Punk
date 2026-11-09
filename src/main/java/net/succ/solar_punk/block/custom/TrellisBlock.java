package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.phys.shapes.VoxelShape;

// Hangs on a wall like a ladder (and is climbable like one, via #minecraft:climbable).
// Plants go on in layers (see TrellisPlant): right-click with Vines, then optionally a small
// flower or Glow Berries on top. Shears take the top layer off again. Vines slowly climb to
// bare trellises above, or sideways when there's none above. Vines are tinted with the biome's
// foliage colour and flowers with their flower's colour (ModClientEvents.onRegisterBlockColors).
public class TrellisBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<TrellisBlock> CODEC = simpleCodec(TrellisBlock::new);
    public static final EnumProperty<TrellisPlant> PLANT = EnumProperty.create("plant", TrellisPlant.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // FACING points away from the wall, so a north-facing trellis sits on the south edge.
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape WEST_SHAPE = Block.box(13, 0, 0, 16, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(0, 0, 0, 3, 16, 16);

    public TrellisBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PLANT, TrellisPlant.NONE)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PLANT, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = defaultBlockState().setValue(WATERLOGGED, water);
        // The clicked wall first, then whichever wall the player is looking towards.
        if (context.getClickedFace().getAxis().isHorizontal()) {
            BlockState onClicked = state.setValue(FACING, context.getClickedFace());
            if (onClicked.canSurvive(context.getLevel(), context.getClickedPos())) return onClicked;
        }
        for (Direction direction : context.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            BlockState candidate = state.setValue(FACING, direction.getOpposite());
            if (candidate.canSurvive(context.getLevel(), context.getClickedPos())) return candidate;
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(PLANT).hasVines();
    }

    // About as fast as vanilla vines spread (they try on 1 in 4 random ticks).
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        BlockPos target = pos.above();
        if (!isBareTrellis(level, target)) {
            target = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
            if (!isBareTrellis(level, target)) return;
        }
        level.setBlock(target, level.getBlockState(target).setValue(PLANT, TrellisPlant.VINES), Block.UPDATE_ALL);
    }

    private boolean isBareTrellis(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(this) && state.getValue(PLANT) == TrellisPlant.NONE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        TrellisPlant plant = state.getValue(PLANT);
        TrellisPlant adding = TrellisPlant.forItem(stack);
        // Vines on a bare trellis; a flower or berries only on plain vines.
        boolean fits = adding == TrellisPlant.VINES
                ? plant == TrellisPlant.NONE
                : adding != null && plant == TrellisPlant.VINES;
        if (fits) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(PLANT, adding), Block.UPDATE_ALL);
                level.playSound(null, pos, adding == TrellisPlant.VINES ? SoundEvents.VINE_PLACE : SoundEvents.CAVE_VINES_PLACE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (plant != TrellisPlant.NONE && stack.is(Items.SHEARS)) {
            if (!level.isClientSide) {
                TrellisPlant below = plant == TrellisPlant.VINES ? TrellisPlant.NONE : TrellisPlant.VINES;
                level.setBlock(pos, state.setValue(PLANT, below), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0f, 1.0f);
                popResourceFromFace(level, pos, hit.getDirection(), new ItemStack(plant.item()));
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
