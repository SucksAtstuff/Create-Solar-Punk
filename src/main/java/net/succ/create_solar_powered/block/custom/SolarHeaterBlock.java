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
import net.succ.create_solar_powered.block.entity.custom.SolarHeaterBlockEntity;

public class SolarHeaterBlock extends Block implements IBE<SolarHeaterBlockEntity> {

    public SolarHeaterBlock(Properties properties) {
        super(properties);
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