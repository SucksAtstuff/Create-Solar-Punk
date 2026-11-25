package net.succ.solar_punk.worldgen.structure.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * A village built from a folder of plain templates, with no jigsaw blocks. Streets grow out from
 * the centre in straight runs that turn and branch; houses go along both sides with their door
 * (found in the template) facing the street, at the ground height in front of the door. Every
 * template directly in {@code houses} is used, so adding a house is adding a file.
 *
 * <pre>
 * "type": "solarpunk:simple_village",
 * "houses": "solarpunk:abandoned_village",          every .nbt in that folder
 * "center": "solarpunk:abandoned_village/center",   optional: one of these goes in the middle
 * "house_count": {"type": "uniform", ...},           default 4-8
 * "radius": 56,                                      how far from the centre anything goes
 * "street_width": 3,
 * "street": a block state provider,                  default Dirt Path
 * "street_on_water": a block state provider,         optional: bridges, else gaps
 * "max_slope": 4                                     ground height difference a house tolerates
 * </pre>
 * The structure's {@code terrain_adaptation} must not be {@code none}: houses always beard the
 * terrain under them, but Minecraft skips structures with none.
 */
public class SimpleVillageStructure extends Structure {
    public static final MapCodec<SimpleVillageStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            ResourceLocation.CODEC.fieldOf("houses").forGetter(s -> s.houses),
            ResourceLocation.CODEC.optionalFieldOf("center").forGetter(s -> s.center),
            IntProvider.codec(1, 64).optionalFieldOf("house_count", UniformInt.of(4, 8)).forGetter(s -> s.houseCount),
            Codec.intRange(16, 96).optionalFieldOf("radius", 56).forGetter(s -> s.radius),
            Codec.intRange(1, 7).optionalFieldOf("street_width", 3).forGetter(s -> s.streetWidth),
            BlockStateProvider.CODEC.optionalFieldOf("street", BlockStateProvider.simple(Blocks.DIRT_PATH)).forGetter(s -> s.street),
            BlockStateProvider.CODEC.optionalFieldOf("street_on_water").forGetter(s -> s.bridge),
            Codec.intRange(0, 16).optionalFieldOf("max_slope", 4).forGetter(s -> s.maxSlope)
    ).apply(i, SimpleVillageStructure::new));

    private static final int STREET_STEP = 2;   // a street climbs at most this much per sample
    private static final int SAMPLE_EVERY = 3;  // street cells between terrain samples
    private static final int STREET_PER_HOUSE = 10; // street cells grown per wanted house

    private final ResourceLocation houses;
    private final Optional<ResourceLocation> center;
    private final IntProvider houseCount;
    private final int radius;
    private final int streetWidth;
    private final BlockStateProvider street;
    private final Optional<BlockStateProvider> bridge;
    private final int maxSlope;

    public SimpleVillageStructure(StructureSettings settings, ResourceLocation houses, Optional<ResourceLocation> center,
                                  IntProvider houseCount, int radius, int streetWidth, BlockStateProvider street,
                                  Optional<BlockStateProvider> bridge, int maxSlope) {
        super(settings);
        this.houses = houses;
        this.center = center;
        this.houseCount = houseCount;
        this.radius = radius;
        this.streetWidth = streetWidth;
        this.street = street;
        this.bridge = bridge;
        this.maxSlope = maxSlope;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        Terrain terrain = new Terrain(context);
        if (terrain.water(x, z)) return Optional.empty();
        int y = terrain.ground(x, z);
        return Optional.of(new GenerationStub(new BlockPos(x, y, z), builder -> {
            List<StructurePiece> pieces = new Layout(context, terrain, x, y, z).build();
            pieces.forEach(builder::addPiece);
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SIMPLE_VILLAGE.get();
    }

    /** Ground heights from the noise (no blocks exist yet), cached since streets ask often. */
    private static final class Terrain {
        private final GenerationContext context;
        private final Long2IntOpenHashMap surface = new Long2IntOpenHashMap();
        private final Long2IntOpenHashMap floor = new Long2IntOpenHashMap();

        Terrain(GenerationContext context) {
            this.context = context;
        }

        /** Y of the top block, water included. */
        int ground(int x, int z) {
            return surface.computeIfAbsent(ChunkPos.asLong(x, z), k -> height(x, z, Heightmap.Types.WORLD_SURFACE_WG));
        }

        boolean water(int x, int z) {
            int bottom = floor.computeIfAbsent(ChunkPos.asLong(x, z), k -> height(x, z, Heightmap.Types.OCEAN_FLOOR_WG));
            return ground(x, z) > bottom;
        }

        private int height(int x, int z, Heightmap.Types type) {
            return context.chunkGenerator().getFirstOccupiedHeight(x, z, type, context.heightAccessor(), context.randomState());
        }
    }

    /** A straight run of street: {@code length} cells along its centre line from (x, z). */
    private record Segment(int x, int z, Direction dir, int length, BoundingBox box) {
        int cx(int t) {
            return x + dir.getStepX() * t;
        }

        int cz(int t) {
            return z + dir.getStepZ() * t;
        }
    }

    private record Branch(int x, int z, Direction dir, int depth, BoundingBox parent) {}

    private final class Layout {
        private final GenerationContext context;
        private final Terrain terrain;
        private final RandomSource random;
        private final int x, y, z;
        private final int lo, hi; // street cells to the left (counter-clockwise) and right of the centre line
        private final List<StructurePiece> pieces = new ArrayList<>();
        private final List<BoundingBox> occupied = new ArrayList<>();
        private final List<Segment> segments = new ArrayList<>();

        Layout(GenerationContext context, Terrain terrain, int x, int y, int z) {
            this.context = context;
            this.terrain = terrain;
            this.random = context.random();
            this.x = x;
            this.y = y;
            this.z = z;
            this.lo = (streetWidth - 1) / 2;
            this.hi = streetWidth - 1 - lo;
        }

        List<StructurePiece> build() {
            List<VillageTemplates.Piece> houseTemplates = VillageTemplates.folder(context.structureTemplateManager(), houses);
            if (houseTemplates.isEmpty()) return List.of();

            BoundingBox hub = placeCenter();
            int target = houseCount.sample(random);
            growStreets(hub, target);
            int placed = placeHouses(houseTemplates, target);
            return placed > 0 ? pieces : List.of();
        }

        /** The centre piece, or a small square of street when there is none. */
        private BoundingBox placeCenter() {
            List<VillageTemplates.Piece> centers = center
                    .map(folder -> VillageTemplates.folder(context.structureTemplateManager(), folder))
                    .orElse(List.of());
            if (!centers.isEmpty()) {
                VillageTemplates.Piece piece = centers.get(random.nextInt(centers.size()));
                Rotation rotation = Rotation.getRandom(random);
                BoundingBox atZero = piece.template().getBoundingBox(settings(rotation), BlockPos.ZERO);
                BlockPos mid = atZero.getCenter();
                BlockPos origin = new BlockPos(x - mid.getX(), y - piece.floor(), z - mid.getZ());
                VillageHousePiece house = new VillageHousePiece(context.structureTemplateManager(), piece.id(), origin, rotation, piece.floor());
                pieces.add(house);
                occupied.add(house.getBoundingBox());
                return house.getBoundingBox();
            }
            int r = lo + 1;
            BoundingBox plaza = new BoundingBox(x - r, y - 2, z - r, x + r, y + 2, z + r);
            pieces.add(new VillageStreetPiece(plaza, street, bridge.orElse(null)));
            occupied.add(plaza);
            return plaza;
        }

        private void growStreets(BoundingBox hub, int houses) {
            Deque<Branch> queue = new ArrayDeque<>();
            List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
            shuffle(dirs);
            int arms = 2 + random.nextInt(3);
            BlockPos mid = hub.getCenter();
            for (int i = 0; i < arms; i++) {
                Direction d = dirs.get(i);
                int sx = d.getStepX() > 0 ? hub.maxX() + 1 : d.getStepX() < 0 ? hub.minX() - 1 : mid.getX();
                int sz = d.getStepZ() > 0 ? hub.maxZ() + 1 : d.getStepZ() < 0 ? hub.minZ() - 1 : mid.getZ();
                queue.add(new Branch(sx, sz, d, 0, hub));
            }

            // About one street cell per block of house front on each side, so the streets fill
            // up instead of running on empty
            int budget = Math.min(radius * 4, Math.max(30, houses * STREET_PER_HOUSE)), total = 0;
            while (!queue.isEmpty() && total < budget) {
                Branch b = queue.poll();
                if (terrain.water(b.x(), b.z()) && bridge.isEmpty()) continue;
                int length = 8 + random.nextInt(11);
                int prevY = terrain.ground(b.x(), b.z()), minY = prevY, maxY = prevY;
                int ok = 0;
                for (int t = 0; t < length; t++) {
                    int px = b.x() + b.dir().getStepX() * t, pz = b.z() + b.dir().getStepZ() * t;
                    if (!inRadius(px, pz, 4)) break;
                    if (collides(cell(px, pz, b.dir()), b.parent())) break;
                    if (t % SAMPLE_EVERY == SAMPLE_EVERY - 1) {
                        if (terrain.water(px, pz) && bridge.isEmpty()) break;
                        int h = terrain.ground(px, pz);
                        if (Math.abs(h - prevY) > STREET_STEP) break;
                        prevY = h;
                        minY = Math.min(minY, h);
                        maxY = Math.max(maxY, h);
                    }
                    ok = t + 1;
                }
                if (ok < 4) continue;

                BoundingBox a = cell(b.x(), b.z(), b.dir());
                BoundingBox e = cell(b.x() + b.dir().getStepX() * (ok - 1), b.z() + b.dir().getStepZ() * (ok - 1), b.dir());
                BoundingBox box = new BoundingBox(Math.min(a.minX(), e.minX()), minY - 2, Math.min(a.minZ(), e.minZ()),
                        Math.max(a.maxX(), e.maxX()), maxY + 2, Math.max(a.maxZ(), e.maxZ()));
                pieces.add(new VillageStreetPiece(box, street, bridge.orElse(null)));
                occupied.add(box);
                Segment seg = new Segment(b.x(), b.z(), b.dir(), ok, box);
                segments.add(seg);
                total += ok;

                if (b.depth() >= 4) continue;
                if (ok == length) {
                    // Carry on straight, or turn at the end (the turn starts inside this street, filling the corner)
                    int ex = seg.cx(ok - 1), ez = seg.cz(ok - 1);
                    float r = random.nextFloat();
                    if (r < 0.5f) queue.add(new Branch(ex + b.dir().getStepX(), ez + b.dir().getStepZ(), b.dir(), b.depth() + 1, box));
                    else queue.add(new Branch(ex, ez, r < 0.75f ? b.dir().getClockWise() : b.dir().getCounterClockWise(), b.depth() + 1, box));
                }
                if (ok >= 10 && random.nextFloat() < 0.35f) {
                    int t = ok / 2;
                    Direction side = random.nextBoolean() ? b.dir().getClockWise() : b.dir().getCounterClockWise();
                    queue.add(new Branch(seg.cx(t), seg.cz(t), side, b.depth() + 1, box));
                }
            }
        }

        /**
         * Houses go along the streets, one per street side per round, so they spread over the
         * village instead of filling the first street.
         */
        private int placeHouses(List<VillageTemplates.Piece> templates, int target) {
            List<int[]> cursors = new ArrayList<>(); // segment index, side (0 right, 1 left), position
            for (int s = 0; s < segments.size(); s++) {
                cursors.add(new int[]{s, 0, 1 + random.nextInt(2)});
                cursors.add(new int[]{s, 1, 1 + random.nextInt(2)});
            }
            int placed = 0;
            int[] uses = new int[templates.size()];
            boolean progress = true;
            while (placed < target && progress) {
                progress = false;
                for (int[] c : cursors) {
                    if (placed >= target) break;
                    Segment seg = segments.get(c[0]);
                    Direction side = c[1] == 0 ? seg.dir().getClockWise() : seg.dir().getCounterClockWise();
                    while (c[2] < seg.length() - 1) {
                        progress = true;
                        int pick = leastUsed(uses);
                        VillageTemplates.Piece piece = templates.get(pick);
                        int advance = tryHouse(seg, side, c[2], piece);
                        if (advance > 0) {
                            c[2] += advance + 1 + random.nextInt(3);
                            placed++;
                            uses[pick]++;
                            break;
                        }
                        c[2] += 2;
                    }
                }
            }
            return placed;
        }

        /** A random template among those used least so far, so every house appears before any repeats. */
        private int leastUsed(int[] uses) {
            int min = Integer.MAX_VALUE, count = 0;
            for (int u : uses) {
                if (u < min) {
                    min = u;
                    count = 1;
                } else if (u == min) count++;
            }
            int n = random.nextInt(count);
            for (int i = 0; i < uses.length; i++) {
                if (uses[i] == min && n-- == 0) return i;
            }
            return 0;
        }

        /** Places a house beside the street at {@code t}; returns how far it reaches along the street, or 0. */
        private int tryHouse(Segment seg, Direction side, int t, VillageTemplates.Piece piece) {
            int edge = side == seg.dir().getClockWise() ? hi : lo;
            BlockPos gap = new BlockPos(seg.cx(t), 0, seg.cz(t)).relative(side, edge + 1);
            if (terrain.water(gap.getX(), gap.getZ())) return 0;
            int ground = terrain.ground(gap.getX(), gap.getZ());

            Rotation rotation = rotationFor(piece.front(), side.getOpposite());
            BlockPos door = gap.relative(side, 1 + piece.inset());
            BlockPos turned = StructureTemplate.transform(piece.door(), Mirror.NONE, rotation, BlockPos.ZERO);
            BlockPos origin = new BlockPos(door.getX() - turned.getX(), ground - piece.floor(), door.getZ() - turned.getZ());
            BoundingBox box = piece.template().getBoundingBox(settings(rotation), origin);

            if (!inRadius(box.minX(), box.minZ(), 0) || !inRadius(box.maxX(), box.maxZ(), 0)
                    || !inRadius(box.minX(), box.maxZ(), 0) || !inRadius(box.maxX(), box.minZ(), 0)) return 0;
            if (collides(box.inflatedBy(1), null)) return 0;
            if (!flatEnough(box)) return 0;

            pieces.add(new VillageHousePiece(context.structureTemplateManager(), piece.id(), origin, rotation, piece.floor()));
            occupied.add(box);
            // The cell between the door and the street
            BoundingBox path = new BoundingBox(gap.getX(), ground - 2, gap.getZ(), gap.getX(), ground + 2, gap.getZ());
            pieces.add(new VillageStreetPiece(path, street, bridge.orElse(null)));

            int along = seg.dir().getAxis() == Direction.Axis.X ? seg.cx(t) : seg.cz(t);
            return seg.dir().getAxisDirection() == Direction.AxisDirection.POSITIVE
                    ? (seg.dir().getAxis() == Direction.Axis.X ? box.maxX() : box.maxZ()) - along
                    : along - (seg.dir().getAxis() == Direction.Axis.X ? box.minX() : box.minZ());
        }

        private boolean flatEnough(BoundingBox box) {
            int[][] points = {{box.minX(), box.minZ()}, {box.maxX(), box.minZ()}, {box.minX(), box.maxZ()},
                    {box.maxX(), box.maxZ()}, {box.getCenter().getX(), box.getCenter().getZ()}};
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (int[] p : points) {
                if (terrain.water(p[0], p[1])) return false;
                int h = terrain.ground(p[0], p[1]);
                min = Math.min(min, h);
                max = Math.max(max, h);
            }
            return max - min <= maxSlope;
        }

        /** The street's cross-section at one centre-line cell. */
        private BoundingBox cell(int cx, int cz, Direction dir) {
            Direction right = dir.getClockWise();
            int ax = cx - right.getStepX() * lo, az = cz - right.getStepZ() * lo;
            int bx = cx + right.getStepX() * hi, bz = cz + right.getStepZ() * hi;
            return new BoundingBox(Math.min(ax, bx), 0, Math.min(az, bz), Math.max(ax, bx), 0, Math.max(az, bz));
        }

        private boolean collides(BoundingBox box, BoundingBox except) {
            for (BoundingBox o : occupied) {
                if (o != except && o.intersects(box.minX(), box.minZ(), box.maxX(), box.maxZ())) return true;
            }
            return false;
        }

        private boolean inRadius(int px, int pz, int margin) {
            int dx = px - x, dz = pz - z, r = radius - margin;
            return dx * dx + dz * dz <= r * r;
        }

        private void shuffle(List<Direction> list) {
            for (int i = list.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Direction tmp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, tmp);
            }
        }
    }

    private static StructurePlaceSettings settings(Rotation rotation) {
        return new StructurePlaceSettings().setRotation(rotation);
    }

    /** The rotation that turns a template's {@code from} edge to face {@code to}. */
    private static Rotation rotationFor(Direction from, Direction to) {
        for (Rotation r : Rotation.values()) {
            if (r.rotate(from) == to) return r;
        }
        return Rotation.NONE;
    }
}
