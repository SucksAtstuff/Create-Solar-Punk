package net.succ.create_solar_powered.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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
import net.succ.create_solar_powered.block.entity.custom.HeatBatteryBlockEntity;

public class HeatBatteryBlock extends Block implements IBE<HeatBatteryBlockEntity> {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public HeatBatteryBlock(Properties properties) {
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
        if (!(level.getBlockEntity(pos) instanceof HeatBatteryBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (FluidUtil.interactWithFluidHandler(player, hand, be.fluidTank))
            return ItemInteractionResult.SUCCESS;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<HeatBatteryBlockEntity> getBlockEntityClass() {
        return HeatBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HeatBatteryBlockEntity> getBlockEntityType() {
        return ModBlockEntities.HEAT_BATTERY.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.HEAT_BATTERY.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<HeatBatteryBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
