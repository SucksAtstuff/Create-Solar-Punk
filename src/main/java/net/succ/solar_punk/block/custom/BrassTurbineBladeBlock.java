package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrassTurbineBladeBlock extends Block implements IWrenchable {

    public static final BooleanProperty HIDDEN  = BooleanProperty.create("hidden");
    // Which perpendicular direction the arm points - purely cosmetic for the idle
    // (non-spinning) model. The structure scan never checks this, only the block
    // type; the master rotor's scan keeps it (and AXIS) in sync once the blade is
    // part of a formed turbine, so placement-time defaults barely matter.
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    // Which axis the turbine this blade belongs to spins on - determines which way
    // the thin flat plate lies.
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    // Thin flat plate shapes, one per possible rotor axis (2/16 thick along that axis).
    private static final VoxelShape SHAPE_AXIS_Y = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE_AXIS_X = Block.box(0, 0, 0, 2, 16, 16);
    private static final VoxelShape SHAPE_AXIS_Z = Block.box(0, 0, 0, 16, 16, 2);

    public BrassTurbineBladeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.EAST)
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(HIDDEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS, HIDDEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(HIDDEN, false);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> SHAPE_AXIS_X;
            case Z -> SHAPE_AXIS_Z;
            default -> SHAPE_AXIS_Y;
        };
    }
}
