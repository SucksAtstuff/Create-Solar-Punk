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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.ElectrolyzerBlockEntity;
import net.succ.solar_punk.util.MachineInteractions;

import java.util.EnumMap;
import java.util.Map;

/**
 * Splits Water into Hydrogen and Oxygen with FE, built like a Hofmann voltameter: Water goes
 * into the bulb on top, and each gas collects in one of the two glass tubes and leaves
 * through the copper flange beside it (Hydrogen on the model's east side, Oxygen on the
 * west). See ElectrolyzerBlockEntity.
 */
public class ElectrolyzerBlock extends Block implements IBE<ElectrolyzerBlockEntity>, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        // As modelled facing north: base plate, the two tubes, centre post, bulb, the three
        // flanges and the terminal box on the back.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            SHAPES.put(dir, Shapes.or(
                    box(dir, 1, 0, 1, 15, 1, 15),
                    box(dir, 2, 1, 4, 6, 10, 12),
                    box(dir, 10, 1, 4, 14, 10, 12),
                    box(dir, 7, 4, 7, 9, 11, 9),
                    box(dir, 5, 11, 5, 11, 15, 11),
                    box(dir, 3, 15, 3, 13, 16, 13),
                    box(dir, 0, 3, 3, 2, 13, 13),
                    box(dir, 14, 3, 3, 16, 13, 13),
                    box(dir, 5, 6, 14, 11, 10, 16)));
        }
    }

    // A box in model pixels (facing north), turned to face dir.
    private static VoxelShape box(Direction dir, double x1, double y1, double z1, double x2, double y2, double z2) {
        return switch (dir) {
            case SOUTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case EAST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case WEST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    public ElectrolyzerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** The face the Hydrogen flange is on. */
    public static Direction hydrogenSide(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }

    /** The face the Oxygen flange is on. */
    public static Direction oxygenSide(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    // Buckets: an empty one takes Hydrogen first, then Oxygen; a Water Bucket fills it.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ElectrolyzerBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return MachineInteractions.useItem(level, player, hand, stack, be, be.allFluids, null, 0);
    }

    @Override
    public Class<ElectrolyzerBlockEntity> getBlockEntityClass() {
        return ElectrolyzerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectrolyzerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ELECTROLYZER.get();
    }

    // Both sides: the server runs it, the client eases the drawn fluid levels.
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ELECTROLYZER.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ElectrolyzerBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
