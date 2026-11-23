package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.EndPlateBlockEntity;
import net.succ.offbyonelib.util.MachineInteractions;

/**
 * Clamps one end of a Fuel Cell stack. FACING points away from the stack: the flat plate
 * touches the cells, and the rod ends and two ports face out. The port on FACING's clockwise
 * side (the model's east) takes Hydrogen and Oxygen; the one on the counter-clockwise side
 * (the model's west) gives out Water. ROLL turns the plate around FACING in quarter turns (a
 * Wrench cycles it), so the ports can point any way; the rolled models are neoforge:composite
 * wrappers of end_plate.json with a root transform. See EndPlateBlockEntity.
 */
public class EndPlateBlock extends Block implements IBE<EndPlateBlockEntity>, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Quarter turns around FACING; 0 is the model as made. */
    public static final IntegerProperty ROLL = IntegerProperty.create("roll", 0, 3);

    private static final VoxelShape[] SHAPES = new VoxelShape[6 * 4];
    static {
        // As modelled facing north: the plate on the south side, and the two ports.
        for (Direction dir : Direction.values()) {
            for (int roll = 0; roll < 4; roll++) {
                SHAPES[dir.get3DDataValue() * 4 + roll] = Shapes.or(
                        box(dir, roll, 0, 0, 13, 16, 16, 16),
                        box(dir, roll, 0, 3, 3, 7, 13, 13),
                        box(dir, roll, 9, 3, 3, 16, 13, 13));
            }
        }
    }

    /**
     * A point in model space (facing north, centred on the block) turned the way the model is:
     * first the roll (the composite's root transform, a right-handed turn around Z, so +X goes
     * to +Y), then the blockstate's x/y rotation.
     */
    private static double[] toWorld(Direction dir, int roll, double x, double y, double z) {
        for (int i = 0; i < roll; i++) {
            double t = x;
            x = -y;
            y = t;
        }
        return switch (dir) {
            case SOUTH -> new double[]{-x, y, -z};
            case EAST -> new double[]{-z, y, x};
            case WEST -> new double[]{z, y, -x};
            // x = 90: north turns to face down.
            case DOWN -> new double[]{x, z, -y};
            // x = 270: north turns to face up.
            case UP -> new double[]{x, -z, y};
            default -> new double[]{x, y, z};
        };
    }

    // A box in model pixels (facing north), turned the way the model is turned.
    private static VoxelShape box(Direction dir, int roll, double x1, double y1, double z1, double x2, double y2, double z2) {
        double[] a = toWorld(dir, roll, x1 - 8, y1 - 8, z1 - 8);
        double[] b = toWorld(dir, roll, x2 - 8, y2 - 8, z2 - 8);
        return Block.box(Math.min(a[0], b[0]) + 8, Math.min(a[1], b[1]) + 8, Math.min(a[2], b[2]) + 8,
                Math.max(a[0], b[0]) + 8, Math.max(a[1], b[1]) + 8, Math.max(a[2], b[2]) + 8);
    }

    // The world side that the model's side (dx, 0, 0) ends up on.
    private static Direction side(Direction facing, int roll, int dx) {
        double[] v = toWorld(facing, roll, dx, 0, 0);
        return Direction.getNearest(v[0], v[1], v[2]);
    }

    public EndPlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH).setValue(ROLL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACING, ROLL);
    }

    // Placed against the end of a stack (or anything), it faces away from what was clicked.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    // A Wrench rolls the plate a quarter turn around FACING, so the ports point another way.
    @Override
    public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
        return state.cycle(ROLL);
    }

    // The ports moved, so pipes have to look up the sided fluid handlers again.
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (oldState.is(this) && oldState.getValue(ROLL) != state.getValue(ROLL))
            level.invalidateCapabilities(pos);
    }

    // Turned or mirrored with a structure: keep the ports on the same world sides they turn to.
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return withGasSide(state, rotation.rotate(state.getValue(FACING)), rotation.rotate(gasSide(state)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return withGasSide(state, mirror.mirror(state.getValue(FACING)), mirror.mirror(gasSide(state)));
    }

    private static BlockState withGasSide(BlockState state, Direction facing, Direction gas) {
        for (int roll = 0; roll < 4; roll++)
            if (side(facing, roll, 1) == gas)
                return state.setValue(FACING, facing).setValue(ROLL, roll);
        return state.setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue() * 4 + state.getValue(ROLL)];
    }

    /** The face the gas port opens onto (the model's east). */
    public static Direction gasSide(BlockState state) {
        return side(state.getValue(FACING), state.getValue(ROLL), 1);
    }

    /** The face the Water port opens onto (the model's west). */
    public static Direction waterSide(BlockState state) {
        return side(state.getValue(FACING), state.getValue(ROLL), -1);
    }

    // Buckets: Hydrogen or Oxygen in, an empty one takes Water out.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EndPlateBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return MachineInteractions.useItem(level, player, hand, stack, be, be.fluidHandler(null), null, 0);
    }

    @Override
    public Class<EndPlateBlockEntity> getBlockEntityClass() {
        return EndPlateBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EndPlateBlockEntity> getBlockEntityType() {
        return ModBlockEntities.END_PLATE.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.END_PLATE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<EndPlateBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
