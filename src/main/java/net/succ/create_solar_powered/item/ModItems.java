package net.succ.create_solar_powered.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.create_solar_powered.Create_solar_powered;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Create_solar_powered.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}