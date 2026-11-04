package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.SolarGlassBlockEntity;
import org.jetbrains.annotations.Nullable;

// Glass that makes a little FE under open sky; see SolarGlassBlockEntity.
public class SolarGlassPaneBlock extends IronBarsBlock implements EntityBlock {
    public SolarGlassPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarGlassBlockEntity(ModBlockEntities.SOLAR_GLASS.get(), pos, state);
    }

    // Server only: generation and sharing are server logic.
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SOLAR_GLASS.get()) return null;
        return (l, p, s, be) -> ((SolarGlassBlockEntity) be).tick();
    }
}
