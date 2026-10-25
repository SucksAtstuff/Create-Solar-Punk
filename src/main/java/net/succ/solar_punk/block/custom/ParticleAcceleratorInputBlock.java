package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.ParticleAcceleratorInputBlockEntity;

// The Particle Accelerator's controller: a straight ring piece that takes shaft power from
// below and items through its side cap, and gives the result back out of the cap.
public class ParticleAcceleratorInputBlock extends HorizontalKineticBlock implements IBE<ParticleAcceleratorInputBlockEntity> {

    public ParticleAcceleratorInputBlock(Properties properties) {
        super(properties);
    }

    // Faces away from the player, like the pipes, so the ring runs the way you look.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    // Like the pipes, the wrench always turns it around the vertical axis.
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.setValue(HORIZONTAL_FACING, originalState.getValue(HORIZONTAL_FACING).getClockWise());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ParticleAcceleratorPipeBlock.inputShape(state.getValue(HORIZONTAL_FACING));
    }

    // Passes block events (the beams' collision when a craft finishes) to the block entity
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    public Class<ParticleAcceleratorInputBlockEntity> getBlockEntityClass() {
        return ParticleAcceleratorInputBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ParticleAcceleratorInputBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PARTICLE_ACCELERATOR_INPUT.get();
    }
}
