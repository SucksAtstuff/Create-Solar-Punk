package net.succ.solar_punk.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

// Radial menu for picking which shape a held item places (Particle Accelerator Pipes, filigree
// railings), styled and controlled like Create's toolbox menu: hold the toolbelt key (Alt),
// point at a shape, release. Scrolling, clicking and the hotbar number keys work too. The
// current shape sits in the middle. Opened by ShapeMenuHandler.
public class RadialShapeMenu extends AbstractSimiScreen {

    private final List<ItemStack> options;
    private final Predicate<ItemStack> stillHeld;
    private final IntConsumer onPick;
    private final int current;
    private int ticksOpen;
    private int hoveredSlot = -1;
    private boolean scrollMode;
    private int scrollSlot;
    private boolean cancelled;

    // options: one stack per shape, in slot order (slot 0 at the top). stillHeld closes the menu
    // if the item leaves the main hand. onPick gets the chosen slot when it isn't the current one.
    public RadialShapeMenu(List<ItemStack> options, int current, Predicate<ItemStack> stillHeld, IntConsumer onPick) {
        this.options = options;
        this.current = current;
        this.stillHeld = stillHeld;
        this.onPick = onPick;
        this.scrollSlot = current;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

        Window window = getMinecraft().getWindow();
        float hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        float hoveredY = mouseY - window.getGuiScaledHeight() / 2f;
        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        hoveredSlot = -1;
        float slotAngle = 360f / options.size();
        if (distance > 25 && distance < 10000)
            // equal slices, slot 0 at the top
            hoveredSlot = Mth.floor(((AngleHelper.deg(Mth.atan2(hoveredY, hoveredX)) + 360 + 90 + slotAngle / 2) % 360) / slotAngle);
        if (scrollMode && distance > 150)
            scrollMode = false;
        int selected = selected();

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(width / 2f, height / 2f, 0);

        // the shape the item is set to now, in the middle
        AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
        GuiGameElement.of(options.get(current)).at(-9, -9).render(graphics);

        Component tip = null;
        for (int slot = 0; slot < options.size(); slot++) {
            ms.pushPose();
            TransformStack.of(ms)
                    .rotateZDegrees(slot * slotAngle)
                    .translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
                    .rotateZDegrees(-slot * slotAngle);
            ms.translate(-12, -12, 0);

            AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
            ItemStack stack = options.get(slot);
            GuiGameElement.of(stack).at(3, 3).render(graphics);
            if (slot == selected) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                tip = stack.getHoverName();
            }
            ms.popPose();
        }
        ms.popPose();

        if (tip != null) {
            int alpha = Mth.clamp((int) (fade * 255), 0, 255);
            if (alpha > 8) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.drawString(font, tip, Math.round((width - font.width(tip)) / 2f), height - 72,
                        0xFFFFFF | (alpha << 24), false);
                RenderSystem.disableBlend();
            }
        }
    }

    private int selected() {
        return scrollMode ? scrollSlot : hoveredSlot;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Color color = BACKGROUND_COLOR.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));
        graphics.fillGradient(0, 0, width, height, color.getRGB(), color.getRGB());
    }

    @Override
    public void tick() {
        ticksOpen++;
        // the item left the main hand (e.g. dropped), so there's nothing to set
        if (!stillHeld.test(minecraft.player.getMainHandItem())) {
            cancelled = true;
            onClose();
        }
        super.tick();
    }

    @Override
    public void removed() {
        super.removed();
        int selected = selected();
        if (cancelled || selected < 0 || selected == current)
            return;
        onPick.accept(selected);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollMode = true;
        scrollSlot = Math.floorMod(scrollSlot - (int) Math.signum(scrollY), options.size());
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && selected() >= 0) {
            onClose();
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean keyPressed(int code, int scanCode, int modifiers) {
        KeyMapping[] hotbarBinds = minecraft.options.keyHotbarSlots;
        for (int i = 0; i < options.size() && i < hotbarBinds.length; i++) {
            if (hotbarBinds[i].matches(code, scanCode)) {
                scrollMode = true;
                scrollSlot = i;
                onClose();
                return true;
            }
        }
        return super.keyPressed(code, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int code, int scanCode, int modifiers) {
        if (AllKeys.TOOLBELT.getKeybind().isActiveAndMatches(InputConstants.getKey(code, scanCode))) {
            onClose();
            return true;
        }
        return super.keyReleased(code, scanCode, modifiers);
    }
}
