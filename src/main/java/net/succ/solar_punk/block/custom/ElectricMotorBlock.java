package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.ElectricMotorBlockEntity;

import java.util.EnumMap;
import java.util.Map;

/**
 * Turns FE into rotation. Sits on its base plate with the shaft out of the FACING side (the
 * model's north); FE goes in through any face. See ElectricMotorBlockEntity.
 */
public class ElectricMotorBlock extends KineticBlock implements IBE<ElectricMotorBlockEntity> {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        // Base plate, body and back plate, as modelled facing north; the shaft is drawn by the renderer.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            SHAPES.put(dir, Shapes.or(
                    box(dir, 0, 0, 4, 16, 2, 13),
                    box(dir, 1, 1, 1, 15, 15, 15),
                    box(dir, 4, 4, 15, 12, 12, 16)));
        }
    }

    // A box in model pixels (facing north), turned to face dir.
    private static VoxelShape box(Direction dir, double x1, double y1, double z1, double x2, double y2, double z2) {
        return switch (dir) {
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    public ElectricMotorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACING);
    }

    // Clicking the side of a block points the shaft into it; on a floor it points away from you.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        Direction facing = clicked.getAxis().isHorizontal() ? clicked.getOpposite() : context.getHorizontalDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) facing = facing.getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Class<ElectricMotorBlockEntity> getBlockEntityClass() {
        return ElectricMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectricMotorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTRIC_MOTOR.get();
    }
}
