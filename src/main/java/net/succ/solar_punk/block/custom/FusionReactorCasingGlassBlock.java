package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// See-through variant of FusionReactorCasingBlock - swap in for observation windows on
// the containment shell, same as TurbineCasingGlassBlock is to TurbineCasingBlock.
public class FusionReactorCasingGlassBlock extends TransparentBlock implements IWrenchable {
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
}
