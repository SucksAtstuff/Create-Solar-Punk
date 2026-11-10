package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.WireSwitchBlockEntity;
import net.succ.solar_punk.item.SpoolItem;
import net.succ.solar_punk.wire.WireNetwork;
import net.succ.solar_punk.wire.WireNodeBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A knife switch with two wire posts. Wires on the same post are always joined; closing the
 * switch joins the two posts. Placed on a floor, wall or ceiling like a Lever, and flipped by
 * hand. The Redstone Wire Relay is the same block driven by redstone instead.
 */
public class WireSwitchBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock, WireNodeBlock, IWrenchable {
    public static final MapCodec<WireSwitchBlock> CODEC = simpleCodec(WireSwitchBlock::new);
    public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

    // Where wires tie onto each post, in model pixels (the model as drawn: on a floor, facing north).
    private static final Vec3[] TERMINALS = {new Vec3(12, 4.5, 5), new Vec3(12, 4.5, 11)};

    private final Map<BlockState, VoxelShape> shapes;

    public WireSwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)
                .setValue(CLOSED, true));
        shapes = getShapeForEachState(state -> {
            Vec3 a = rotate(new Vec3(3, 0, 3), state), b = rotate(new Vec3(13, 6, 13), state);
            return Shapes.box(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z),
                    Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
        });
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, CLOSED);
    }

    // Blockstate rotations, the same as a vanilla Lever's.
    public static int xRot(AttachFace face) {
        return switch (face) {
            case FLOOR -> 0;
            case WALL -> 90;
            case CEILING -> 180;
        };
    }

    public static int yRot(AttachFace face, Direction facing) {
        int y = ((int) facing.toYRot() + 180) % 360;
        return face == AttachFace.CEILING ? (y + 180) % 360 : y;
    }

    /** A point in model pixels, turned the way the blockstate turns the model, in block units (0 to 1). */
    public static Vec3 rotate(Vec3 pixels, BlockState state) {
        double x = pixels.x / 16 - 0.5, y = pixels.y / 16 - 0.5, z = pixels.z / 16 - 0.5;
        AttachFace face = state.getValue(FACE);
        for (int i = 0; i < xRot(face) / 90; i++) {
            double ny = z, nz = -y;
            y = ny;
            z = nz;
        }
        for (int i = 0; i < yRot(face, state.getValue(FACING)) / 90; i++) {
            double nx = -z, nz = x;
            x = nx;
            z = nz;
        }
        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
    }

    @Override
    public Vec3 attachPoint(BlockState state, BlockPos pos, int terminal) {
        return rotate(TERMINALS[terminal == 1 ? 1 : 0], state).add(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int terminalAt(BlockState state, BlockPos pos, Vec3 hit) {
        return hit.distanceToSqr(attachPoint(state, pos, 1)) < hit.distanceToSqr(attachPoint(state, pos, 0)) ? 1 : 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) setClosed(level, pos, state, !state.getValue(CLOSED), player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected void setClosed(Level level, BlockPos pos, BlockState state, boolean closed, @Nullable Player player) {
        if (state.getValue(CLOSED) != closed) {
            level.setBlock(pos, state.setValue(CLOSED, closed), 3);
            WireNetwork.changed();
        }
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, clickVolume(), closed ? 0.6f : 0.5f);
        level.gameEvent(player, closed ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    protected float clickVolume() {
        return 0.3f;
    }

    // Shears cut every wire; a Spool goes to the item instead of flipping the switch.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof SpoolItem) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (!stack.is(Tags.Items.TOOLS_SHEAR)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof WireSwitchBlockEntity be) || be.connections().isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            be.cutAll();
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.2f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // A wrench only picks it up (sneaking); it doesn't rotate.
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WireSwitchBlockEntity be)
            be.cutAll();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(CLOSED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WireSwitchBlockEntity(ModBlockEntities.WIRE_SWITCH.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.WIRE_SWITCH.get()) return null;
        return (l, p, s, be) -> ((WireSwitchBlockEntity) be).tick();
    }
}
