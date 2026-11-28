package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

/**
 * A corroded, dead Streetlamp for ruins: the Streetlamp's model with streetlamp_broken.png, no
 * block entity and no light. Two cells tall like the Streetlamp: the lower half draws the whole
 * lamp and the upper half is an invisible placeholder; removing either removes both. An axe
 * scrapes it back into a working Streetlamp, like the Rusted Geyser Cap.
 */
public class BrokenStreetlampBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public BrokenStreetlampBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;
        if (!level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? StreetlampBlock.LOWER_SHAPE : StreetlampBlock.UPPER_SHAPE;
    }

    // Like StreetlampBlock: a half whose partner is gone goes too
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction == (half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN)) {
            boolean partnerThere = neighborState.is(this) && neighborState.getValue(HALF) != half;
            if (!partnerThere) return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // Like vanilla doors: a creative player breaking the upper half removes the lower half
    // quietly first, so it doesn't drop.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.is(this)) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, below, Block.getId(belowState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!RuinRepair.canScrape(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lamp = ModBlocks.STREETLAMP.get().defaultBlockState();
        if (!level.isClientSide) {
            // Both halves at once, without shape updates in between, or each half would see its
            // partner gone and remove itself (as in RuinedSolarMirrorBlock)
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            level.setBlock(lower.above(), lamp.setValue(StreetlampBlock.HALF, DoubleBlockHalf.UPPER), flags);
            level.setBlock(lower, lamp.setValue(StreetlampBlock.HALF, DoubleBlockHalf.LOWER), flags);
            level.blockUpdated(lower, lamp.getBlock());
            level.blockUpdated(lower.above(), lamp.getBlock());
        }
        RuinRepair.scrape(level, pos, lamp, player, stack, hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
