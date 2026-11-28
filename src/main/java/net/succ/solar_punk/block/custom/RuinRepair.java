package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

/**
 * Fixing the ruined solar blocks: what broke on them is the glass, so the same clear Glass Panes
 * their recipes use put them right (3 for a Solar Heater, 1 for a Solar Mirror). The rusted
 * ones (Geyser Cap, Kinetic Sprinkler, Streetlamp) are scraped clean with an axe instead.
 */
final class RuinRepair {
    private RuinRepair() {}

    /** Whether {@code stack} holds enough clear Glass Panes for the repair. */
    static boolean canRepair(ItemStack stack, Player player, int panes) {
        return stack.is(Tags.Items.GLASS_PANES_COLORLESS) && (player.isCreative() || stack.getCount() >= panes);
    }

    /** Uses up the panes and plays the repair's sound and sparkle at {@code pos}. */
    static void finish(Level level, BlockPos pos, Player player, ItemStack stack, int panes) {
        if (!player.isCreative()) stack.shrink(panes);
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.6f, 1.2f);
        if (level instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    10, 0.35, 0.25, 0.35, 0.05);
    }

    /** Whether {@code stack} is an axe that can scrape the rust off. */
    static boolean canScrape(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_SCRAPE);
    }

    /** Plays the scrape's sound and particles at {@code pos} and wears the axe. Call on both sides. */
    static void scrape(Level level, BlockPos pos, BlockState result, Player player, ItemStack stack, InteractionHand hand) {
        level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.levelEvent(player, 3005, pos, 0); // the scraping particles
        if (level.isClientSide) return;
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, result));
        if (!player.isCreative()) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
    }
}
