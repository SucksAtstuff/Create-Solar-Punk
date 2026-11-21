package net.succ.solar_punk.item;

import net.succ.solar_punk.block.custom.RailingShape;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.Block;
import studio.offbyone.lib.shape.ShapedBlockItem;

import java.util.function.Supplier;

// The one item for every shape of a filigree railing material: flat, slope and steep slope.
// It places whichever shape is stored on the stack (picked in the radial shape menu), and is
// named after that shape. The addon registers the RailingShape data component.
public class FiligreeRailingItem extends ShapedBlockItem<RailingShape> {
    private final Supplier<? extends Block> slope;
    private final Supplier<? extends Block> steepSlope;

    public FiligreeRailingItem(Block flat, Supplier<? extends Block> slope, Supplier<? extends Block> steepSlope,
                               Supplier<? extends DataComponentType<RailingShape>> shapeComponent, Properties properties) {
        super(flat, shapeComponent, RailingShape.values(), properties);
        this.slope = slope;
        this.steepSlope = steepSlope;
    }

    @Override
    public Block blockFor(RailingShape shape) {
        return switch (shape) {
            case FLAT -> getBlock();
            case SLOPE -> slope.get();
            case STEEP_SLOPE -> steepSlope.get();
        };
    }
}
