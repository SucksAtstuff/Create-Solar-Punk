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
import net.succ.offbyonelib.util.MachineInteractions;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.GeyserWellBlockEntity;

/**
 * Geyser Well (see GeyserWellBlockEntity). FACING runs along the two pipe ports; the model
 * faces north, with the ports on its north and south sides and the gauges east and west.
 */
public class GeyserWellBlock extends Block implements IBE<GeyserWellBlockEntity>, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // Base, body and both ports, along north-south; turned for east-west
    private static final VoxelShape SHAPE_Z = Shapes.or(
            Block.box(1, 0, 1, 15, 5, 15),
            Block.box(2, 5, 3, 14, 14, 13),
            Block.box(3, 3, 0, 13, 13, 16));
    private static final VoxelShape SHAPE_X = Shapes.or(
            Block.box(1, 0, 1, 15, 5, 15),
            Block.box(3, 5, 2, 13, 14, 14),
            Block.box(0, 3, 3, 16, 13, 13));

    public GeyserWellBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Buckets fill it with Water or take Steam out
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GeyserWellBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return MachineInteractions.useItem(level, player, hand, stack, be, be.fluidHandler, null, 0);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.GEYSER_WELL.get()
                ? (l, p, s, be) -> ((GeyserWellBlockEntity) be).tick()
                : null;
    }

    @Override
    public Class<GeyserWellBlockEntity> getBlockEntityClass() {
        return GeyserWellBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GeyserWellBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GEYSER_WELL.get();
    }
}
