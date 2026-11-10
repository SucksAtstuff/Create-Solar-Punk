package net.succ.solar_punk.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.custom.WireNodeBlockEntity;
import net.succ.solar_punk.wire.WireNodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.wire.WireTier;

import java.util.List;

/**
 * A spool of wire. Click one Wire Connector (or a post of a Wire Switch), then another, to string a wire between them. The
 * spool holds {@link #WIRES} wires (its durability) and uses one per blocks_per_wire blocks of
 * length; an emptied spool turns back into an Empty Spool. Sneak-click anything else to let go
 * of a started wire.
 */
public class SpoolItem extends Item {
    public static final int WIRES = 4;

    private final WireTier tier;

    public SpoolItem(WireTier tier, Properties properties) {
        super(properties.durability(WIRES));
        this.tier = tier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        GlobalPos start = stack.get(ModDataComponents.WIRE_START.get());
        BlockState state = level.getBlockState(pos);

        if (!(level.getBlockEntity(pos) instanceof WireNodeBlockEntity here)
                || !(state.getBlock() instanceof WireNodeBlock node)) {
            if (start == null || player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
            if (!level.isClientSide) {
                clearStart(stack);
                message(player, "cancelled");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        int terminal = node.terminalAt(state, pos, context.getClickLocation());
        if (start == null) {
            if (here.countOn(terminal) >= Config.wireMaxConnections) return fail(player, stack, "full");
            stack.set(ModDataComponents.WIRE_START.get(), GlobalPos.of(level.dimension(), pos));
            stack.set(ModDataComponents.WIRE_START_TERMINAL.get(), terminal);
            message(player, "started");
            return InteractionResult.SUCCESS;
        }

        BlockPos from = start.pos();
        int fromTerminal = stack.getOrDefault(ModDataComponents.WIRE_START_TERMINAL.get(), 0);
        if (from.equals(pos)) {
            clearStart(stack);
            message(player, "cancelled");
            return InteractionResult.SUCCESS;
        }
        if (!start.dimension().equals(level.dimension()) || !level.isLoaded(from)
                || !(level.getBlockEntity(from) instanceof WireNodeBlockEntity there)
                || fromTerminal >= there.terminals())
            return fail(player, stack, "start_missing");
        if (here.isConnectedTo(terminal, from, fromTerminal)) return fail(player, stack, "already_connected");
        if (here.countOn(terminal) >= Config.wireMaxConnections
                || there.countOn(fromTerminal) >= Config.wireMaxConnections)
            return fail(player, stack, "full");

        double length = Math.sqrt(pos.distSqr(from));
        if (length > tier.maxLength()) return fail(player, stack, "too_long", tier.maxLength());

        int wires = WireTier.wiresFor(length);
        boolean creative = player != null && player.getAbilities().instabuild;
        int left = stack.getMaxDamage() - stack.getDamageValue();
        if (!creative && wires > left) return fail(player, stack, "not_enough", wires);

        here.addConnection(terminal, from, fromTerminal, tier, wires);
        there.addConnection(fromTerminal, pos, terminal, tier, wires);
        clearStart(stack);
        level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);

        if (!creative) {
            if (wires >= left) {
                ItemStack empty = new ItemStack(ModItems.EMPTY_SPOOL.get());
                stack.shrink(1);
                if (player != null && !player.getInventory().add(empty)) player.drop(empty, false);
            } else {
                stack.setDamageValue(stack.getDamageValue() + wires);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void clearStart(ItemStack stack) {
        stack.remove(ModDataComponents.WIRE_START.get());
        stack.remove(ModDataComponents.WIRE_START_TERMINAL.get());
    }

    private InteractionResult fail(Player player, ItemStack stack, String key, Object... args) {
        clearStart(stack);
        message(player, key, args);
        return InteractionResult.FAIL;
    }

    private static void message(Player player, String key, Object... args) {
        if (player != null) player.displayClientMessage(Component.translatable("message.solarpunk.wire." + key, args), true);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.WIRE_START.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int left = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(Component.translatable("item.solarpunk.spool.wires_left", left, left * Config.wireBlocksPerWire)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.solarpunk.spool.stats", EnergyFormat.feRate(tier.rate()), tier.maxLength())
                .withStyle(ChatFormatting.GRAY));
        GlobalPos start = stack.get(ModDataComponents.WIRE_START.get());
        if (start != null)
            tooltip.add(Component.translatable("item.solarpunk.spool.started",
                    start.pos().getX(), start.pos().getY(), start.pos().getZ()).withStyle(ChatFormatting.GOLD));
    }
}
