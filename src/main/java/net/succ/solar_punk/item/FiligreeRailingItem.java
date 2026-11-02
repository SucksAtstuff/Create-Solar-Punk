package net.succ.solar_punk.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.RailingShape;

import java.util.Map;
import java.util.function.Supplier;

// The one item for every shape of a filigree railing material: flat, slope and steep slope.
// It places whichever shape is stored on the stack (picked in RadialShapeMenu), and is named
// after that shape. Works like ParticleAcceleratorPipeItem.
public class FiligreeRailingItem extends BlockItem {
    private final Supplier<? extends Block> slope;
    private final Supplier<? extends Block> steepSlope;

    public FiligreeRailingItem(Block flat, Supplier<? extends Block> slope, Supplier<? extends Block> steepSlope,
                               Properties properties) {
        super(flat, properties);
        this.slope = slope;
        this.steepSlope = steepSlope;
    }

    public Block blockFor(RailingShape shape) {
        return switch (shape) {
            case FLAT -> getBlock();
            case SLOPE -> slope.get();
            case STEEP_SLOPE -> steepSlope.get();
        };
    }

    public Block blockFor(ItemStack stack) {
        return blockFor(getShape(stack));
    }

    public static RailingShape getShape(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.RAILING_SHAPE.get(), RailingShape.FLAT);
    }

    /** Sets the shape. Flat clears it, so those stacks still stack with fresh railings. */
    public static void setShape(ItemStack stack, RailingShape shape) {
        if (shape == RailingShape.FLAT) stack.remove(ModDataComponents.RAILING_SHAPE.get());
        else stack.set(ModDataComponents.RAILING_SHAPE.get(), shape);
    }

    public static ItemStack stackOf(Item item, RailingShape shape) {
        ItemStack stack = new ItemStack(item);
        setShape(stack, shape);
        return stack;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = blockFor(context.getItemInHand()).getStateForPlacement(context);
        return state != null && canPlace(context, state) ? state : null;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return blockFor(stack).getDescriptionId();
    }

    // The slopes have no item of their own, so they map to this one: they drop it when broken
    // and pick-block gives it.
    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        blockToItemMap.put(slope.get(), item);
        blockToItemMap.put(steepSlope.get(), item);
    }
}
