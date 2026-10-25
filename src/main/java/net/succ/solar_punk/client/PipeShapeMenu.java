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
import net.neoforged.neoforge.network.PacketDistributor;
import net.succ.solar_punk.block.custom.PipeShape;
import net.succ.solar_punk.item.ParticleAcceleratorPipeItem;
import net.succ.solar_punk.network.SelectPipeShapePayload;

// Radial menu for picking a Particle Accelerator Pipe's shape, styled and controlled
// like Create's toolbox menu: hold the toolbelt key (Alt), point at a shape, release.
// Scrolling, clicking and the hotbar number keys work too. The current shape sits in
// the middle.
public class PipeShapeMenu extends AbstractSimiScreen {

    private static final PipeShape[] SHAPES = PipeShape.values();

    private final PipeShape current;
    private int ticksOpen;
    private int hoveredSlot = -1;
    private boolean scrollMode;
    private int scrollSlot;
    private boolean cancelled;

    public PipeShapeMenu(PipeShape current) {
        this.current = current;
        this.scrollSlot = current.ordinal();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

        Window window = getMinecraft().getWindow();
        float hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        float hoveredY = mouseY - window.getGuiScaledHeight() / 2f;
        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        hoveredSlot = -1;
        if (distance > 25 && distance < 10000)
            // 4 slots, 90 degrees each, slot 0 at the top
            hoveredSlot = (Mth.floor(AngleHelper.deg(Mth.atan2(hoveredY, hoveredX)) + 360 + 90 + 45) % 360) / 90;
        if (scrollMode && distance > 150)
            scrollMode = false;
        int selected = selected();

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(width / 2f, height / 2f, 0);

        // the shape the pipe is set to now, in the middle
        AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
        GuiGameElement.of(stackOf(current)).at(-9, -9).render(graphics);

        Component tip = null;
        for (int slot = 0; slot < SHAPES.length; slot++) {
            ms.pushPose();
            TransformStack.of(ms)
                    .rotateZDegrees(slot * 90)
                    .translate(0, -40 + (10 * (1 - fade) * (1 - fade)), 0)
                    .rotateZDegrees(-slot * 90);
            ms.translate(-12, -12, 0);

            AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
            ItemStack stack = stackOf(SHAPES[slot]);
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

    private ItemStack stackOf(PipeShape shape) {
        return ParticleAcceleratorPipeItem.stackOf(minecraft.player.getMainHandItem().getItem(), shape);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Color color = BACKGROUND_COLOR.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));
        graphics.fillGradient(0, 0, width, height, color.getRGB(), color.getRGB());
    }

    @Override
    public void tick() {
        ticksOpen++;
        // the pipe left the main hand (e.g. dropped), so there's nothing to set
        if (!(minecraft.player.getMainHandItem().getItem() instanceof ParticleAcceleratorPipeItem)) {
            cancelled = true;
            onClose();
        }
        super.tick();
    }

    @Override
    public void removed() {
        super.removed();
        int selected = selected();
        if (cancelled || selected < 0 || SHAPES[selected] == current)
            return;
        PipeShape shape = SHAPES[selected];
        // set it here too, so the held item changes straight away
        ParticleAcceleratorPipeItem.setShape(minecraft.player.getMainHandItem(), shape);
        PacketDistributor.sendToServer(new SelectPipeShapePayload(shape));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollMode = true;
        scrollSlot = Math.floorMod(scrollSlot - (int) Math.signum(scrollY), SHAPES.length);
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
        for (int i = 0; i < SHAPES.length && i < hotbarBinds.length; i++) {
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
