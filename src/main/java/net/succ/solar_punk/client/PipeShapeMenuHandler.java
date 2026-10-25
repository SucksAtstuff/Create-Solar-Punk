package net.succ.solar_punk.client;

import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.toolbox.RadialToolboxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;
import org.lwjgl.glfw.GLFW;

// Opens PipeShapeMenu on Create's toolbelt key (Alt by default) while holding a pipe.
// Create opens its toolbox menu on the same key when a toolbox is nearby, so while a
// pipe is held that menu is swapped for this one.
@EventBusSubscriber(modid = SolarPunk.MODID, value = Dist.CLIENT)
public class PipeShapeMenuHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS || !AllKeys.TOOLBELT.doesModifierAndCodeMatch(event.getKey()))
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        ItemStack held = heldPipe(mc.player);
        if (!held.isEmpty())
            mc.setScreen(new PipeShapeMenu(ParticleAcceleratorPipeItem.getShape(held)));
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof RadialToolboxMenu))
            return;
        Minecraft mc = Minecraft.getInstance();
        ItemStack held = heldPipe(mc.player);
        if (held.isEmpty())
            return;
        if (event.getCurrentScreen() instanceof PipeShapeMenu)
            event.setCanceled(true);
        else
            event.setNewScreen(new PipeShapeMenu(ParticleAcceleratorPipeItem.getShape(held)));
    }

    private static ItemStack heldPipe(Player player) {
        if (player == null) return ItemStack.EMPTY;
        ItemStack held = player.getMainHandItem();
        return held.getItem() instanceof ParticleAcceleratorPipeItem ? held : ItemStack.EMPTY;
    }
}
