package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.EndPlateBlockEntity;
import net.succ.solar_punk.util.MachineInteractions;

import java.util.EnumMap;
import java.util.Map;

/**
 * Clamps one end of a Fuel Cell stack. FACING points away from the stack: the flat plate
 * touches the cells, and the rod ends and two ports face out. The port on FACING's clockwise
 * side (the model's east) takes Hydrogen and Oxygen; the one on the counter-clockwise side
 * (the model's west) gives out Water. See EndPlateBlockEntity.
 */
public class EndPlateBlock extends Block implements IBE<EndPlateBlockEntity>, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        // As modelled facing north: the plate on the south side, and the two ports.
        for (Direction dir : Direction.values()) {
            SHAPES.put(dir, Shapes.or(
                    box(dir, 0, 0, 13, 16, 16, 16),
                    box(dir, 0, 3, 3, 7, 13, 13),
                    box(dir, 9, 3, 3, 16, 13, 13)));
        }
    }

    // A box in model pixels (facing north), turned the way the blockstate turns the model.
    private static VoxelShape box(Direction dir, double x1, double y1, double z1, double x2, double y2, double z2) {
        return switch (dir) {
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            // x = 90: north turns to face down.
            case DOWN -> Block.box(x1, z1, 16 - y2, x2, z2, 16 - y1);
            // x = 270: north turns to face up.
            case UP -> Block.box(x1, 16 - z2, y1, x2, 16 - z1, y2);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    public EndPlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACING);
    }

    // Placed against the end of a stack (or anything), it faces away from what was clicked.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** The face the gas port opens onto. */
    public static Direction gasSide(BlockState state) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis().isVertical() ? Direction.EAST : facing.getClockWise();
    }

    /** The face the Water port opens onto. */
    public static Direction waterSide(BlockState state) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis().isVertical() ? Direction.WEST : facing.getCounterClockWise();
    }

    // Buckets: Hydrogen or Oxygen in, an empty one takes Water out.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EndPlateBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return MachineInteractions.useItem(level, player, hand, stack, be, be.fluidHandler(null), null, 0);
    }

    @Override
    public Class<EndPlateBlockEntity> getBlockEntityClass() {
        return EndPlateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EndPlateBlockEntity> getBlockEntityType() {
        return ModBlockEntities.END_PLATE.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.END_PLATE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<EndPlateBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
