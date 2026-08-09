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
        SCHEMATICS.put("biomass_gasifier/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "create:shaft", "axis", "y")
                .addBlock(2, 2, 2, "solarpunk:biomass_gasifier", "facing", "north", "lit", "false"));

        SCHEMATICS.put("biofuel_engine/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "create:shaft", "axis", "y")
                .addBlock(2, 2, 2, "solarpunk:biofuel_engine", "facing", "north", "lit", "false"));

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

        // cap facing=north → rotation axis=X → shafts connect on east/west (x axis)
        SCHEMATICS.put("geyser_cap/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:geyser_vent")
                .addBlock(2, 2, 2, "solarpunk:geyser_cap", "facing", "north", "lit", "false")
                .addBlock(1, 2, 2, "create:shaft", "axis", "x")
                .addBlock(3, 2, 2, "create:shaft", "axis", "x"));

        // 2×2×3 minimum working vat: x=1-2, z=1-2, y=1-3
        SCHEMATICS.put("fermentation_vat/usage",   addVatLayers(new SceneStructure().withBasePlate(), 2, 3));
        // 3×3×3 larger vat to show scaling
        SCHEMATICS.put("fermentation_vat/scaling", addVatLayers(new SceneStructure().withBasePlate(), 3, 3));

        // floor + 3 blade layers + cap = 5 total layers; 7x7x7 schematic
        SCHEMATICS.put("turbine_rotor/structure",
                addTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));
        SCHEMATICS.put("turbine_rotor/condensate",
                addTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));

        // floor + 7 blade layers + cap = 9 total layers; 7x11x7 schematic
        SCHEMATICS.put("turbine_rotor/max_turbine",
                addTurbineLayers(new SceneStructure(7, 11, 7).withBasePlate(), 7, true));

        // Same size as turbine_rotor/structure, but grown along the X axis instead of Y:
        // floor(x=1) + 3 blade layers(x=2-4) + cap(x=5); 7x7x7 schematic.
        SCHEMATICS.put("turbine_rotor/horizontal",
                addHorizontalTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));

        // Tower pushed to the back corner (x=5-7/z=5-7/y=1-3) with a heliostat field
        // spread out on the ground toward the camera, to its west and north - mirrors
        // sit in front of the tower instead of behind it so the tower doesn't block
        // them from view. Mirrors are freestanding two-cell blocks (lower/upper), not
        // attached to the tower's faces.
        SceneStructure towerWithMirrors = addTowerLayers(new SceneStructure(9, 8, 9).withBasePlate());
        addGroundMirror(towerWithMirrors, 3, 5); // west side, near
        addGroundMirror(towerWithMirrors, 3, 7);
        addGroundMirror(towerWithMirrors, 0, 5); // west side, far
        addGroundMirror(towerWithMirrors, 0, 7);
        addGroundMirror(towerWithMirrors, 5, 3); // north side, near
        addGroundMirror(towerWithMirrors, 7, 3);
        addGroundMirror(towerWithMirrors, 5, 0); // north side, far
        addGroundMirror(towerWithMirrors, 7, 0);
        SCHEMATICS.put("solar_power_tower/usage",   towerWithMirrors);
        SCHEMATICS.put("solar_power_tower/mirrors", towerWithMirrors);
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

    private static SceneStructure addVatLayers(SceneStructure s, int width, int height) {
        for (int x = 1; x <= width; x++)
            for (int z = 1; z <= width; z++) {
                String bottom = height == 1 ? "single" : "bottom";
                s.addBlock(x, 1, z, "solarpunk:fermentation_vat", "lit", "false", "position", bottom);
                for (int y = 2; y < height; y++)
                    s.addBlock(x, y, z, "solarpunk:fermentation_vat", "lit", "false", "position", "middle");
                if (height > 1)
                    s.addBlock(x, height, z, "solarpunk:fermentation_vat", "lit", "false", "position", "top");
            }
        return s;
    }

    // Adds a floor (y=1), `bladeLayers` blade layers (y=2..bladeLayers+1), then a top cap (y=bladeLayers+2).
    // Floor and cap are full 7x7 of casings with the rotor at center.
    // Blade layer rings use solid casing at the 4 corner columns and glass casing on the wall panels between them.
    private static SceneStructure addTurbineLayers(SceneStructure s, int bladeLayers, boolean allBrass) {
        String blade = allBrass ? "solarpunk:brass_turbine_blade" : "solarpunk:andesite_turbine_blade";
        // Floor layer (y=1): full 7x7 casings, no rotor.
        for (int x = 0; x <= 6; x++)
            for (int z = 0; z <= 6; z++)
                s.addBlock(x, 1, z, "solarpunk:turbine_casing");
        // Blade layers (y=2 to y=bladeLayers+1).
        for (int y = 2; y <= bladeLayers + 1; y++) {
            for (int x = 0; x <= 6; x++)
                for (int z = 0; z <= 6; z++)
                    if (x == 0 || x == 6 || z == 0 || z == 6) {
                        boolean isCorner = (x == 0 || x == 6) && (z == 0 || z == 6);
                        String wall = isCorner ? "solarpunk:turbine_casing" : "solarpunk:turbine_casing_glass";
                        s.addBlock(x, y, z, wall);
                    }
            s.addBlock(3, y, 3, "solarpunk:turbine_rotor", "lit", "false");
            s.addBlock(4, y, 3, blade, "facing", "east",  "hidden", "false");
            s.addBlock(2, y, 3, blade, "facing", "west",  "hidden", "false");
            s.addBlock(3, y, 4, blade, "facing", "south", "hidden", "false");
            s.addBlock(3, y, 2, blade, "facing", "north", "hidden", "false");
        }
        // Top cap (y=bladeLayers+2): full 7x7 casings + rotor at center.
        int cap = bladeLayers + 2;
        for (int x = 0; x <= 6; x++)
            for (int z = 0; z <= 6; z++)
                if (x == 3 && z == 3) s.addBlock(3, cap, 3, "solarpunk:turbine_rotor", "lit", "false");
                else                   s.addBlock(x, cap, z, "solarpunk:turbine_casing");
        return s;
    }

    // Same idea as addTurbineLayers, but grown along the X axis (East/West) instead of
    // Y: x=0 is left empty (matching the reserved y=0 ground row the vertical schematics
    // use), floor at x=1, `bladeLayers` blade layers (x=2..bladeLayers+1), cap at
    // x=bladeLayers+2. The 7x7 ring now lies in the Y-Z plane at each X layer, and blades
    // sit on the Up/Down/North/South arms (the 4 directions perpendicular to X) instead
    // of East/South/West/North.
    private static SceneStructure addHorizontalTurbineLayers(SceneStructure s, int bladeLayers, boolean allBrass) {
        String blade = allBrass ? "solarpunk:brass_turbine_blade" : "solarpunk:andesite_turbine_blade";
        // Floor layer (x=1): full 7x7 (y,z) casings, no rotor.
        for (int y = 0; y <= 6; y++)
            for (int z = 0; z <= 6; z++)
                s.addBlock(1, y, z, "solarpunk:turbine_casing");
        // Blade layers (x=2 to x=bladeLayers+1).
        for (int x = 2; x <= bladeLayers + 1; x++) {
            for (int y = 0; y <= 6; y++)
                for (int z = 0; z <= 6; z++)
                    if (y == 0 || y == 6 || z == 0 || z == 6) {
                        boolean isCorner = (y == 0 || y == 6) && (z == 0 || z == 6);
                        String wall = isCorner ? "solarpunk:turbine_casing" : "solarpunk:turbine_casing_glass";
                        s.addBlock(x, y, z, wall);
                    }
            s.addBlock(x, 3, 3, "solarpunk:turbine_rotor", "lit", "false", "axis", "x");
            s.addBlock(x, 4, 3, blade, "facing", "up",    "hidden", "false", "axis", "x");
            s.addBlock(x, 2, 3, blade, "facing", "down",  "hidden", "false", "axis", "x");
            s.addBlock(x, 3, 4, blade, "facing", "south", "hidden", "false", "axis", "x");
            s.addBlock(x, 3, 2, blade, "facing", "north", "hidden", "false", "axis", "x");
        }
        // Top cap (x=bladeLayers+2): full 7x7 casings + rotor at center.
        int cap = bladeLayers + 2;
        for (int y = 0; y <= 6; y++)
            for (int z = 0; z <= 6; z++)
                if (y == 3 && z == 3) s.addBlock(cap, y, z, "solarpunk:turbine_rotor", "lit", "false", "axis", "x");
                else                   s.addBlock(cap, y, z, "solarpunk:turbine_casing");
        return s;
    }

    // Adds a 3×3×3 tower footprint (x=1-3, z=1-3, y=1-3) with correct position states.
    // Footprint x=5-7/z=5-7/y=1-3 - pushed to the back corner of the plate so the
    // mirror field (in front, toward lower x/z) isn't hidden behind it.
    private static SceneStructure addTowerLayers(SceneStructure s) {
        for (int x = 5; x <= 7; x++)
            for (int z = 5; z <= 7; z++) {
                s.addBlock(x, 1, z, "solarpunk:solar_power_tower", "lit", "false", "position", "bottom");
                s.addBlock(x, 2, z, "solarpunk:solar_power_tower", "lit", "false", "position", "middle");
                s.addBlock(x, 3, z, "solarpunk:solar_power_tower", "lit", "false", "position", "top");
            }
        return s;
    }

    // A ground-mounted Solar Mirror is two cells tall: a lower half (base/post/plate,
    // facing=up) and a bare upper half (see SolarMirrorBlock) reserving the space the
    // post and plate poke up into.
    private static SceneStructure addGroundMirror(SceneStructure s, int x, int z) {
        s.addBlock(x, 1, z, "solarpunk:solar_mirror", "facing", "up", "half", "lower");
        s.addBlock(x, 2, z, "solarpunk:solar_mirror", "facing", "up", "half", "upper");
        return s;
    }

    // -------------------------------------------------------------------------
    // Structure builder
    // -------------------------------------------------------------------------

    private static class SceneStructure {
        private final List<CompoundTag> palette = new ArrayList<>();
        private final List<CompoundTag> blocks = new ArrayList<>();
        private final Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        private final int sizeX, sizeY, sizeZ;

        SceneStructure() { this(5, 8, 5); }

        SceneStructure(int sizeX, int sizeY, int sizeZ) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        SceneStructure withBasePlate() {
            int idx = getOrAddPalette("minecraft:smooth_stone", Map.of());
            for (int x = 0; x < sizeX; x++)
                for (int z = 0; z < sizeZ; z++)
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
            size.add(IntTag.valueOf(sizeX));
            size.add(IntTag.valueOf(sizeY));
            size.add(IntTag.valueOf(sizeZ));
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