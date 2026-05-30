package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.ModBlocks;

public class DeadGrassBlock extends BushBlock {
    public static final MapCodec<DeadGrassBlock> CODEC = simpleCodec(DeadGrassBlock::new);

    public DeadGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
            || state.is(ModBlocks.DEAD_GRASS_BLOCK.get())
            || state.is(ModBlocks.RUINED_DIRT.get())
            || state.is(ModBlocks.ASH_BLOCK.get());
    }
}