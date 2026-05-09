package net.succ.create_solar_powered.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;

public class SolarHeaterBlock extends Block implements IBE<SolarHeaterBlockEntity> {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SolarHeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
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
