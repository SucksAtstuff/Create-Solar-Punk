package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.SolarMirrorBlockEntity;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A heliostat mirror. {@link #FACING} only decides which face the static mount is
 * bolted to at placement time - the reflective plate itself is rendered separately
 * and continuously reoriented to track the sun by {@link SolarMirrorBlockEntity}'s
 * renderer, once a nearby Solar Power Tower has linked it. Wrenching just picks the
 * block up like any other Create block; there is no manual facing to cycle anymore.
 *
 * <p>The post and mirror plate physically poke up into the block above the one that
 * was placed (they're taller than a single cell), so like a door or a tall flower this
 * occupies two vertical cells: a {@link DoubleBlockHalf#LOWER} half holding the block
 * entity/renderer, and a bare {@link DoubleBlockHalf#UPPER} half that only exists to
 * reserve that space - collision-wise and for placement purposes - and to keep the two
 * halves in sync (removing either one removes both). Only floor placement is supported:
 * the dynamic tracking renderer always treats the post as pointing world-up regardless
 * of {@link #FACING}, so a wall/ceiling mount would never visually line up with its own
 * base anyway.
 */
public class SolarMirrorBlock extends Block implements EntityBlock, IWrenchable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Lower half: the static base plate plus the post's lower segment (matches
    // solar_mirror_block.json / solar_mirror_post.json).
    private static final VoxelShape LOWER_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(7, 2, 7, 9, 16, 9));
    // Upper half: the post's remaining segment, plus a generous box approximating the
    // mirror plate's swept volume as it tracks the sun (it rotates freely, so an exact
    // dynamic hitbox isn't practical - this just keeps the reserved space honest).
    private static final VoxelShape UPPER_SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 6, 9),
            Block.box(1, 2, 1, 15, 6, 15));

    public SolarMirrorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Only floor placement is supported (see class doc) - the post needs to poke up
        // into the block above, so that cell has to be free.
        if (context.getClickedFace() != Direction.UP) return null;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (pos.getY() >= level.getMaxBuildHeight() - 1) return null;
        if (!level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, Direction.UP).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), defaultBlockState().setValue(FACING, Direction.UP).setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
    }

    // Keeps the two halves as a single unit regardless of how either one disappears
    // (mining, wrench pickup, explosion, /setblock, ...): if the half that's supposed
    // to be its partner isn't there anymore, this half goes too.
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        Direction expectedNeighborDir = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (direction == expectedNeighborDir) {
            DoubleBlockHalf expectedNeighborHalf = half == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
            boolean partnerStillThere = neighborState.is(this) && neighborState.getValue(HALF) == expectedNeighborHalf;
            if (!partnerStillThere) return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SolarMirrorBlockEntity mirrorBE) {
            BlockPos towerPos = mirrorBE.getLinkedTower();
            if (towerPos != null && level.getBlockEntity(towerPos) instanceof SolarPowerTowerBlockEntity towerBE)
                towerBE.unregisterMirror(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return null;
        return new SolarMirrorBlockEntity(ModBlockEntities.SOLAR_MIRROR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
