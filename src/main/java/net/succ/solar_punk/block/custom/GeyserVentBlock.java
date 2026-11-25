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
import net.succ.solar_punk.particle.SeasonalSteam;

public class GeyserVentBlock extends Block implements IBE<GeyserVentBlockEntity> {
    // The Basalt Deltas variant: more Geyser Cap output, fire and smoke instead of water
    private final boolean nether;

    public GeyserVentBlock(boolean nether, Properties properties) {
        super(properties);
        this.nether = nether;
    }

    public boolean isNether() {
        return nether;
    }

    /** Whether the block at this state is a Nether Geyser Vent. */
    public static boolean isNether(BlockState state) {
        return state.getBlock() instanceof GeyserVentBlock vent && vent.nether;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;

        // Particles only. The puff sound is played from GeyserVentBlockEntity, since
        // animateTick only fires when the client randomly samples this block.

        // Dense steam column, thicker from a strong vent and thinner from a weak one
        float strength = level.getBlockEntity(pos) instanceof GeyserVentBlockEntity vent ? vent.getStrength().multiplier() : 1;
        int steam = Math.round((20 + random.nextInt(10)) * Math.min(2, Math.max(0.4f, strength)));
        for (int i = 0; i < steam; i++) {
            double x = cx + (random.nextDouble() - 0.5) * 0.8;
            double z = cz + (random.nextDouble() - 0.5) * 0.8;
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = 0.8 + random.nextDouble() * 0.8;
            double vz = (random.nextDouble() - 0.5) * 0.3;
            SeasonalSteam.add(level, x, cy, z, vx, vy, vz);
        }
        // Water spray, or embers and the odd lava drip from a Nether vent
        for (int i = 0; i < 10 + random.nextInt(8); i++) {
            double x = cx + (random.nextDouble() - 0.5) * 0.6;
            double z = cz + (random.nextDouble() - 0.5) * 0.6;
            double vx = (random.nextDouble() - 0.5) * 0.5;
            double vy = 0.7 + random.nextDouble() * 0.7;
            double vz = (random.nextDouble() - 0.5) * 0.5;
            if (!nether) level.addParticle(ParticleTypes.SPLASH, x, cy, z, vx, vy, vz);
            else if (i < 4) level.addParticle(ParticleTypes.FLAME, x, cy, z, vx * 0.1, vy * 0.1, vz * 0.1);
        }
        if (nether && random.nextInt(4) == 0)
            level.addParticle(ParticleTypes.LAVA, cx, cy, cz, 0, 0, 0);
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

    // Comparators read how close the next eruption is (see GeyserVentBlockEntity)
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof GeyserVentBlockEntity vent ? vent.getComparatorSignal() : 0;
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