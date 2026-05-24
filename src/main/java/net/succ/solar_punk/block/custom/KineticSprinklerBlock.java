package net.succ.solar_punk.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity;

public class KineticSprinklerBlock extends Block implements IBE<KineticSprinklerBlockEntity> {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 6, 16),
        Block.box(3, 6, 3, 13, 11, 13)
    );

    public KineticSprinklerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.KINETIC_SPRINKLER.get())
            return (BlockEntityTicker<T>) (BlockEntityTicker<KineticSprinklerBlockEntity>) (l, p, s, be) -> be.tick();
        return null;
    }

    @Override
    public Class<KineticSprinklerBlockEntity> getBlockEntityClass() {
        return KineticSprinklerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticSprinklerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.KINETIC_SPRINKLER.get();
    }
}
