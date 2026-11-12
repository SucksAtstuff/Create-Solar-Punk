package net.succ.solar_punk.datagen;

import net.succ.solar_punk.compat.ponder.FermentationVatScenes;
import net.succ.solar_punk.compat.ponder.ParticleAcceleratorScenes;
import net.minecraft.core.BlockPos;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;

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
        // Like Create's own mixer scene: a Basin, a one-block gap, then the Mechanical Mixer, turned
        // by a cogwheel beside it (a mixer has no shaft input). The gasifier sits on that cogwheel
        // on an Andesite Casing pillar, fed by a Hopper (with a Chest on it) on a pillar to its
        // west. See BiomassGasifierScenes.
        SceneStructure gasifier = new SceneStructure(5, 6, 5).withBasePlate()
                .addBlock(2, 1, 2, "create:basin", "facing", "down")
                .addBlock(2, 3, 2, "create:mechanical_mixer")
                .addBlock(1, 1, 2, "create:andesite_casing")
                .addBlock(1, 2, 2, "create:andesite_casing")
                .addBlock(1, 3, 2, "create:cogwheel", "axis", "y")
                .addBlock(1, 4, 2, "solarpunk:biomass_gasifier", "facing", "north", "lit", "false")
                .addBlock(0, 1, 2, "create:andesite_casing")
                .addBlock(0, 2, 2, "create:andesite_casing")
                .addBlock(0, 3, 2, "create:andesite_casing")
                .addBlock(0, 4, 2, "minecraft:hopper", "facing", "east", "enabled", "true")
                .addBlock(0, 5, 2, "minecraft:chest", "facing", "north", "type", "single");
        SCHEMATICS.put("biomass_gasifier/usage", gasifier);

        // Engine on a Gearbox, a shaft east into a Mechanical Drill facing a block of Andesite,
        // and a fuel pipe into the engine's west side. See BiofuelEngineScenes.
        SceneStructure engine = new SceneStructure().withBasePlate()
                .addBlock(1, 1, 2, "create:gearbox", "axis", "z")
                .addBlock(1, 2, 2, "solarpunk:biofuel_engine", "facing", "north", "lit", "false")
                .addBlock(2, 1, 2, "create:shaft", "axis", "x")
                .addBlock(3, 1, 2, "create:mechanical_drill", "facing", "east")
                .addBlock(4, 1, 2, "minecraft:andesite");
        addPipe(engine, 0, 2, 2, "east", "west");
        SCHEMATICS.put("biofuel_engine/usage", engine);

        // Chest -> Hopper -> Solar Heater -> pipe -> Heat Battery, along x at z=2. Shared with
        // the Heat Battery's filling scene. See SolarHeaterScenes / HeatBatteryScenes.
        SceneStructure saltLine = new SceneStructure().withBasePlate()
                .addBlock(0, 1, 2, "minecraft:hopper", "facing", "east", "enabled", "true")
                .addBlock(0, 2, 2, "minecraft:chest", "facing", "north", "type", "single")
                .addBlock(1, 1, 2, "solarpunk:solar_heater", "facing", "north", "lit", "false")
                .addBlock(3, 1, 2, "solarpunk:heat_battery", "heat", "0");
        addPipe(saltLine, 2, 1, 2, "east", "west");
        SCHEMATICS.put("solar_heater/usage", saltLine);

        // A water pipe into a Solar Heater standing on a Hopper that empties into a Chest
        SceneStructure evaporation = new SceneStructure().withBasePlate()
                .addBlock(2, 1, 2, "minecraft:hopper", "facing", "south", "enabled", "true")
                .addBlock(2, 1, 3, "minecraft:chest", "facing", "north", "type", "single")
                .addBlock(2, 2, 2, "solarpunk:solar_heater", "facing", "north", "lit", "false");
        addPipe(evaporation, 0, 2, 2, "east", "west");
        addPipe(evaporation, 1, 2, 2, "east", "west");
        SCHEMATICS.put("solar_heater/evaporation", evaporation);

        SCHEMATICS.put("greenhouse/usage", addGreenhouse(new SceneStructure().withBasePlate()));

        // Crystallizer on a Hopper emptying into a Chest, with Molten Salt piped in from the west,
        // Water from the north and Steam out to the east. See CrystallizerScenes.
        SceneStructure crystallizer = new SceneStructure().withBasePlate()
                .addBlock(2, 1, 2, "minecraft:hopper", "facing", "south", "enabled", "true")
                .addBlock(2, 1, 3, "minecraft:chest", "facing", "north", "type", "single")
                .addBlock(2, 2, 2, "solarpunk:crystallizer", "facing", "north", "lit", "false");
        for (int x : new int[]{0, 1}) addPipe(crystallizer, x, 2, 2, "east", "west");
        for (int z : new int[]{0, 1}) addPipe(crystallizer, 2, 2, z, "north", "south");
        for (int x : new int[]{3, 4}) addPipe(crystallizer, x, 2, 2, "east", "west");
        SCHEMATICS.put("crystallizer/usage", crystallizer);

        // Electrolyzer (facing north) with its Brass Solar Panel behind it, a pipe out of each
        // flange (Hydrogen east, Oxygen west) and a Water pipe coming down into its top.
        SceneStructure electrolyzer = new SceneStructure().withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:electrolyzer", "facing", "north", "lit", "false")
                .addBlock(2, 1, 3, "solarpunk:brass_solar_panel", "lit", "false");
        for (int x : new int[]{0, 1, 3, 4}) addPipe(electrolyzer, x, 1, 2, "east", "west");
        for (int y : new int[]{2, 3}) addPipe(electrolyzer, 2, y, 2, "up", "down");
        SCHEMATICS.put("electrolyzer/usage", electrolyzer);

        // Three Andesite Solar Panels on a row of meshing cogwheels driving a Millstone
        SceneStructure andesitePanels = new SceneStructure().withBasePlate();
        for (int x = 0; x <= 2; x++) {
            andesitePanels.addBlock(x, 1, 2, "create:cogwheel", "axis", "y");
            andesitePanels.addBlock(x, 2, 2, "solarpunk:andesite_solar_panel", "lit", "false");
        }
        andesitePanels.addBlock(3, 1, 2, "create:millstone");
        SCHEMATICS.put("solar_panel/andesite", andesitePanels);

        // Three Brass Solar Panels (z=1): an Electric Motor touching the east one drives a shaft
        // into an Encased Fan, and a Wire Connector on the middle one's south side is wired to one
        // on the back of an Electrolyzer (its top and flange sides are pipe ports). See SolarPanelScenes.
        SceneStructure brassPanels = new SceneStructure().withBasePlate();
        for (int x = 0; x <= 2; x++) brassPanels.addBlock(x, 1, 1, "solarpunk:brass_solar_panel", "lit", "false");
        brassPanels.addBlock(3, 1, 1, "solarpunk:electric_motor", "facing", "south", "lit", "false")
                .addBlock(3, 1, 2, "create:shaft", "axis", "z")
                .addBlock(3, 1, 3, "create:shaft", "axis", "z")
                .addBlock(3, 1, 4, "create:encased_fan", "facing", "south")
                .addBlock(1, 1, 2, "solarpunk:wire_connector", "facing", "south")
                .addBlock(0, 1, 3, "solarpunk:electrolyzer", "facing", "north", "lit", "false")
                .addBlock(0, 1, 4, "solarpunk:wire_connector", "facing", "south");
        SCHEMATICS.put("solar_panel/brass", brassPanels);

        SCHEMATICS.put("heat_battery/filling", saltLine);

        SCHEMATICS.put("heat_battery/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:heat_battery", "heat", "2"));

        // Battery along Z. Charging side (south): a Gearbox (axis x, so it joins north and up)
        // under an Andesite Solar Panel. Discharge side (north): a shaft into an Encased Fan.
        SCHEMATICS.put("kinetic_battery/usage", new SceneStructure()
                .withBasePlate()
                .addBlock(2, 1, 2, "solarpunk:kinetic_battery", "axis", "z", "lit", "false")
                .addBlock(2, 1, 3, "create:gearbox", "axis", "x")
                .addBlock(2, 2, 3, "solarpunk:andesite_solar_panel", "lit", "false")
                .addBlock(2, 1, 1, "create:shaft", "axis", "z")
                .addBlock(2, 1, 0, "create:encased_fan", "facing", "north"));

        // cap facing=north -> rotation axis=X -> shafts connect on east/west (x axis)
        // East: a Mechanical Press over a Depot set into the floor (the gap a press needs).
        // West: an Encased Fan.
        SCHEMATICS.put("geyser_cap/usage", new SceneStructure()
                .withBasePlateExcept(4, 2)
                .addBlock(4, 0, 2, "create:depot")
                .addBlock(2, 1, 2, "solarpunk:geyser_vent")
                .addBlock(2, 2, 2, "solarpunk:geyser_cap", "facing", "north", "lit", "false")
                .addBlock(1, 2, 2, "create:shaft", "axis", "x")
                .addBlock(3, 2, 2, "create:shaft", "axis", "x")
                .addBlock(4, 2, 2, "create:mechanical_press", "facing", "east")
                .addBlock(0, 2, 2, "create:encased_fan", "facing", "west"));

        // 2x2x4, the smallest vat that ferments, and a 3x3x6 one to show scaling.
        // Sizes live in FermentationVatScenes so the scene and schematic can't drift apart.
        SCHEMATICS.put("fermentation_vat/usage", addVatLayers(new SceneStructure().withBasePlate(),
                FermentationVatScenes.USAGE_WIDTH, FermentationVatScenes.USAGE_HEIGHT));
        SCHEMATICS.put("fermentation_vat/scaling", addVatLayers(new SceneStructure().withBasePlate(),
                FermentationVatScenes.SCALING_WIDTH, FermentationVatScenes.SCALING_HEIGHT));

        // 20-block octagon ring around a 10x10 plate, laid out in ParticleAcceleratorScenes.RING
        SCHEMATICS.put("particle_accelerator/ring", addAcceleratorRing(new SceneStructure(10, 3, 10)));
        SCHEMATICS.put("particle_accelerator/crafting", addAcceleratorRing(new SceneStructure(10, 3, 10)));

        // floor + 3 blade layers + cap = 5 total layers; 7x7x7 schematic
        SCHEMATICS.put("turbine_rotor/structure",
                addTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));
        SCHEMATICS.put("turbine_rotor/condensate",
                addTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));

        // floor + 20 blade layers + cap = 22 total layers; 7x24x7 schematic
        SCHEMATICS.put("turbine_rotor/max_turbine",
                addTurbineLayers(new SceneStructure(7, 24, 7).withBasePlate(), 20, true));

        // Same size as turbine_rotor/structure, but grown along the X axis instead of Y:
        // floor(x=1) + 3 blade layers(x=2-4) + cap(x=5); 7x7x7 schematic.
        SCHEMATICS.put("turbine_rotor/horizontal",
                addHorizontalTurbineLayers(new SceneStructure(7, 7, 7).withBasePlate(), 3, false));

        // Tower in the back corner with the mirror field spread out on the ground to its
        // west and north, in front of the tower so it doesn't block them from view.
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

        // Reactor centered at (4,5,4) in a 9x10x9 box (y=0 is the ground row), built with
        // the same shape rules as FusionReactorCoreBlockEntity's structure scan.
        SCHEMATICS.put("fusion_reactor/structure",
                addReactorShell(new SceneStructure(9, 10, 9).withBasePlate()));

        SCHEMATICS.put("grow_lamp/usage", addGrowLampRoom(new SceneStructure(5, 7, 5).withBasePlate()));
        SCHEMATICS.put("kinetic_sprinkler/usage", addSprinklerField(new SceneStructure(7, 5, 7)));
        SCHEMATICS.put("firebox_boiler/usage", addFireboxRow(new SceneStructure(5, 4, 5).withBasePlate()));
        SCHEMATICS.put("electric_motor/usage", addMotorPress(new SceneStructure(5, 5, 5).withBasePlate()));
        SCHEMATICS.put("biofilter/usage", addBiofilterWasteland(new SceneStructure(5, 4, 5)));
        SCHEMATICS.put("fuel_cell/usage", addFuelCellStack(new SceneStructure(7, 5, 5).withBasePlate()));
        SCHEMATICS.put("wire_connector/usage", addWireNetwork(new SceneStructure(7, 4, 5).withBasePlate()));
        SCHEMATICS.put("wire_switch/usage", addSwitchLine(new SceneStructure(7, 4, 5).withBasePlate(), false));
        SCHEMATICS.put("wire_switch/relay", addSwitchLine(new SceneStructure(7, 4, 5).withBasePlate(), true));
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

    // A 3x3 wheat field on farmland, Greenhouse Glass walls (y 1-3) with the controller in the
    // north wall facing in, and a Greenhouse Glass roof (y 4). See GreenhouseScenes.
    private static SceneStructure addGreenhouse(SceneStructure s) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                boolean edge = x == 0 || x == 4 || z == 0 || z == 4;
                if (edge) {
                    for (int y = 1; y <= 3; y++) {
                        if (x == 2 && y == 2 && z == 0) s.addBlock(x, y, z, "solarpunk:greenhouse_controller", "facing", "south");
                        else s.addBlock(x, y, z, "solarpunk:greenhouse_glass");
                    }
                } else {
                    s.addBlock(x, 1, z, "minecraft:farmland", "moisture", "7");
                    s.addBlock(x, 2, z, "minecraft:wheat", "age", "1"); // planted again by GreenhouseScenes
                }
                s.addBlock(x, 4, z, "solarpunk:greenhouse_glass");
            }
        }
        return s;
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

    // Adds a 3x3x3 tower at x=5-7, z=5-7, y=1-3 with correct position states. It sits in
    // the back corner of the plate so the mirror field in front of it isn't hidden.
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

    // The scenes' octagon ring on a plate, with a shaft in the floor under the Input.
    private static SceneStructure addAcceleratorRing(SceneStructure s) {
        BlockPos shaft = ParticleAcceleratorScenes.SHAFT;
        s.withBasePlateExcept(shaft.getX(), shaft.getZ());
        s.addBlock(shaft.getX(), shaft.getY(), shaft.getZ(), "create:shaft", "axis", "y");
        for (ParticleAcceleratorScenes.Piece piece : ParticleAcceleratorScenes.RING)
            s.addBlock(piece.x(), 1, piece.z(), "solarpunk:" + piece.block(), "facing", piece.facing());
        return s;
    }

    // Builds the reactor centered at local (4,5,4), using FusionReactorCoreBlockEntity's
    // isCasingPosition()/isBlanketPosition(). The shell is solid Casing with a Casing
    // Glass window in the corner facing the default Ponder camera (dx+dz <= -5). The
    // Blanket is split by hemisphere (Beryllium on +X, Lithium on -X) so the scene shows
    // a mixed ratio.
    private static SceneStructure addReactorShell(SceneStructure s) {
        int r = 4;
        for (int dx = -r; dx <= r; dx++)
            for (int dy = -r; dy <= r; dy++)
                for (int dz = -r; dz <= r; dz++) {
                    int x = dx + 4, y = dy + 5, z = dz + 4;
                    if (FusionReactorCoreBlockEntity.isCasingPosition(dx, dy, dz)) {
                        String block = (dx + dz <= -5) ? "solarpunk:fusion_reactor_casing_glass" : "solarpunk:fusion_reactor_casing";
                        s.addBlock(x, y, z, block);
                    } else if (FusionReactorCoreBlockEntity.isBlanketPosition(dx, dy, dz)) {
                        String module = dx >= 0 ? "solarpunk:beryllium_reflector_module" : "solarpunk:lithium_breeder_module";
                        s.addBlock(x, y, z, module);
                    }
                }
        s.addBlock(4, 5, 4, "solarpunk:fusion_reactor_core");
        return s;
    }

    // A Create fluid pipe with the given sides connected.
    private static SceneStructure addPipe(SceneStructure s, int x, int y, int z, String... sides) {
        java.util.Set<String> open = java.util.Set.of(sides);
        String[] props = new String[12];
        String[] all = {"north", "south", "east", "west", "up", "down"};
        for (int i = 0; i < all.length; i++) {
            props[i * 2] = all[i];
            props[i * 2 + 1] = String.valueOf(open.contains(all[i]));
        }
        return s.addBlock(x, y, z, "create:fluid_pipe", props);
    }

    // A 3x3 carrot bed between two log posts, three Grow Lamps hung post to post above it, and a
    // Brass Solar Panel on each post wired to a Wire Connector on every lamp. See GrowLampScenes.
    private static SceneStructure addGrowLampRoom(SceneStructure s) {
        for (int x = 1; x <= 3; x++)
            for (int z = 1; z <= 3; z++) {
                // The scene plants these again on start (GrowLampScenes.plant()), like Create's
                // harvester scene does: crops can drop out while Ponder loads the schematic
                s.addBlock(x, 1, z, "minecraft:farmland", "moisture", "7");
                s.addBlock(x, 2, z, "minecraft:carrots", "age", "0");
            }
        for (int x : new int[]{0, 4}) {
            for (int y = 1; y <= 4; y++) s.addBlock(x, y, 2, "minecraft:stripped_spruce_log", "axis", "y");
            s.addBlock(x, 5, 2, "solarpunk:brass_solar_panel", "lit", "false");
            // On the panel's side, so it doesn't shade it
            s.addBlock(x, 5, 1, "solarpunk:wire_connector", "facing", "north");
        }
        for (int x = 1; x <= 3; x++) {
            s.addBlock(x, 4, 2, "solarpunk:grow_lamp", "axis", "x", "ceiling", "false", "lit", "false");
            s.addBlock(x, 5, 2, "solarpunk:wire_connector", "facing", "up");
        }
        return s;
    }

    // A 7x7 wheat field on dry farmland, with the sprinkler on a pipe rising out of the floor
    // in the middle. See KineticSprinklerScenes.
    private static SceneStructure addSprinklerField(SceneStructure s) {
        s.withBasePlateExcept(3, 3);
        for (int y = 0; y <= 2; y++) addPipe(s, 3, y, 3, "up", "down");
        s.addBlock(3, 3, 3, "solarpunk:kinetic_sprinkler");
        for (int x = 0; x < 7; x++)
            for (int z = 0; z < 7; z++) {
                if (x == 3 && z == 3) continue;
                s.addBlock(x, 1, z, "minecraft:farmland", "moisture", "0");
                s.addBlock(x, 2, z, "minecraft:wheat", "age", "0"); // planted again by the scene
            }
        return s;
    }

    // Three Firebox Boilers in a row, a water line along the back and a steam line along the top.
    private static SceneStructure addFireboxRow(SceneStructure s) {
        for (int x = 1; x <= 3; x++) {
            s.addBlock(x, 1, 2, "solarpunk:firebox_boiler", "facing", "north", "lit", "false");
            if (x == 1) {
                addPipe(s, x, 1, 3, "north", "east");
                addPipe(s, x, 2, 2, "down", "east");
            } else {
                addPipe(s, x, 1, 3, "north", "east", "west");
                addPipe(s, x, 2, 2, "down", "east", "west");
            }
        }
        addPipe(s, 4, 1, 3, "east", "west");
        addPipe(s, 4, 2, 2, "east", "west");
        return s;
    }

    // A Brass Solar Panel and an Electric Motor on two-high Andesite Casing pillars, the motor
    // turning a Mechanical Press with the one-block gap it needs above a Depot.
    private static SceneStructure addMotorPress(SceneStructure s) {
        for (int y = 1; y <= 2; y++) {
            s.addBlock(0, y, 2, "create:andesite_casing");
            s.addBlock(1, y, 2, "create:andesite_casing");
        }
        s.addBlock(0, 3, 2, "solarpunk:brass_solar_panel", "lit", "false");
        s.addBlock(1, 3, 2, "solarpunk:electric_motor", "facing", "east", "lit", "false");
        s.addBlock(2, 3, 2, "create:shaft", "axis", "x");
        s.addBlock(3, 3, 2, "create:mechanical_press", "facing", "east");
        s.addBlock(3, 1, 2, "create:depot");
        return s;
    }

    // Polluted ground (Dead Grass Blocks, Ruined Dirt, Dead Grass) with a lit Firebox Boiler in
    // the back corner, the Biofilter on a shaft coming up through the floor, and a Comparator
    // reading it into a Redstone Lamp. See BiofilterScenes.
    private static SceneStructure addBiofilterWasteland(SceneStructure s) {
        int floor = 0;
        for (int x = 0; x < 5; x++)
            for (int z = 0; z < 5; z++) {
                if (x == 2 && z == 2) continue;
                String block = (x * 3 + z * 7) % 5 == 0 ? "solarpunk:ruined_dirt" : "solarpunk:dead_grass_block";
                s.addBlock(x, floor, z, block);
            }
        s.addBlock(2, 0, 2, "create:shaft", "axis", "y");
        s.addBlock(2, 1, 2, "solarpunk:biofilter");
        s.addBlock(3, 1, 2, "minecraft:comparator", "facing", "west", "mode", "compare", "powered", "false");
        s.addBlock(4, 1, 2, "minecraft:redstone_lamp", "lit", "false");
        s.addBlock(4, 1, 4, "solarpunk:firebox_boiler", "facing", "north", "lit", "true");
        for (int[] p : new int[][]{{0, 0}, {1, 3}, {0, 4}, {3, 0}, {4, 0}, {2, 4}})
            s.addBlock(p[0], 1, p[1], "solarpunk:dead_grass");
        return s;
    }

    // A three-cell stack along X: the controller plate (facing west) at x=1, cells x=2-4 and the
    // other plate (facing east) at x=5. Hydrogen comes into the west plate's port (north),
    // Oxygen into the east plate's (south) and Water leaves the east plate's other port (north).
    // A Wire Connector on the middle cell feeds a Streetlamp behind it. See FuelCellScenes.
    private static SceneStructure addFuelCellStack(SceneStructure s) {
        s.addBlock(1, 1, 2, "solarpunk:end_plate", "facing", "west", "lit", "false");
        for (int x = 2; x <= 4; x++) s.addBlock(x, 1, 2, "solarpunk:fuel_cell", "axis", "x");
        s.addBlock(5, 1, 2, "solarpunk:end_plate", "facing", "east", "lit", "false");
        for (int z : new int[]{0, 1}) {
            addPipe(s, 1, 1, z, "north", "south");
            addPipe(s, 5, 1, z, "north", "south");
        }
        for (int z : new int[]{3, 4}) addPipe(s, 5, 1, z, "north", "south");
        s.addBlock(3, 1, 4, "solarpunk:streetlamp", "half", "lower", "lit", "false");
        s.addBlock(3, 2, 4, "solarpunk:streetlamp", "half", "upper", "lit", "false");
        s.addBlock(3, 2, 2, "solarpunk:wire_connector", "facing", "up");
        s.addBlock(3, 3, 4, "solarpunk:wire_connector", "facing", "up");
        return s;
    }

    // A 2x3 Brass Solar Panel field at the back with a Wire Connector on it, and two Electric
    // Motors at the front, each with a connector and a shaft. See WireScenes.
    private static SceneStructure addWireNetwork(SceneStructure s) {
        for (int x = 5; x <= 6; x++)
            for (int z = 1; z <= 3; z++)
                s.addBlock(x, 1, z, "solarpunk:brass_solar_panel", "lit", "false");
        s.addBlock(4, 1, 2, "solarpunk:wire_connector", "facing", "west"); // on the side, not shading the panels
        for (int z : new int[]{1, 3}) {
            s.addBlock(1, 1, z, "solarpunk:electric_motor", "facing", "west", "lit", "false");
            s.addBlock(0, 1, z, "create:shaft", "axis", "x");
            s.addBlock(1, 2, z, "solarpunk:wire_connector", "facing", "up");
        }
        return s;
    }

    // Panels at the back (x=6) wired through a Wire Switch, or a Redstone Wire Relay with a Lever
    // beside it, on the floor at (3,1,2) to an Electric Motor at the front. See WireScenes.
    private static SceneStructure addSwitchLine(SceneStructure s, boolean relay) {
        for (int z = 1; z <= 3; z++) s.addBlock(6, 1, z, "solarpunk:brass_solar_panel", "lit", "false");
        s.addBlock(5, 1, 2, "solarpunk:wire_connector", "facing", "west"); // on the side, not shading the panels
        if (relay) {
            s.addBlock(3, 1, 2, "solarpunk:redstone_wire_relay",
                    "face", "floor", "facing", "east", "closed", "false", "inverted", "false");
            s.addBlock(3, 1, 1, "minecraft:lever", "face", "floor", "facing", "east", "powered", "false");
        } else {
            s.addBlock(3, 1, 2, "solarpunk:wire_switch", "face", "floor", "facing", "east", "closed", "false");
        }
        s.addBlock(1, 1, 2, "solarpunk:electric_motor", "facing", "west", "lit", "false");
        s.addBlock(0, 1, 2, "create:shaft", "axis", "x");
        s.addBlock(1, 2, 2, "solarpunk:wire_connector", "facing", "up");
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
            return withBasePlateExcept(-1, -1);
        }

        // A plate with one hole, for something set into the floor (e.g. a shaft)
        SceneStructure withBasePlateExcept(int holeX, int holeZ) {
            int idx = getOrAddPalette("minecraft:smooth_stone", Map.of());
            for (int x = 0; x < sizeX; x++)
                for (int z = 0; z < sizeZ; z++)
                    if (x != holeX || z != holeZ) placeBlock(idx, x, 0, z);
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