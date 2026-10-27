package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.damage.ModDamageTypes;
import net.succ.solar_punk.sound.ModSounds;
import net.succ.solar_punk.util.PonderHelper;

import java.util.List;

// An uncapped vent erupts every few minutes: it rumbles for a few seconds, then shoots a
// column of steam that scalds and launches anything standing over it. A Geyser Cap tames it.
// The schedule is a game time saved with the world and synced to clients, so the goggles
// can count down and both sides agree on the phase without sending updates every tick.
// The idle puff sound plays here every 40 ticks, since GeyserVentBlock#animateTick only
// fires when the client randomly samples this block. The idle particles stay in animateTick.
public class GeyserVentBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public enum Phase { DORMANT, WARNING, ERUPTING }

    private static final int HIT_INTERVAL = 10;
    private static final double LAUNCH_SPEED = 0.9;

    // Game time the next eruption starts at, 0 when none is scheduled (capped, eruptions
    // turned off, or in Ponder, where only the client runs)
    private long nextEruption;
    // Saved with the schedule, since clients don't share the server's COMMON config
    private int warningTicks, eruptionTicks;
    // Comparator output, kept so neighbours only update when it changes
    private int lastSignal;

    public GeyserVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            tickClient();
            return;
        }

        long time = level.getGameTime();
        boolean active = Config.geyserEruptions && !isCapped();
        if (!active) {
            if (nextEruption != 0) setSchedule(0);
        } else if (nextEruption == 0 || time >= nextEruption + eruptionTicks) {
            // First tick, just uncapped, the last eruption ended, or the chunk was unloaded
            // through the eruption: schedule the next one
            RandomSource random = level.random;
            setSchedule(time + Mth.randomBetweenInclusive(random,
                    Config.geyserMinIntervalTicks, Config.geyserMaxIntervalTicks));
        }

        if (getPhase() == Phase.ERUPTING && (time - nextEruption) % HIT_INTERVAL == 0)
            scaldEntities();

        if (time % 20 == 0) {
            int signal = getComparatorSignal();
            if (signal != lastSignal) {
                lastSignal = signal;
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
        }
    }

    private void setSchedule(long eruption) {
        nextEruption = eruption;
        warningTicks = Config.geyserWarningTicks;
        eruptionTicks = Config.geyserEruptionTicks;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    private boolean isCapped() {
        return level.getBlockState(worldPosition.above()).is(ModBlocks.GEYSER_CAP.get());
    }

    public Phase getPhase() {
        if (level == null || nextEruption == 0) return Phase.DORMANT;
        long time = level.getGameTime();
        if (time >= nextEruption && time < nextEruption + eruptionTicks) return Phase.ERUPTING;
        if (time >= nextEruption - warningTicks && time < nextEruption) return Phase.WARNING;
        return Phase.DORMANT;
    }

    /** Ticks until the next eruption starts, or -1 when none is scheduled. */
    public long ticksUntilEruption() {
        if (level == null || nextEruption == 0) return -1;
        return Math.max(0, nextEruption - level.getGameTime());
    }

    /** 15 while erupting, otherwise rising from 0 to 14 over the last two minutes before it. */
    public int getComparatorSignal() {
        if (getPhase() == Phase.ERUPTING) return 15;
        long ticks = ticksUntilEruption();
        if (ticks < 0) return 0;
        int window = 2 * 60 * 20;
        return ticks >= window ? 0 : 14 - (int) (ticks * 14 / window);
    }

    /** How many blocks the column reaches above the vent before something solid stops it. */
    private int columnHeight() {
        int height = 0;
        BlockPos.MutableBlockPos pos = worldPosition.mutable();
        while (height < Config.geyserHeight) {
            pos.move(0, 1, 0);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) break;
            height++;
        }
        return height;
    }

    private void scaldEntities() {
        int height = columnHeight();
        if (height <= 0) return;
        AABB column = new AABB(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1 + height, worldPosition.getZ() + 1);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, column,
                e -> !e.isSpectator() && !(e instanceof Player p && p.getAbilities().invulnerable));
        for (LivingEntity entity : entities) {
            if (Config.geyserDamage > 0)
                entity.hurt(ModDamageTypes.source(level, ModDamageTypes.SCALDING), Config.geyserDamage);
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, Math.max(motion.y, LAUNCH_SPEED), motion.z);
            entity.hurtMarked = true;  // sends the new motion to players
            if (entity instanceof ServerPlayer player)
                ModTriggers.GEYSER_SCALDED.get().trigger(player);
        }
    }

    private void tickClient() {
        if (PonderHelper.isPonder(level)) return;
        long time = level.getGameTime();
        double cx = worldPosition.getX() + 0.5, cy = worldPosition.getY() + 1.0, cz = worldPosition.getZ() + 0.5;
        RandomSource random = level.random;

        switch (getPhase()) {
            case DORMANT -> {
                // A Geyser Cap on top plays the same sound itself, so don't double up.
                if (time % 40 != 0 || isCapped()) return;
                level.playLocalSound(cx, cy, cz, ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS,
                        0.15f, 0.9f + random.nextFloat() * 0.2f, false);
            }
            case WARNING -> {
                if (time % 15 == 0)
                    level.playLocalSound(cx, cy, cz, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS,
                            0.8f, 0.6f + random.nextFloat() * 0.2f, false);
                for (int i = 0; i < 3; i++)
                    level.addParticle(ParticleTypes.BUBBLE_POP, cx + (random.nextDouble() - 0.5) * 0.8, cy,
                            cz + (random.nextDouble() - 0.5) * 0.8, 0, 0.05, 0);
                if (random.nextInt(3) == 0)
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy, cz, 0, 0.05, 0);
            }
            case ERUPTING -> {
                if (time == nextEruption) {
                    level.playLocalSound(cx, cy, cz, ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS,
                            1.5f, 0.6f, false);
                    level.playLocalSound(cx, cy, cz, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                            1.0f, 0.5f, false);
                }
                if (time % 20 == 0)
                    level.playLocalSound(cx, cy, cz, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                            1.0f, 0.5f + random.nextFloat() * 0.2f, false);
                // Speed scales with the column, so the steam tops out about where it stops hurting
                double speed = 0.25 + columnHeight() * 0.12;
                for (int i = 0; i < 12; i++)
                    level.addParticle(ParticleTypes.CLOUD, cx + (random.nextDouble() - 0.5) * 0.6, cy,
                            cz + (random.nextDouble() - 0.5) * 0.6, (random.nextDouble() - 0.5) * 0.1,
                            speed * (0.7 + random.nextDouble() * 0.5), (random.nextDouble() - 0.5) * 0.1);
                for (int i = 0; i < 8; i++)
                    level.addParticle(ParticleTypes.SPLASH, cx + (random.nextDouble() - 0.5) * 0.5, cy,
                            cz + (random.nextDouble() - 0.5) * 0.5, (random.nextDouble() - 0.5) * 0.4,
                            speed * (0.8 + random.nextDouble() * 0.6), (random.nextDouble() - 0.5) * 0.4);
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.geyser_vent_header").forGoggles(tooltip);
        Component status = switch (getPhase()) {
            case ERUPTING -> CreateLang.translate("solar_punk.tooltip.geyser_vent_erupting")
                    .style(ChatFormatting.RED).component();
            case WARNING -> CreateLang.translate("solar_punk.tooltip.geyser_vent_warning")
                    .style(ChatFormatting.GOLD).component();
            case DORMANT -> {
                long ticks = ticksUntilEruption();
                if (ticks < 0)
                    yield CreateLang.translate("solar_punk.tooltip.geyser_vent_quiet")
                            .style(ChatFormatting.GREEN).component();
                long seconds = (ticks + 19) / 20;
                yield Component.literal(seconds >= 60
                                ? (seconds / 60) + "m " + (seconds % 60) + "s"
                                : seconds + "s")
                        .withStyle(ChatFormatting.AQUA);
            }
        };
        CreateLang.translate("solar_punk.tooltip.geyser_vent_next")
                .style(ChatFormatting.GRAY)
                .add(status)
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("NextEruption", nextEruption);
        tag.putInt("WarningTicks", warningTicks);
        tag.putInt("EruptionTicks", eruptionTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        nextEruption = tag.getLong("NextEruption");
        warningTicks = tag.getInt("WarningTicks");
        eruptionTicks = tag.getInt("EruptionTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
