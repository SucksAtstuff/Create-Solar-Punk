package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.succ.solar_punk.block.ModBlocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A broken Solar Heater for ruins: the Solar Heater's model with a cracked, weathered mirror
 * (ruined_solar_heater.json), and no machine inside. Right-click it with 3 clear Glass Panes to
 * fix the mirror and turn it into a working Solar Heater facing the same way (RuinRepair).
 */
public class RuinedSolarHeaterBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Same as SolarHeaterBlock's: arms along X facing north/south, along Z facing east/west
    private static final VoxelShape SHAPE_NS = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(0, 1, 6, 2, 9, 10),
            Block.box(14, 1, 6, 16, 9, 10),
            Block.box(2, 5, 2, 14, 12, 14));
    private static final VoxelShape SHAPE_EW = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(6, 1, 0, 10, 9, 2),
            Block.box(6, 1, 14, 10, 9, 16),
            Block.box(2, 5, 2, 14, 12, 14));

    public RuinedSolarHeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    static final int REPAIR_PANES = 3;

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!RuinRepair.canRepair(stack, player, REPAIR_PANES))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            level.setBlock(pos, ModBlocks.SOLAR_HEATER.get().defaultBlockState()
                    .setValue(SolarHeaterBlock.FACING, state.getValue(FACING)), Block.UPDATE_ALL);
            RuinRepair.finish(level, pos, player, stack, REPAIR_PANES);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
