package net.succ.solar_punk.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

// What hoppers, funnels and pipes see of a machine's inventory: which slots they may put
// items into and which they may take items out of. Without it they'd pull fuel or
// half-melted input straight back out along with the products.
public class AutomationItemHandler implements IItemHandler {

    private final IItemHandler inventory;
    private final IntPredicate canInsert;
    private final BiPredicate<Integer, ItemStack> canExtract;

    public AutomationItemHandler(IItemHandler inventory, IntPredicate canInsert, BiPredicate<Integer, ItemStack> canExtract) {
        this.inventory = inventory;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    /** Inserts only into {@code inputSlot} and extracts only from {@code outputSlot}. */
    public static AutomationItemHandler inputOutput(IItemHandler inventory, int inputSlot, int outputSlot) {
        return new AutomationItemHandler(inventory, slot -> slot == inputSlot, (slot, stack) -> slot == outputSlot);
    }

    @Override public int getSlots() { return inventory.getSlots(); }
    @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
    @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return canInsert.test(slot) && inventory.isItemValid(slot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return canInsert.test(slot) ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return canExtract.test(slot, inventory.getStackInSlot(slot))
                ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }
}
