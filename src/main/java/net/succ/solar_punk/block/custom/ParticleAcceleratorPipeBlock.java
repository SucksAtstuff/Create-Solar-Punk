package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;

import java.util.List;

// Particle Accelerator ring pieces: the straight pipe, the 45 degree bends and the
// diagonal pipe. They place facing away from the player; AcceleratorRing works out how
// they connect. The Input is its own kinetic block (ParticleAcceleratorInputBlock).
public class ParticleAcceleratorPipeBlock extends HorizontalDirectionalBlock implements IWrenchable {

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
    // The Input's hitbox: the straight pipe plus its side branch and item cap, which
    // stick out of the tube's left side (west when facing north), turned to each facing.
    private static final VoxelShape INPUT_NORTH = Shapes.or(STRAIGHT_Z, Block.box(1, 5, 5, 6, 11, 11));
    private static final VoxelShape INPUT_EAST  = Shapes.or(STRAIGHT_X, Block.box(5, 5, 1, 11, 11, 6));
    private static final VoxelShape INPUT_SOUTH = Shapes.or(STRAIGHT_Z, Block.box(10, 5, 5, 15, 11, 11));
    private static final VoxelShape INPUT_WEST  = Shapes.or(STRAIGHT_X, Block.box(5, 5, 10, 11, 11, 15));

    public enum Kind { STRAIGHT, ANGLED }

    private final Kind kind;
    private final MapCodec<ParticleAcceleratorPipeBlock> codec;

    public ParticleAcceleratorPipeBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        this.codec = simpleCodec(p -> new ParticleAcceleratorPipeBlock(p, kind));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Snaps onto the ring: tries all four rotations (the way the player looks first) and
    // takes the one that connects to the most neighbouring ring pieces. Bends need their
    // straight end toward the ring on one corner and away from it on the next, so "face
    // the way you look" alone would need the wrench at every corner.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction look = context.getHorizontalDirection();
        BlockState best = defaultBlockState().setValue(FACING, look);
        int bestConnections = 0;
        for (Direction facing : new Direction[]{look, look.getClockWise(), look.getOpposite(), look.getCounterClockWise()}) {
            BlockState state = defaultBlockState().setValue(FACING, facing);
            int connections = 0;
            for (AcceleratorRing.Port port : AcceleratorRing.portsOf(state)) {
                List<AcceleratorRing.Port> neighbour = AcceleratorRing.portsOf(
                        context.getLevel().getBlockState(port.from(context.getClickedPos())));
                if (neighbour != null && neighbour.contains(port.opposite())) connections++;
            }
            if (connections > bestConnections) {
                best = state;
                bestConnections = connections;
            }
        }
        return best;
    }

    // Ring pieces lie flat, so the wrench always turns them a quarter turn around the
    // vertical axis, whichever face was clicked. Sneak-wrenching picks them up (the
    // IWrenchable default).
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.setValue(FACING, originalState.getValue(FACING).getClockWise());
    }

    // The bends and diagonal have no item of their own: pick-block gives a pipe set to
    // this shape.
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        PipeShape shape = PipeShape.of(this);
        if (shape == null) return super.getCloneItemStack(level, pos, state);
        return ParticleAcceleratorPipeItem.stackOf(asItem(), shape);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (kind == Kind.ANGLED) return ANGLED;
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? STRAIGHT_X : STRAIGHT_Z;
    }

    static VoxelShape inputShape(Direction facing) {
        return switch (facing) {
            case EAST -> INPUT_EAST;
            case SOUTH -> INPUT_SOUTH;
            case WEST -> INPUT_WEST;
            default -> INPUT_NORTH;
        };
    }
}