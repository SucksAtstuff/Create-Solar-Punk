package net.succ.solar_punk.block.custom;

import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.succ.solar_punk.block.entity.custom.BioreactorBlockEntity;
import net.succ.solar_punk.util.MachineInteractions;

/**
 * The aerator layer under a Bioreactor. It has no block entity: everything goes to the
 * controller of the glass tank above ({@link BioreactorBlockEntity#above}). Water, nutrients
 * and Kelp go in, Biomass and Oxygen come out.
 */
public class BioreactorBaseBlock extends Block implements IProxyHoveringInformation {

    public BioreactorBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        BioreactorBlockEntity ctrl = BioreactorBlockEntity.above(level, pos);
        if (ctrl == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return ctrl.useItem(level, player, hand, stack, ctrl.baseFluidHandler);
    }

    // An empty hand takes the Biomass out.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BioreactorBlockEntity ctrl = BioreactorBlockEntity.above(level, pos);
        if (ctrl == null) return InteractionResult.PASS;
        return MachineInteractions.takeOut(level, player, ctrl, ctrl.inventory, BioreactorBlockEntity.OUTPUT_SLOT);
    }

    // How full the Biomass slot is.
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BioreactorBlockEntity ctrl = BioreactorBlockEntity.above(level, pos);
        return ctrl != null ? ctrl.biomassSignal() : 0;
    }

    // Goggles on a Base show the tank's info.
    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        BioreactorBlockEntity ctrl = BioreactorBlockEntity.above(level, pos);
        return ctrl != null ? ctrl.getBlockPos() : pos;
    }
}
