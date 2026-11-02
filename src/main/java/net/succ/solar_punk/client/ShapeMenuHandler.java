package net.succ.solar_punk.client;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.PipeShape;
import net.succ.solar_punk.block.custom.RailingShape;
import net.succ.solar_punk.item.FiligreeRailingItem;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;
import net.succ.solar_punk.network.SelectPipeShapePayload;
import net.succ.solar_punk.network.SelectRailingShapePayload;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

// Opens a RadialShapeMenu on Create's toolbelt key (Alt by default) while holding an item with
// shapes: a Particle Accelerator Pipe or a filigree railing. Create opens its toolbox menu on
// the same key when a toolbox is nearby, so while one of these is held that menu is swapped
// for ours.
@EventBusSubscriber(modid = SolarPunk.MODID, value = Dist.CLIENT)
public class ShapeMenuHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS || !AllKeys.TOOLBELT.doesModifierAndCodeMatch(event.getKey()))
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        RadialShapeMenu menu = menuFor(mc.player);
        if (menu != null)
            mc.setScreen(menu);
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof RadialToolboxMenu))
            return;
        RadialShapeMenu menu = menuFor(Minecraft.getInstance().player);
        if (menu == null)
            return;
        if (event.getCurrentScreen() instanceof RadialShapeMenu)
            event.setCanceled(true);
        else
            event.setNewScreen(menu);
    }

    // The menu for the item in the main hand, or null if it has no shapes. Picking a shape sets
    // it on the held stack straight away and tells the server.
    private static RadialShapeMenu menuFor(Player player) {
        if (player == null) return null;
        ItemStack held = player.getMainHandItem();
        Item item = held.getItem();
        if (item instanceof ParticleAcceleratorPipeItem) {
            PipeShape[] shapes = PipeShape.values();
            return new RadialShapeMenu(
                    Arrays.stream(shapes).map(shape -> ParticleAcceleratorPipeItem.stackOf(item, shape)).toList(),
                    ParticleAcceleratorPipeItem.getShape(held).ordinal(),
                    stack -> stack.getItem() instanceof ParticleAcceleratorPipeItem,
                    slot -> {
                        ParticleAcceleratorPipeItem.setShape(player.getMainHandItem(), shapes[slot]);
                        PacketDistributor.sendToServer(new SelectPipeShapePayload(shapes[slot]));
                    });
        }
        if (item instanceof FiligreeRailingItem) {
            RailingShape[] shapes = RailingShape.values();
            return new RadialShapeMenu(
                    Arrays.stream(shapes).map(shape -> FiligreeRailingItem.stackOf(item, shape)).toList(),
                    FiligreeRailingItem.getShape(held).ordinal(),
                    stack -> stack.getItem() == item,
                    slot -> {
                        FiligreeRailingItem.setShape(player.getMainHandItem(), shapes[slot]);
                        PacketDistributor.sendToServer(new SelectRailingShapePayload(shapes[slot]));
                    });
        }
        return null;
    }
}
