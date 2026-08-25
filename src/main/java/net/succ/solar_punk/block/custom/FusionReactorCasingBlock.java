package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FusionReactorCasingBlockEntity;

// Containment shell for the Fusion Reactor - Netherite Ingot applied to a Copper Block,
// one tier past the Steam Turbine's industrial iron casing. No plumbing of its own
// (unlike TurbineCasingBlock, this doesn't proxy a fluid handler to anything - the
// reactor's steam output is its own tank, not piped through the shell walls); the
// block entity exists purely to proxy the goggle tooltip through to the buried Core -
// see FusionReactorCasingBlockEntity.
public class FusionReactorCasingBlock extends Block implements IWrenchable, EntityBlock {
    public FusionReactorCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionReactorCasingBlockEntity(ModBlockEntities.FUSION_REACTOR_CASING.get(), pos, state);
    }
}
