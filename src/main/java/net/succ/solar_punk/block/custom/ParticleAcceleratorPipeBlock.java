package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Particle Accelerator ring pieces: the straight pipe, the 45 degree bend and the
// diagonal pipe. Placeholder for now: they only place facing away from the player and
// have no ring logic yet (see docs/PARTICLE_ACCELERATOR_PLAN.md).
public class ParticleAcceleratorPipeBlock extends HorizontalDirectionalBlock {

    // Stand (platform, post and the arms holding the tube) plus the tube, for a pipe
    // running along Z (facing north/south) or X (facing east/west).
    private static final VoxelShape STRAIGHT_Z = Shapes.or(
            Block.box(5, 0, 5, 11, 1, 11),
            Block.box(7, 1, 7, 9, 6, 9),
            Block.box(5, 4, 7, 11, 8, 9),
            Block.box(6, 6, 0, 10, 10, 16));
    private static final VoxelShape STRAIGHT_X = Shapes.or(
            Block.box(5, 0, 5, 11, 1, 11),
            Block.box(7, 1, 7, 9, 6, 9),
            Block.box(7, 4, 5, 9, 8, 11),
            Block.box(0, 6, 6, 16, 10, 10));
    // The bend and diagonal tubes run at an angle, which a box can't follow, so they
    // get a hitbox covering the whole footprint up to the top of the tube.
    private static final VoxelShape ANGLED = Block.box(0, 0, 0, 16, 10, 16);

    private final boolean straight;
    private final MapCodec<ParticleAcceleratorPipeBlock> codec;

    public ParticleAcceleratorPipeBlock(Properties properties, boolean straight) {
        super(properties);
        this.straight = straight;
        this.codec = simpleCodec(p -> new ParticleAcceleratorPipeBlock(p, straight));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!straight) return ANGLED;
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? STRAIGHT_X : STRAIGHT_Z;
    }
}