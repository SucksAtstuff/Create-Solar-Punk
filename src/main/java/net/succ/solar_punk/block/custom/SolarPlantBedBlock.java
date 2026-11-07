package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.SolarPlantBedBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Solar Plant Bed: a raised bed of always-watered soil with a small panel and grow light on
 * posts above it; see SolarPlantBedBlockEntity. The plant grows in the cell above, which the
 * model's posts and canopy reach into. Only the bed itself has a shape, so the plant can
 * still be clicked and harvested.
 *
 * The shape must not be a full cube: the renderer lights every face of a full-cube block from
 * the block beside that face, so the canopy's undersides would take the light of the ground
 * under the bed and render black.
 */
public class SolarPlantBedBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // The soil box with the rim around its top half (matches the model).
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 15, 15),
            Block.box(0, 8, 0, 16, 16, 1),
            Block.box(0, 8, 15, 16, 16, 16),
            Block.box(0, 8, 1, 1, 16, 15),
            Block.box(15, 8, 1, 16, 16, 15));

    public SolarPlantBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Anything that grows on dirt or farmland: crops, stems, flowers, saplings, berry bushes.
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return facing == Direction.UP && plant.getBlock() instanceof BushBlock ? TriState.TRUE : TriState.DEFAULT;
    }

    // Counts as watered farmland, so crops grow at the hydrated speed with no water nearby.
    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPlantBedBlockEntity(ModBlockEntities.SOLAR_PLANT_BED.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.SOLAR_PLANT_BED.get()) return null;
        return (l, p, s, be) -> ((SolarPlantBedBlockEntity) be).tick();
    }
}
