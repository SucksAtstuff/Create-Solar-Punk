package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.WireConnectorBlockEntity;
import net.succ.solar_punk.wire.WireNodeBlock;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * A small insulator that sticks to any face of a block. Spools string wires between
 * connectors, and energy the block pushes in comes out of the blocks under the other
 * connectors; see WireConnectorBlockEntity. Shears cut every wire on it.
 */
public class WireConnectorBlock extends Block implements EntityBlock, WireNodeBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Where wires attach: this far out from the block the connector sits on.
    public static final double ATTACH_HEIGHT = 5.5 / 16;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        for (Direction dir : Direction.values()) SHAPES.put(dir, shapeFacing(dir));
    }

    public WireConnectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    // The model (6/16 tall, 2/16 wide at the base) pointing away from the block it's on.
    private static VoxelShape shapeFacing(Direction dir) {
        double lo = 6.5 / 16, hi = 9.5 / 16, len = 6.5 / 16;
        double[] min = {lo, lo, lo}, max = {hi, hi, hi};
        int axis = dir.getAxis().ordinal();
        if (dir.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            min[axis] = 0;
            max[axis] = len;
        } else {
            min[axis] = 1 - len;
            max[axis] = 1;
        }
        return Shapes.box(min[0], min[1], min[2], max[0], max[1], max[2]);
    }

    /** Where wires meet this connector, in world coordinates. */
    public static Vec3 attachPoint(BlockPos pos, Direction facing) {
        double out = ATTACH_HEIGHT - 0.5;
        return Vec3.atCenterOf(pos).add(facing.getStepX() * out, facing.getStepY() * out, facing.getStepZ() * out);
    }

    @Override
    public Vec3 attachPoint(BlockState state, BlockPos pos, int terminal) {
        return attachPoint(pos, state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    // Needs something to sit on; any block will do, not only full faces.
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState on = level.getBlockState(pos.relative(state.getValue(FACING).getOpposite()));
        return !on.isAir() && !on.canBeReplaced();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Tags.Items.TOOLS_SHEAR)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof WireConnectorBlockEntity be) || be.connections().isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            be.cutAll();
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.2f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WireConnectorBlockEntity be)
            be.cutAll();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WireConnectorBlockEntity(ModBlockEntities.WIRE_CONNECTOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.WIRE_CONNECTOR.get()) return null;
        return (l, p, s, be) -> ((WireConnectorBlockEntity) be).tick();
    }
}
