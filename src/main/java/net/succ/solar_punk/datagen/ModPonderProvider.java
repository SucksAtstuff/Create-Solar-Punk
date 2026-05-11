package net.succ.solar_punk.datagen;

import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.succ.solar_punk.SolarPunk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

public class ModPonderProvider implements DataProvider {

    private final PackOutput output;

    public ModPonderProvider(PackOutput output) {
        this.output = output;
    }

    // -------------------------------------------------------------------------
    // Scene schematic definitions
    // -------------------------------------------------------------------------

    private static final Map<String, SceneStructure> SCHEMATICS = new LinkedHashMap<>();

    static {
        SceneStructure solarHeaterScene = new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:solar_heater",
                        "facing", "north", "lit", "false");

        SCHEMATICS.put("solar_heater/usage", solarHeaterScene);
        SCHEMATICS.put("solar_heater/evaporation", solarHeaterScene);

        SCHEMATICS.put("solar_panel/andesite", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "create:shaft", "axis", "y")
                .addBlock(2, 2, 2, "solarpunk:andesite_solar_panel", "lit", "false"));

        SCHEMATICS.put("solar_panel/brass", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:brass_solar_panel", "lit", "false"));

        SCHEMATICS.put("heat_battery/filling", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:heat_battery", "heat", "0"));

        SCHEMATICS.put("heat_battery/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:heat_battery", "heat", "2"));

        SCHEMATICS.put("kinetic_battery/usage", new SceneStructure()
                .withBasePlate()
                // battery on the Z axis so shaft connections are north/south
                .addBlock(2, 1, 2, "solarpunk:kinetic_battery", "axis", "z", "lit", "false")
                // charging side (south, z+): shaft then hand-crank facing south
                // facing=south → crank handle points away, shaft connects toward battery
                .addBlock(2, 1, 3, "create:shaft", "axis", "z")
                .addBlock(2, 1, 4, "create:hand_crank", "facing", "south")
                // discharge side (north, z-): shaft then mechanical press facing=north
                // facing=north → press shaft connects on south face toward battery
                .addBlock(2, 1, 1, "create:shaft", "axis", "z")
                .addBlock(2, 1, 0, "create:mechanical_press", "facing", "north"));
    }

    // -------------------------------------------------------------------------
    // DataProvider implementation
    // -------------------------------------------------------------------------

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (var entry : SCHEMATICS.entrySet()) {
            futures.add(saveSchematic(cache, entry.getKey(), entry.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveSchematic(CachedOutput cache, String path, SceneStructure structure) {
        Path outputPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(SolarPunk.MODID)
                .resolve("ponder")
                .resolve(path + ".nbt");

        try {
            byte[] bytes = toGzipBytes(structure.build());
            cache.writeIfNeeded(outputPath, bytes, Hashing.sha256().hashBytes(bytes));
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static byte[] toGzipBytes(CompoundTag tag) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(baos))) {
            NbtIo.write(tag, dos);
        }
        return baos.toByteArray();
    }

    @Override
    public String getName() {
        return "Ponder Schematics: " + SolarPunk.MODID;
    }

    // -------------------------------------------------------------------------
    // Structure builder
    // -------------------------------------------------------------------------

    private static class SceneStructure {
        private final List<CompoundTag> palette = new ArrayList<>();
        private final List<CompoundTag> blocks = new ArrayList<>();
        private final Map<String, Integer> paletteIndex = new LinkedHashMap<>();

        SceneStructure withBasePlate() {
            int idx = getOrAddPalette("minecraft:smooth_stone", Map.of());
            for (int x = 0; x < 5; x++)
                for (int z = 0; z < 5; z++)
                    placeBlock(idx, x, 0, z);
            return this;
        }

        SceneStructure addBlock(int x, int y, int z, String name, String... properties) {
            Map<String, String> props = new LinkedHashMap<>();
            for (int i = 0; i < properties.length; i += 2)
                props.put(properties[i], properties[i + 1]);
            int idx = getOrAddPalette(name, props);
            placeBlock(idx, x, y, z);
            return this;
        }

        private int getOrAddPalette(String name, Map<String, String> properties) {
            String key = name + properties;
            return paletteIndex.computeIfAbsent(key, k -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("Name", name);
                if (!properties.isEmpty()) {
                    CompoundTag props = new CompoundTag();
                    properties.forEach(props::putString);
                    entry.put("Properties", props);
                }
                palette.add(entry);
                return palette.size() - 1;
            });
        }

        private void placeBlock(int paletteIdx, int x, int y, int z) {
            CompoundTag block = new CompoundTag();
            block.putInt("state", paletteIdx);
            ListTag pos = new ListTag();
            pos.add(IntTag.valueOf(x));
            pos.add(IntTag.valueOf(y));
            pos.add(IntTag.valueOf(z));
            block.put("pos", pos);
            blocks.add(block);
        }

        CompoundTag build() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("DataVersion", 3955);

            ListTag size = new ListTag();
            size.add(IntTag.valueOf(5));
            size.add(IntTag.valueOf(8));
            size.add(IntTag.valueOf(5));
            tag.put("size", size);

            ListTag paletteList = new ListTag();
            palette.forEach(paletteList::add);
            tag.put("palette", paletteList);

            ListTag blocksList = new ListTag();
            blocks.forEach(blocksList::add);
            tag.put("blocks", blocksList);

            tag.put("entities", new ListTag());
            return tag;
        }
    }
}