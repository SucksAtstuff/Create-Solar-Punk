package net.succ.solar_punk.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.offbyonelib.registry.CreativeTabContents;


public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SolarPunk.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SOLAR_PUNK_TAB =
            CREATIVE_MODE_TABS.register("solar_punk_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solar_punk"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModBlocks.ANDESITE_SOLAR_PANEL.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            // Everything the mod registers except the building blocks, so new
                            // blocks and items show up on their own: blocks in ModBlocks order,
                            // then items in ModItems order, then fluid buckets.
                            CreativeTabContents.addAll(output, ModBlocks.BLOCKS, ModItems.ITEMS, ModBlocks.decorativeBlocks()))
                    .build());

    // Stairs, slabs, walls and other decorative blocks (ModBlocks.registerDecorative), in
    // ModBlocks order.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING_BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("building_blocks_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.solar_punk.building_blocks"))
                    .withTabsBefore(SOLAR_PUNK_TAB.getKey())
                    .icon(() -> ModBlocks.CHISELED_SALT_BRICKS.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            CreativeTabContents.addBlocks(output, ModBlocks.decorativeBlocks()))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
