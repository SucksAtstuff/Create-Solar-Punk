package net.succ.solar_punk.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluids;

// The 45 degree railing slope: always a FULL piece, climbing a whole block per block. Placed by
// hand it climbs away from the player, or up the stairs when placed on top of them.
public class FiligreeRailingSteepSlopeBlock extends FiligreeRailingSlopeBlock {
    public static final MapCodec<FiligreeRailingSteepSlopeBlock> CODEC = simpleCodec(FiligreeRailingSteepSlopeBlock::new);

    public FiligreeRailingSteepSlopeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PART, Part.FULL));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(WATERLOGGED,
                context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        if (below.getBlock() instanceof StairBlock && below.getValue(StairBlock.HALF) == Half.BOTTOM)
            return state.setValue(FACING, below.getValue(StairBlock.FACING));
        return state.setValue(FACING, context.getHorizontalDirection());
    }
}
