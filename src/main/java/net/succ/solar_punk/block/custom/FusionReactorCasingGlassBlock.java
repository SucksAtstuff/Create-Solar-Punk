package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;

// See-through variant of FusionReactorCasingBlock - swap in for observation windows on
// the containment shell, same as TurbineCasingGlassBlock is to TurbineCasingBlock.
// Shares FusionReactorCasingBlockEntity with the opaque variant (same goggle-tooltip
// proxy, same shared registration TurbineCasingGlassBlock uses for TURBINE_CASING).
public class FusionReactorCasingGlassBlock extends TransparentBlock implements IWrenchable, EntityBlock {
    public FusionReactorCasingGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.getBlock() instanceof FusionReactorCasingGlassBlock
            || adjacent.getBlock() instanceof FusionReactorCasingBlock
            || super.skipRendering(state, adjacent, side);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionReactorCasingBlockEntity(ModBlockEntities.FUSION_REACTOR_CASING.get(), pos, state);
    }
}
