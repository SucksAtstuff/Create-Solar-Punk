package net.succ.solar_punk.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

// Right-click handling shared by the machine blocks: buckets fill or empty the tanks,
// held items go into the input slot, and an empty hand takes items back out.
public final class MachineInteractions {

    private MachineInteractions() {}

    /**
     * A bucket (or other fluid container) fills or empties {@code fluids}; anything else
     * goes into {@code inventory}'s {@code slot} if it fits. Either may be null.
     */
    public static ItemInteractionResult useItem(Level level, Player player, InteractionHand hand, ItemStack stack,
                                                BlockEntity be, @Nullable IFluidHandler fluids,
                                                @Nullable IItemHandler inventory, int slot) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (fluids != null && FluidUtil.interactWithFluidHandler(player, hand, fluids))
            return ItemInteractionResult.SUCCESS;
        if (inventory != null) {
            ItemStack remaining = inventory.insertItem(slot, stack, true);
            if (remaining.getCount() < stack.getCount()) {
                remaining = inventory.insertItem(slot, stack, false);
                if (!player.isCreative()) player.setItemInHand(hand, remaining);
                be.setChanged();
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Gives the player the whole stack from the first of {@code slots} that holds
     * something. Whatever doesn't fit in their inventory drops at their feet.
     */
    public static InteractionResult takeOut(Level level, Player player, BlockEntity be, IItemHandler inventory, int... slots) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        for (int slot : slots) {
            ItemStack extracted = inventory.extractItem(slot, inventory.getSlotLimit(slot), false);
            if (extracted.isEmpty()) continue;
            ItemHandlerHelper.giveItemToPlayer(player, extracted);
            be.setChanged();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
