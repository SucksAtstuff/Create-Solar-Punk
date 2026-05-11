package net.succ.create_solar_powered.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
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
import net.neoforged.neoforge.fluids.FluidUtil;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;

public class SolarHeaterBlock extends Block implements IBE<SolarHeaterBlockEntity>, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Arms run along X when facing NORTH or SOUTH; along Z when facing EAST or WEST.
    private static final VoxelShape SHAPE_NS = Shapes.or(
        Block.box(1, 0, 1, 15, 3, 15),
        Block.box(0, 1, 6, 2, 9, 10),
        Block.box(14, 1, 6, 16, 9, 10),
        Block.box(2, 5, 2, 14, 12, 14)
    );
    private static final VoxelShape SHAPE_EW = Shapes.or(
        Block.box(1, 0, 1, 15, 3, 15),
        Block.box(6, 1, 0, 10, 9, 2),
        Block.box(6, 1, 14, 10, 9, 16),
        Block.box(2, 5, 2, 14, 12, 14)
    );

    public SolarHeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SolarHeaterBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Bucket fill/empty
        if (FluidUtil.interactWithFluidHandler(player, hand, be.fluidTank)) {
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack simResult = be.itemHandler.insertItem(0, stack, true);
        if (simResult.getCount() < stack.getCount()) {
            ItemStack remaining = be.itemHandler.insertItem(0, stack, false);
            if (!player.isCreative()) player.setItemInHand(hand, remaining);
            be.setChanged();
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SolarHeaterBlockEntity be)) return InteractionResult.PASS;
        ItemStack extracted = be.itemHandler.extractItem(0, 64, false);
        if (!extracted.isEmpty()) {
            player.getInventory().add(extracted);
            be.setChanged();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return (facing == Direction.EAST || facing == Direction.WEST) ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    public Class<SolarHeaterBlockEntity> getBlockEntityClass() {
        return SolarHeaterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SolarHeaterBlockEntity> getBlockEntityType() {
        return ModBlockEntities.SOLAR_HEATER.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SOLAR_HEATER.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<SolarHeaterBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
