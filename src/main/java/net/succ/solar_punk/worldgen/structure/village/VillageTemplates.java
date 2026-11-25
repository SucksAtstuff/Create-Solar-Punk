package net.succ.solar_punk.worldgen.structure.village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds the templates in a structure folder and where each one's door is, so a village can be
 * built from plain .nbt files with no jigsaw blocks.
 */
final class VillageTemplates {
    private VillageTemplates() {}

    /**
     * A template and its front: the door (lower half, in template space), the edge of the
     * template it faces out of, and how far it's set back from that edge. A template with no door
     * gets a made-up one in the middle of its south edge, one block up.
     */
    record Piece(ResourceLocation id, StructureTemplate template, BlockPos door, Direction front, int inset) {
        /** Template Y of the floor, which goes at ground level (the layer the door stands on). */
        int floor() {
            return door.getY() - 1;
        }
    }

    // Listing a folder walks every structure file of every mod, so it's done once per folder
    private static final Map<StructureTemplateManager, Map<ResourceLocation, List<ResourceLocation>>> FOLDERS =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<StructureTemplate, Piece> PIECES = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile List<Block> doors;

    /** Every template directly inside {@code folder} (not in subfolders), sorted so seeds repeat. */
    static List<Piece> folder(StructureTemplateManager manager, ResourceLocation folder) {
        List<ResourceLocation> ids = FOLDERS.computeIfAbsent(manager, m -> new ConcurrentHashMap<>())
                .computeIfAbsent(folder, f -> {
                    String prefix = f.getPath() + "/";
                    return manager.listTemplates()
                            .filter(id -> id.getNamespace().equals(f.getNamespace())
                                    && id.getPath().startsWith(prefix)
                                    && id.getPath().indexOf('/', prefix.length()) < 0)
                            .sorted()
                            .toList();
                });
        List<Piece> pieces = new ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            manager.get(id).ifPresent(template -> pieces.add(PIECES.computeIfAbsent(template, t -> analyse(id, t))));
        }
        return pieces;
    }

    private static Piece analyse(ResourceLocation id, StructureTemplate template) {
        int sx = template.getSize().getX(), sz = template.getSize().getZ();
        BlockPos best = null;
        Direction bestFront = Direction.SOUTH;
        int bestInset = Integer.MAX_VALUE;
        StructurePlaceSettings settings = new StructurePlaceSettings();
        for (Block block : doorBlocks()) {
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(BlockPos.ZERO, settings, block, false)) {
                if (info.state().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        && info.state().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER) continue;
                BlockPos p = info.pos();
                // The door faces out of whichever edge it's nearest
                int[] dist = {p.getZ(), sx - 1 - p.getX(), sz - 1 - p.getZ(), p.getX()};
                Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
                for (int i = 0; i < 4; i++) {
                    boolean better = dist[i] < bestInset || (dist[i] == bestInset && best != null && p.getY() < best.getY());
                    if (better) {
                        best = p;
                        bestFront = dirs[i];
                        bestInset = dist[i];
                    }
                }
            }
        }
        if (best == null) return new Piece(id, template, new BlockPos(sx / 2, 1, sz - 1), Direction.SOUTH, 0);
        return new Piece(id, template, best, bestFront, bestInset);
    }

    private static List<Block> doorBlocks() {
        List<Block> list = doors;
        if (list == null) {
            list = BuiltInRegistries.BLOCK.stream().filter(b -> b instanceof DoorBlock).toList();
            doors = list;
        }
        return list;
    }
}
