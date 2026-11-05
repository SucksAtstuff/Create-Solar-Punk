package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.GrowLampBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * An FE-powered grow light tube; see GrowLampBlockEntity. Hangs under a ceiling, or from a
 * wall (or another Grow Lamp) at either end of the tube, which shows a mounting rail instead
 * (the CEILING state picks the model). It breaks when it loses all of those.
 */
public class GrowLampBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // The tube along Z, and with it the rail on top when there's no ceiling (matches the models).
    private static final VoxelShape SHAPE_Z = Block.box(6.5, 10, 0, 9.5, 16, 16);
    private static final VoxelShape SHAPE_X = Block.box(0, 10, 6.5, 16, 16, 9.5);

    public GrowLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(AXIS, Direction.Axis.Z).setValue(CEILING, true).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, CEILING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Run along the clicked wall, or the way the player is looking when hung from a ceiling.
        Direction.Axis axis = context.getClickedFace().getAxis().isHorizontal()
                ? context.getClickedFace().getAxis()
                : context.getHorizontalDirection().getAxis();
        BlockState state = defaultBlockState().setValue(AXIS, axis);
        if (!state.canSurvive(level, pos)) {
            state = state.setValue(AXIS, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
            if (!state.canSurvive(level, pos)) return null;
        }
        return state.setValue(CEILING, hasCeiling(level, pos));
    }

    private static boolean hasCeiling(BlockGetter level, BlockPos pos) {
        return isSupport(level.getBlockState(pos.above()));
    }

    private static boolean isSupport(BlockState state) {
        return !state.isAir() && !state.canBeReplaced();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (hasCeiling(level, pos)) return true;
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction end : Direction.values()) {
            if (end.getAxis() != axis) continue;
            BlockState neighbour = level.getBlockState(pos.relative(end));
            // Another lamp only holds this one up if it runs the same way, so the rail lines up.
            if (neighbour.getBlock() instanceof GrowLampBlock
                    ? neighbour.getValue(AXIS) == axis
                    : isSupport(neighbour))
                return true;
        }
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return state.setValue(CEILING, hasCeiling(level, pos));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrowLampBlockEntity(ModBlockEntities.GROW_LAMP.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.GROW_LAMP.get()) return null;
        return (l, p, s, be) -> ((GrowLampBlockEntity) be).tick();
    }
}
