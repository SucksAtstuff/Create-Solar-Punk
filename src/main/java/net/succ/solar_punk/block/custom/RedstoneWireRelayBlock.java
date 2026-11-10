package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A Wire Switch thrown by redstone: closed while powered, or open while powered once a Wrench
 * inverts it. It follows the signal at most once every {@link #DELAY} ticks, since every flip
 * makes every connector in the world search its network again.
 */
public class RedstoneWireRelayBlock extends WireSwitchBlock {
    public static final MapCodec<RedstoneWireRelayBlock> CODEC = simpleCodec(RedstoneWireRelayBlock::new);
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");
    public static final int DELAY = 4;

    public RedstoneWireRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(INVERTED, false).setValue(CLOSED, false));
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVERTED);
    }

    private static boolean wantsClosed(Level level, BlockPos pos, BlockState state) {
        return level.hasNeighborSignal(pos) != state.getValue(INVERTED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(CLOSED, wantsClosed(context.getLevel(), context.getClickedPos(), state));
    }

    // Not flipped by hand.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || wantsClosed(level, pos, state) == state.getValue(CLOSED)) return;
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, DELAY);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean closed = wantsClosed(level, pos, state);
        if (closed != state.getValue(CLOSED)) setClosed(level, pos, state, closed, null);
    }

    @Override
    protected float clickVolume() {
        return 0.15f;
    }

    // A Wrench swaps closed-while-powered and open-while-powered.
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide) {
            BlockState inverted = state.cycle(INVERTED);
            level.setBlock(pos, inverted, 3);
            setClosed(level, pos, inverted, wantsClosed(level, pos, inverted), context.getPlayer());
            IWrenchable.playRotateSound(level, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
