package net.succ.solar_punk.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A block wires can be strung to: the Wire Connector (one terminal) or a Wire Switch / Redstone
 * Wire Relay (two terminals, one per post).
 */
public interface WireNodeBlock {

    /** Where wires meet the given terminal, in world coordinates. */
    Vec3 attachPoint(BlockState state, BlockPos pos, int terminal);

    /** The terminal a click at {@code hit} (world coordinates) picks. */
    default int terminalAt(BlockState state, BlockPos pos, Vec3 hit) {
        return 0;
    }

    /** Attach point of whatever is at {@code pos}, or the block's centre if it isn't a wire node. */
    static Vec3 attachPoint(BlockGetter level, BlockPos pos, int terminal) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof WireNodeBlock node
                ? node.attachPoint(state, pos, terminal) : Vec3.atCenterOf(pos);
    }
}
