package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import net.succ.solar_punk.item.ModItems;

/**
 * A two-block-tall crop. The lower half is a normal crop on farmland and does all the
 * growing; from {@link #TOP_AGE} on it keeps an upper half above it at the same age. Only
 * the lower half drops anything (see ModBlockLootTablesProvider), so breaking the top in
 * survival breaks the lower half for the player, and a Harvester that only reaches the top
 * gets nothing (the top grows back).
 */
public class HempCropBlock extends CropBlock {
    public static final MapCodec<HempCropBlock> CODEC = simpleCodec(HempCropBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    /** The age at which the upper half appears. */
    public static final int TOP_AGE = 4;

    private static final VoxelShape[] LOWER_SHAPES = {
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(0, 0, 0, 16, 8, 16),
            Block.box(0, 0, 0, 16, 13, 16),
            Block.box(0, 0, 0, 16, 13, 16),
    };
    private static final VoxelShape FULL = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape[] UPPER_SHAPES = {
            Block.box(0, 0, 0, 16, 4, 16),   // age 4
            Block.box(0, 0, 0, 16, 7, 16),   // age 5
            Block.box(0, 0, 0, 16, 10, 16),  // age 6
            Block.box(0, 0, 0, 16, 14, 16),  // age 7
    };

    public HempCropBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<? extends CropBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.HEMP_SEEDS.get();
    }

    private static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = getAge(state);
        if (isUpper(state)) return UPPER_SHAPES[Math.max(0, age - TOP_AGE)];
        return age >= TOP_AGE ? FULL : LOWER_SHAPES[age];
    }

    // -------------------------------------------------------------------------
    // Survival: the lower half is a crop on farmland, the upper half needs a grown lower half
    // -------------------------------------------------------------------------

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!isUpper(state)) return super.canSurvive(state, level, pos);
        BlockState below = level.getBlockState(pos.below());
        return getAge(state) >= TOP_AGE && below.is(this) && !isUpper(below);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (isUpper(state) && direction == Direction.DOWN && !canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    // Breaking the top in survival harvests the lower half, which holds the drops.
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isUpper(state)) {
            BlockPos below = pos.below();
            if (level.getBlockState(below).is(this)) {
                if (player.isCreative()) level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                else level.destroyBlock(below, true, player);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // -------------------------------------------------------------------------
    // Growth, always driven through the lower half
    // -------------------------------------------------------------------------

    // Both halves tick once the top is up (so greenhouses and Grow Lamps reach whichever half
    // they find first), each passing on half of its ticks, so growth stays at crop speed.
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        BlockPos lowerPos = isUpper(state) ? pos.below() : pos;
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this) || isUpper(lower)) return;
        int age = getAge(lower);
        if (age >= TOP_AGE && random.nextBoolean()) return;

        // A grown lower half whose top was cut off (by a Harvester) grows it back.
        if (age >= TOP_AGE && level.isEmptyBlock(lowerPos.above())) {
            placeTop(level, lowerPos, age);
            return;
        }
        if (age >= getMaxAge() || level.getRawBrightness(lowerPos, 0) < 9) return;
        float speed = getGrowthSpeed(lower, level, lowerPos);
        if (CommonHooks.canCropGrow(level, lowerPos, lower, random.nextInt((int) (25.0F / speed) + 1) == 0)) {
            setAge(level, lowerPos, age + 1);
            CommonHooks.fireCropGrowPost(level, lowerPos, lower);
        }
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        BlockPos lowerPos = isUpper(state) ? pos.below() : pos;
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this) || isUpper(lower)) return;
        setAge(level, lowerPos, Math.min(getMaxAge(), getAge(lower) + getBonemealAgeIncrease(level)));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !isMaxAge(state) && (getAge(state) + 1 < TOP_AGE || isUpper(state) || canGrowTop(level, pos));
    }

    /** Sets the lower half's age, adding or updating the top. Stops short of TOP_AGE if the top has no room. */
    private void setAge(Level level, BlockPos lowerPos, int age) {
        if (age >= TOP_AGE && !canGrowTop(level, lowerPos)) age = TOP_AGE - 1;
        level.setBlock(lowerPos, getStateForAge(age), Block.UPDATE_CLIENTS);
        if (age >= TOP_AGE) placeTop(level, lowerPos, age);
    }

    private void placeTop(Level level, BlockPos lowerPos, int age) {
        level.setBlock(lowerPos.above(), getStateForAge(age).setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
    }

    private boolean canGrowTop(LevelReader level, BlockPos lowerPos) {
        BlockState above = level.getBlockState(lowerPos.above());
        return above.isAir() || above.is(this);
    }
}
