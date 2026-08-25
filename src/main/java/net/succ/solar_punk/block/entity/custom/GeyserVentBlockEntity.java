package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.sound.ModSounds;

// Drives the vent's puff sound on the same deterministic ~2s (40-tick) world-clock
// cadence GeyserCapBlockEntity already uses for the exact same sound - a real
// BlockEntity#tick() is called every tick unconditionally for as long as the chunk is
// loaded, unlike GeyserVentBlock#animateTick, which only fires when the client's
// particle system happens to randomly sample this exact block position (it samples
// ~1334 random positions per tick out of tens of thousands in range, not every block
// every tick - see ClientLevel#animateTick). That made the naked vent's puff sound come
// out extremely rare in practice, while a vent with a Geyser Cap on top sounded fine,
// since the Cap's own BlockEntity tick played the same sound on a real per-tick
// schedule. Giving the vent its own BlockEntity for this puts both cases on equal,
// reliable footing. The dense steam/water/mist particle burst stays in
// GeyserVentBlock#animateTick untouched - that's pure ambient flourish, not something
// that needs to land on an exact schedule the way the sound does.
public class GeyserVentBlockEntity extends BlockEntity {

    public GeyserVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null) return;
        if (level.getGameTime() % 40 != 0) return;
        if (!level.isClientSide) return;

        // A Geyser Cap placed directly on top plays this exact sound itself, on the same
        // 40-tick cadence (see GeyserCapBlockEntity#tick) - skip here so a capped vent
        // doesn't double up on the puff every cycle.
        if (level.getBlockState(worldPosition.above()).is(ModBlocks.GEYSER_CAP.get())) return;

        // See GeyserVentBlock#animateTick for the loudness-matching behind 0.15f.
        level.playLocalSound(
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS,
                0.15f, 0.9f + level.random.nextFloat() * 0.2f, false);
    }
}
