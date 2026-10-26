package net.succ.solar_punk.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.custom.PipeShape;

import java.util.Map;

// The one item for every Particle Accelerator Pipe shape. It places whichever shape is
// stored on the stack (picked in PipeShapeMenu), and is named after that shape.
public class ParticleAcceleratorPipeItem extends BlockItem {

    public ParticleAcceleratorPipeItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static PipeShape getShape(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.PIPE_SHAPE.get(), PipeShape.STRAIGHT);
    }

    /** Sets the shape. Straight clears it, so those stacks still stack with fresh pipes. */
    public static void setShape(ItemStack stack, PipeShape shape) {
        if (shape == PipeShape.STRAIGHT) stack.remove(ModDataComponents.PIPE_SHAPE.get());
        else stack.set(ModDataComponents.PIPE_SHAPE.get(), shape);
    }

    public static ItemStack stackOf(Item item, PipeShape shape) {
        ItemStack stack = new ItemStack(item);
        setShape(stack, shape);
        return stack;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = getShape(context.getItemInHand()).block().getStateForPlacement(context);
        return state != null && canPlace(context, state) ? state : null;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getShape(stack).block().getDescriptionId();
    }

    // The other shapes have no item of their own, so they map to this one: they drop it
    // when broken and pick-block gives it.
    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        for (PipeShape shape : PipeShape.values())
            blockToItemMap.put(shape.block(), item);
    }
}
