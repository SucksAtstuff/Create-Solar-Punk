package net.succ.solar_punk.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.succ.solar_punk.block.custom.PipeShape;
import studio.offbyone.lib.shape.ShapedBlockItem;

// The one item for every Particle Accelerator Pipe shape. It places whichever shape is
// stored on the stack (picked in the radial shape menu), and is named after that shape.
public class ParticleAcceleratorPipeItem extends ShapedBlockItem<PipeShape> {

    public ParticleAcceleratorPipeItem(Block block, Properties properties) {
        super(block, ModDataComponents.PIPE_SHAPE, PipeShape.values(), properties);
    }

    @Override
    public Block blockFor(PipeShape shape) {
        return shape.block();
    }

    // Static versions for code that only has a stack

    public static PipeShape shapeOf(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.PIPE_SHAPE.get(), PipeShape.STRAIGHT);
    }

    public static ItemStack stackOf(Item item, PipeShape shape) {
        return ((ParticleAcceleratorPipeItem) item).stackOf(shape);
    }
}
