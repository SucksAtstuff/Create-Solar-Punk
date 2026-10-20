package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.sound.ModSounds;

// Plays the vent's puff sound every 40 ticks, like GeyserCapBlockEntity does. This can't
// live in GeyserVentBlock#animateTick, which only fires when the client randomly samples
// this block, so the sound would be very rare. The particles stay in animateTick.
public class GeyserVentBlockEntity extends BlockEntity {

    public GeyserVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null) return;
        if (level.getGameTime() % 40 != 0) return;
        if (!level.isClientSide) return;

        // A Geyser Cap on top plays the same sound itself, so don't double up.
        if (level.getBlockState(worldPosition.above()).is(ModBlocks.GEYSER_CAP.get())) return;

        level.playLocalSound(
                worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS,
                0.15f, 0.9f + level.random.nextFloat() * 0.2f, false);
    }
}
