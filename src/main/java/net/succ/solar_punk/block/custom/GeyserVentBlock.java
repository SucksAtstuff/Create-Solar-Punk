package net.succ.solar_punk.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.GeyserVentBlockEntity;

public class GeyserVentBlock extends Block implements IBE<GeyserVentBlockEntity> {
    public GeyserVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;

        // The puff sound used to live here too, but animateTick only fires when the
        // client's particle system happens to randomly sample this exact block position
        // (see GeyserVentBlockEntity for why that made the sound extremely rare without a
        // Geyser Cap on top) - it now runs on GeyserVentBlockEntity's real per-tick
        // schedule instead. This method still handles the dense steam/water/mist burst
        // below, which is just ambient flourish and doesn't need to land on an exact beat.

        // Dense steam column
        for (int i = 0; i < 20 + random.nextInt(10); i++) {
            double x = cx + (random.nextDouble() - 0.5) * 0.8;
            double z = cz + (random.nextDouble() - 0.5) * 0.8;
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = 0.8 + random.nextDouble() * 0.8;
            double vz = (random.nextDouble() - 0.5) * 0.3;
            level.addParticle(ParticleTypes.CLOUD, x, cy, z, vx, vy, vz);
        }
        // Water spray
        for (int i = 0; i < 10 + random.nextInt(8); i++) {
            double x = cx + (random.nextDouble() - 0.5) * 0.6;
            double z = cz + (random.nextDouble() - 0.5) * 0.6;
            double vx = (random.nextDouble() - 0.5) * 0.5;
            double vy = 0.7 + random.nextDouble() * 0.7;
            double vz = (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ParticleTypes.SPLASH, x, cy, z, vx, vy, vz);
        }
        // Fine mist wisps
        for (int i = 0; i < 5 + random.nextInt(5); i++) {
            double x = cx + (random.nextDouble() - 0.5) * 0.4;
            double z = cz + (random.nextDouble() - 0.5) * 0.4;
            double vx = (random.nextDouble() - 0.5) * 0.15;
            double vy = 1.0 + random.nextDouble() * 0.5;
            double vz = (random.nextDouble() - 0.5) * 0.15;
            level.addParticle(ParticleTypes.SMOKE, x, cy, z, vx, vy, vz);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.GEYSER_VENT.get())
            return (BlockEntityTicker<T>) (BlockEntityTicker<GeyserVentBlockEntity>) (l, p, s, be) -> be.tick();
        return null;
    }

    @Override
    public Class<GeyserVentBlockEntity> getBlockEntityClass() {
        return GeyserVentBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GeyserVentBlockEntity> getBlockEntityType() {
        return ModBlockEntities.GEYSER_VENT.get();
    }
}