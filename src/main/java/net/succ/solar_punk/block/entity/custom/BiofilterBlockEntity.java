package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.client.sound.BiofilterSoundInstance;
import net.succ.solar_punk.pollution.PollutionSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiofilterBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    private long chunkPollution = 0;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private BiofilterSoundInstance soundInstance;

    public BiofilterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        float impact = Config.biofilterSu;
        this.lastStressApplied = impact;
        return impact;
    }

    public boolean isPowered() {
        return Math.abs(getSpeed()) > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        if (isPowered() && (soundInstance == null || soundInstance.isStopped())) {
            soundInstance = new BiofilterSoundInstance(this);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 20 != 0) return;

        PollutionSavedData data = PollutionSavedData.get(serverLevel);
        long newPollution = data.getPollution(new ChunkPos(worldPosition));
        if (newPollution != chunkPollution) {
            chunkPollution = newPollution;
            sendData();
        }

        if (!Config.globalWarmingEnabled || !isPowered()) return;

        int radius = Config.biofilterRadiusBlocks;
        int radiusChunks = radius > 0 ? (int) Math.ceil(radius / 16.0) : 0;
        double radiusSq = (double) radius * radius;
        ChunkPos sourceChunk = new ChunkPos(worldPosition);
        // Scales with actual rotational speed rather than a flat rate - a cheap 16 RPM
        // feed and a maxed-out network shouldn't clean at the same rate for wildly
        // different SU cost, same reasoning the Turbine's height/blade-ratio knobs use
        // for scaling steam output.
        long absorb = Math.round(Math.abs(getSpeed()) * Config.biofilterAbsorptionPerRpm);

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                ChunkPos affected = new ChunkPos(sourceChunk.x + dx, sourceChunk.z + dz);
                if (radius > 0) {
                    int nearX = Math.max(affected.getMinBlockX(), Math.min(worldPosition.getX(), affected.getMaxBlockX()));
                    int nearZ = Math.max(affected.getMinBlockZ(), Math.min(worldPosition.getZ(), affected.getMaxBlockZ()));
                    double distSq = (double)(nearX - worldPosition.getX()) * (nearX - worldPosition.getX())
                                  + (double)(nearZ - worldPosition.getZ()) * (nearZ - worldPosition.getZ());
                    if (distSq > radiusSq) continue;
                }
                data.reducePollution(affected, absorb);
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.biofilter_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        boolean powered = isPowered();
        CreateLang.translate("solar_punk.tooltip.biofilter_status")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(powered ? "Active" : "No Power")
                        .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        ChatFormatting pollutionColor = chunkPollution == 0 ? ChatFormatting.GREEN
                : chunkPollution < Config.biomeDecayThreshold / 2 ? ChatFormatting.YELLOW
                : ChatFormatting.RED;
        CreateLang.translate("solar_punk.tooltip.biofilter_pollution")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(chunkPollution).style(pollutionColor).component())
                .forGoggles(tooltip, 1);

        if (powered) {
            long absorb = Math.round(Math.abs(getSpeed()) * Config.biofilterAbsorptionPerRpm);
            CreateLang.translate("solar_punk.tooltip.biofilter_removing")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(absorb).text("/s")
                            .style(ChatFormatting.GREEN).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) tag.putLong("ChunkPollution", chunkPollution);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) chunkPollution = tag.getLong("ChunkPollution");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}
