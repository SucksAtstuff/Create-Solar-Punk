package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.GreenhouseControllerBlock;
import net.succ.solar_punk.pollution.GlobalWarmingHandler;
import net.succ.solar_punk.pollution.GreenhouseProtection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns the space in front of it into a greenhouse. Every scan interval it flood fills the air
 * from its front face, through anything that isn't a wall (air, plants, farmland, water), up to
 * a maximum volume; if the fill runs out of room the space isn't sealed. Walls are full blocks,
 * greenhouse glass ({@code #solarpunk:greenhouse_glass}), glass panes, and closed doors,
 * trapdoors and fence gates. Once sealed, plants whose first wall straight up is greenhouse glass
 * get extra random ticks, more with bees inside, and the space and its shell are safe from global
 * warming decay (GreenhouseProtection). No power needed. If several controllers face
 * into the same space, only the one with the lowest position runs it.
 */
public class GreenhouseControllerBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public enum Status { SEALED, NOT_SEALED, FRONT_BLOCKED, OTHER_CONTROLLER }

    private Status status = Status.NOT_SEALED;
    private int volume = 0;
    private int bees = 0;
    // Plants under the glass roof, from the last scan. Server only.
    private final List<BlockPos> crops = new ArrayList<>();
    private int cropCount = 0;
    // The inside space and its shell, kept safe from global warming decay while sealed. Server only.
    private LongOpenHashSet protectedArea = null;

    public GreenhouseControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long time = level.getGameTime();
        // Offset by position so a row of greenhouses doesn't all scan on the same tick.
        if (Math.floorMod(time + worldPosition.asLong(), Config.greenhouseScanInterval) == 0) scan(serverLevel);
        if (time % 20 == 0 && status == Status.SEALED) grow(serverLevel);
    }

    private void scan(ServerLevel level) {
        Status oldStatus = status;
        int oldVolume = volume, oldCrops = cropCount, oldBees = bees;
        crops.clear();
        volume = 0;
        bees = 0;

        BlockPos start = worldPosition.relative(getBlockState().getValue(GreenhouseControllerBlock.FACING));
        if (!isInside(level, start)) {
            status = Status.FRONT_BLOCKED;
        } else {
            status = fill(level, start);
        }
        cropCount = crops.size();

        if (status == Status.SEALED && oldStatus != Status.SEALED) {
            ModTriggers.fireNearby(level, worldPosition, ModTriggers.GREENHOUSE_SEALED);
        }

        // Polluted chunks keep their living biome inside the greenhouse; refresh them when
        // the area changes so the patch appears or goes away right away.
        if (status == Status.SEALED && protectedArea != null) {
            GlobalWarmingHandler.refreshGreenhouseBiomes(level,
                    GreenhouseProtection.register(level, worldPosition, protectedArea));
        } else {
            protectedArea = null;
            GlobalWarmingHandler.refreshGreenhouseBiomes(level, GreenhouseProtection.unregister(level, worldPosition));
        }

        if (status != oldStatus || volume != oldVolume || cropCount != oldCrops || bees != oldBees) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private Status fill(ServerLevel level, BlockPos start) {
        LongOpenHashSet inside = new LongOpenHashSet();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> walls = new ArrayList<>();
        inside.add(start.asLong());
        queue.add(start);
        int minX = start.getX(), minY = start.getY(), minZ = start.getZ();
        int maxX = minX, maxY = minY, maxZ = minZ;

        while (!queue.isEmpty()) {
            BlockPos cell = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = cell.relative(dir);
                if (inside.contains(next.asLong())) continue;
                // An unloaded chunk or the edge of the world can't be part of a sealed space.
                if (!level.isLoaded(next) || level.isOutsideBuildHeight(next)) return notSealed();
                if (!isInside(level, next)) {
                    walls.add(next);
                    continue;
                }
                if (inside.size() >= Config.greenhouseMaxVolume) return notSealed();
                inside.add(next.asLong());
                queue.add(next);
                minX = Math.min(minX, next.getX()); maxX = Math.max(maxX, next.getX());
                minY = Math.min(minY, next.getY()); maxY = Math.max(maxY, next.getY());
                minZ = Math.min(minZ, next.getZ()); maxZ = Math.max(maxZ, next.getZ());
            }
        }
        volume = inside.size();

        // Another controller facing into this space runs it if it has the lower position.
        for (BlockPos wall : walls) {
            if (wall.equals(worldPosition)) continue;
            BlockState state = level.getBlockState(wall);
            if (!(state.getBlock() instanceof GreenhouseControllerBlock)) continue;
            BlockPos front = wall.relative(state.getValue(GreenhouseControllerBlock.FACING));
            if (inside.contains(front.asLong()) && wall.asLong() < worldPosition.asLong()) return Status.OTHER_CONTROLLER;
        }

        for (long packed : inside) {
            BlockPos cell = BlockPos.of(packed);
            if (isPlant(level.getBlockState(cell)) && underGlass(level, cell, inside)) crops.add(cell);
        }

        protectedArea = new LongOpenHashSet(inside);
        for (BlockPos wall : walls) protectedArea.add(wall.asLong());

        AABB box = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        bees = level.getEntitiesOfClass(Bee.class, box, bee -> inside.contains(bee.blockPosition().asLong())).size();
        return Status.SEALED;
    }

    private Status notSealed() {
        volume = 0;
        return Status.NOT_SEALED;
    }

    // The first wall straight up from a plant has to be greenhouse glass for it to get the boost.
    private static boolean underGlass(ServerLevel level, BlockPos cell, LongOpenHashSet inside) {
        BlockPos.MutableBlockPos up = cell.mutable();
        do {
            up.move(Direction.UP);
        } while (inside.contains(up.asLong()));
        return level.getBlockState(up).is(ModTags.Blocks.GREENHOUSE_GLASS);
    }

    /** Whether the flood fill can pass through this block. */
    private static boolean isInside(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.Blocks.GREENHOUSE_GLASS) || state.is(Tags.Blocks.GLASS_PANES)) return false;
        if (state.getBlock() instanceof GreenhouseControllerBlock) return false;
        if ((state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(BlockTags.FENCE_GATES))
                && state.hasProperty(BlockStateProperties.OPEN)) {
            return state.getValue(BlockStateProperties.OPEN);
        }
        return !state.isCollisionShapeFullBlock(level, pos);
    }

    // Every plant counts (so fully grown crops show up on the goggles too); only the ones that
    // still random tick get the boost.
    private static boolean isPlant(BlockState state) {
        var block = state.getBlock();
        return block instanceof BonemealableBlock || block instanceof BushBlock || block instanceof SugarCaneBlock
                || block instanceof CactusBlock || block instanceof BambooStalkBlock;
    }

    // Extra random ticks on top of vanilla's: a plant gets randomTickSpeed / 4096 random ticks
    // per game tick on average, so over one second the boost adds that times 20 times the bonus.
    private void grow(ServerLevel level) {
        int tickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (tickSpeed <= 0 || crops.isEmpty()) return;
        double chance = boostMultiplier() * 20.0 * tickSpeed / 4096.0;
        for (BlockPos cell : crops) {
            BlockState state = level.getBlockState(cell);
            if (!isGrowing(state)) continue;
            double rolls = chance;
            while (rolls > 0) {
                if (rolls >= 1 || level.random.nextDouble() < rolls) {
                    state.randomTick(level, cell, level.random);
                    state = level.getBlockState(cell);
                    if (!isGrowing(state)) break;
                }
                rolls -= 1;
            }
        }
    }

    // Growth speed compared to outside, e.g. "2x" or "2.5x".
    private String speedText() {
        double speed = Math.round((1 + boostMultiplier()) * 100) / 100.0;
        return (speed == Math.floor(speed) ? String.valueOf((long) speed) : String.valueOf(speed)) + "x";
    }

    private static boolean isGrowing(BlockState state) {
        return state.isRandomlyTicking() && isPlant(state);
    }

    /** Extra growth on top of normal, e.g. 1.0 = twice as fast. */
    private double boostMultiplier() {
        return Config.greenhouseGrowthBoost + (bees > 0 ? Config.greenhousePollinationBonus : 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.greenhouse_header").forGoggles(tooltip);

        String key = switch (status) {
            case SEALED -> "greenhouse_sealed";
            case NOT_SEALED -> "greenhouse_not_sealed";
            case FRONT_BLOCKED -> "greenhouse_front_blocked";
            case OTHER_CONTROLLER -> "greenhouse_other_controller";
        };
        CreateLang.translate("solar_punk.tooltip.greenhouse_status")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.translate("solar_punk.tooltip." + key)
                        .style(status == Status.SEALED ? ChatFormatting.GREEN : ChatFormatting.RED).component())
                .forGoggles(tooltip, 1);
        if (status != Status.SEALED) return true;

        CreateLang.translate("solar_punk.tooltip.greenhouse_volume")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(volume).style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.greenhouse_crops")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(cropCount).style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.greenhouse_boost")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(speedText()).withStyle(ChatFormatting.GREEN))
                .forGoggles(tooltip, 1);
        if (bees > 0) {
            CreateLang.translate("solar_punk.tooltip.greenhouse_bees")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(bees).style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }
        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!(level instanceof ServerLevel serverLevel)) return;
        var chunks = GreenhouseProtection.unregister(serverLevel, worldPosition);
        // Broken, not unloaded: turn the living-biome patch back into the dead biome.
        if (!unloading) GlobalWarmingHandler.refreshGreenhouseBiomes(serverLevel, chunks);
    }

    // Set when the chunk unloads (called before setRemoved) so unloading doesn't touch biomes.
    private boolean unloading = false;

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unloading = true;
        if (level instanceof ServerLevel serverLevel) GreenhouseProtection.unregister(serverLevel, worldPosition);
    }

    // --- Display Link readouts ---

    public Status getStatus() {
        return status;
    }

    public int getCropCount() {
        return cropCount;
    }

    public MutableComponent getStatusText() {
        return switch (status) {
            case SEALED -> Component.translatable("create.solar_punk.tooltip.greenhouse_sealed").withStyle(ChatFormatting.GREEN);
            case NOT_SEALED -> Component.translatable("create.solar_punk.tooltip.greenhouse_not_sealed").withStyle(ChatFormatting.RED);
            case FRONT_BLOCKED -> Component.translatable("create.solar_punk.tooltip.greenhouse_front_blocked").withStyle(ChatFormatting.RED);
            case OTHER_CONTROLLER -> Component.translatable("create.solar_punk.tooltip.greenhouse_other_controller").withStyle(ChatFormatting.RED);
        };
    }

    /** Growth speed compared to outside, e.g. "2.5x", or "1x" when not sealed. */
    public String getGrowthText() {
        return status == Status.SEALED ? speedText() : "1x";
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("status", status.name());
        tag.putInt("volume", volume);
        tag.putInt("crops", cropCount);
        tag.putInt("bees", bees);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            status = Status.valueOf(tag.getString("status"));
        } catch (IllegalArgumentException e) {
            status = Status.NOT_SEALED;
        }
        volume = tag.getInt("volume");
        cropCount = tag.getInt("crops");
        bees = tag.getInt("bees");
    }
}
