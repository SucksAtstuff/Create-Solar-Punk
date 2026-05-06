package net.succ.create_solar_powered.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.block.ModBlocks;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Create_solar_powered.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_SOLAR_POWERED_TAB =
            CREATIVE_MODE_TABS.register("create_solar_powered_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_solar_powered"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModBlocks.SOLAR_PANEL.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.SOLAR_PANEL.get());
                        output.accept(ModBlocks.SOLAR_HEATER.get());
                        output.accept(ModBlocks.HEAT_PIPE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
