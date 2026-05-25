package net.succ.solar_punk.pollution;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PollutionSavedData extends SavedData {
    private static final String DATA_NAME = "solarpunk_pollution";

    private final Map<ChunkPos, Long> chunkPollution = new HashMap<>();

    public static PollutionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PollutionSavedData::new, PollutionSavedData::load, DataFixTypes.LEVEL),
            DATA_NAME
        );
    }

    private PollutionSavedData() {}

    public void addPollution(ChunkPos chunk, long amount) {
        chunkPollution.merge(chunk, amount, Long::sum);
        setDirty();
    }

    public long getPollution(ChunkPos chunk) {
        return chunkPollution.getOrDefault(chunk, 0L);
    }

    public void reducePollution(ChunkPos chunk, long amount) {
        long current = chunkPollution.getOrDefault(chunk, 0L);
        if (current <= 0) return;
        long next = current - amount;
        if (next <= 0) {
            chunkPollution.remove(chunk);
        } else {
            chunkPollution.put(chunk, next);
        }
        setDirty();
    }

    public void decayAll(long amount) {
        if (amount <= 0 || chunkPollution.isEmpty()) return;
        chunkPollution.replaceAll((pos, val) -> Math.max(0L, val - amount));
        chunkPollution.entrySet().removeIf(e -> e.getValue() == 0L);
        setDirty();
    }

    public Map<ChunkPos, Long> getChunkPollution() {
        return Collections.unmodifiableMap(chunkPollution);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, Long> entry : chunkPollution.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("X", entry.getKey().x);
            entryTag.putInt("Z", entry.getKey().z);
            entryTag.putLong("Pollution", entry.getValue());
            list.add(entryTag);
        }
        tag.put("ChunkPollution", list);
        return tag;
    }

    public static PollutionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PollutionSavedData data = new PollutionSavedData();
        ListTag list = tag.getList("ChunkPollution", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            ChunkPos pos = new ChunkPos(entryTag.getInt("X"), entryTag.getInt("Z"));
            data.chunkPollution.put(pos, entryTag.getLong("Pollution"));
        }
        return data;
    }
}