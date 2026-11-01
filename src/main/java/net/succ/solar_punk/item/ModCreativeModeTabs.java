package net.succ.solar_punk.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SolarPunk.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SOLAR_PUNK_TAB =
            CREATIVE_MODE_TABS.register("solar_punk_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solar_punk"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModBlocks.ANDESITE_SOLAR_PANEL.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Everything the mod registers except the building blocks, so new blocks
                        // and items show up on their own: blocks in ModBlocks order, then items
                        // in ModItems order, then fluid buckets. Blocks without an item (fluids,
                        // the extra pipe shapes) are skipped, and so are sequenced assembly's
                        // in-between items.
                        Set<Block> decorative = ModBlocks.decorativeBlocks().stream()
                                .map(DeferredBlock::get).collect(Collectors.toSet());
                        for (DeferredHolder<Block, ? extends Block> block : ModBlocks.BLOCKS.getEntries()) {
                            Item item = block.get().asItem();
                            if (item != Items.AIR && !decorative.contains(block.get())) output.accept(item);
                        }
                        List<Item> items = ModItems.ITEMS.getEntries().stream()
                                .<Item>map(DeferredHolder::get)
                                .filter(item -> !(item instanceof BlockItem) && !(item instanceof SequencedAssemblyItem))
                                .toList();
                        items.stream().filter(item -> !(item instanceof BucketItem)).forEach(output::accept);
                        items.stream().filter(item -> item instanceof BucketItem).forEach(output::accept);
                    })
                    .build());

    // Stairs, slabs, walls and other decorative blocks (ModBlocks.registerDecorative), in
    // ModBlocks order.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING_BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("building_blocks_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solar_punk.building_blocks"))
                    .withTabsBefore(SOLAR_PUNK_TAB.getKey())
                    .icon(() -> ModBlocks.CHISELED_SALT_BRICKS.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ModBlocks.decorativeBlocks().forEach(block -> output.accept(block.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
