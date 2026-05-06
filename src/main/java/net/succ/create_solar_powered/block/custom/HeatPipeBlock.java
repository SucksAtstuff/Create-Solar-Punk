package net.succ.create_solar_powered.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.block.entity.custom.HeatPipeBlockEntity;

public class HeatPipeBlock extends Block implements IBE<HeatPipeBlockEntity> {

    public HeatPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<HeatPipeBlockEntity> getBlockEntityClass() {
        return HeatPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HeatPipeBlockEntity> getBlockEntityType() {
        return ModBlockEntities.HEAT_PIPE.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.HEAT_PIPE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<HeatPipeBlockEntity>) (l, p, s, be) -> be.tick();
    }
}