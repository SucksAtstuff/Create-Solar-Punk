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
import net.succ.create_solar_powered.fluid.ModFluids;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Create_solar_powered.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_SOLAR_POWERED_TAB =
            CREATIVE_MODE_TABS.register("create_solar_powered_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_solar_powered"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModBlocks.ANDESITE_SOLAR_PANEL.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.ANDESITE_SOLAR_PANEL.get());
                        output.accept(ModBlocks.BRASS_SOLAR_PANEL.get());
                        output.accept(ModBlocks.SOLAR_HEATER.get());
                        output.accept(ModBlocks.SALT_BLOCK.get());
                        output.accept(ModFluids.MOLTEN_SALT_BUCKET.get());
                        output.accept(ModBlocks.KINETIC_BATTERY.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
