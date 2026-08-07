package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import net.neoforged.neoforge.fluids.FluidUtil;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FireboxBoilerBlockEntity;

// Early-game, non-multiblock Steam source: burns any vanilla furnace fuel to boil
// water into the mod's Steam fluid. Deliberately weak per-block (see
// Config.fireboxBoilerSteamPerTick) so it bootstraps a Steam Turbine before a Solar
// Power Tower is built, without ever being the block-efficient endgame choice.
public class FireboxBoilerBlock extends Block implements IBE<FireboxBoilerBlockEntity>, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FireboxBoilerBlock(Properties properties) {
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
        if (!(level.getBlockEntity(pos) instanceof FireboxBoilerBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Bucket fill/empty - water in, steam out
        if (FluidUtil.interactWithFluidHandler(player, hand, be.combinedFluidHandler)) {
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
        if (!(level.getBlockEntity(pos) instanceof FireboxBoilerBlockEntity be)) return InteractionResult.PASS;
        ItemStack extracted = be.itemHandler.extractItem(0, 64, false);
        if (!extracted.isEmpty()) {
            player.getInventory().add(extracted);
            be.setChanged();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FireboxBoilerBlockEntity be) {
            for (int i = 0; i < be.itemHandler.getSlots(); i++)
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.itemHandler.getStackInSlot(i));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<FireboxBoilerBlockEntity> getBlockEntityClass() {
        return FireboxBoilerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FireboxBoilerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FIREBOX_BOILER.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.FIREBOX_BOILER.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<FireboxBoilerBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
