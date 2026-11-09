package net.succ.solar_punk.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MangroveLeavesBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.NetherrackBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.RootedDirtBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Growth boosts shared by the Greenhouse Controller and the Grow Lamp. */
public final class PlantGrowth {

    public static boolean isPlant(BlockState state) {
        var block = state.getBlock();
        // Bonemealable ground and leaves (grass spreads flowers, moss spreads moss) aren't plants
        if (block instanceof GrassBlock || block instanceof NyliumBlock || block instanceof NetherrackBlock || block instanceof MossBlock
                || block instanceof RootedDirtBlock || block instanceof MangroveLeavesBlock) return false;
        return block instanceof BonemealableBlock || block instanceof BushBlock || block instanceof SugarCaneBlock
                || block instanceof CactusBlock || block instanceof BambooStalkBlock;
    }

    /** A plant that still random ticks, so a boost does something. */
    public static boolean isGrowing(BlockState state) {
        return state.isRandomlyTicking() && isPlant(state);
    }

    // Extra random ticks on top of vanilla's, run once a second: a plant gets
    // randomTickSpeed / 4096 random ticks per game tick on average, so over one second the
    // boost adds that times 20 times the bonus (1.0 = twice as fast).
    public static void boost(ServerLevel level, Iterable<BlockPos> plants, double bonus) {
        int tickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (tickSpeed <= 0 || bonus <= 0) return;
        double chance = bonus * 20.0 * tickSpeed / 4096.0;
        for (BlockPos cell : plants) {
            BlockState state = level.getBlockState(cell);
            if (!isGrowing(state)) continue;
            double rolls = chance;
            while (rolls > 0) {
                if (rolls >= 1 || level.random.nextDouble() < rolls) {
                    state.randomTick(level, cell, level.random);
                    state = level.getBlockState(cell);
                    if (!isGrowing(state)) break;
                }
                rolls -= 1;
            }
        }
    }

    private PlantGrowth() {}
}
