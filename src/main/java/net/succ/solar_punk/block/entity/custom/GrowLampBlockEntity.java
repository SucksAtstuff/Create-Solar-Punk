package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.GrowLampBlock;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.PlantGrowth;

import java.util.ArrayList;
import java.util.List;

/**
 * Grow Lamp. Runs on FE from any side and is lit while it has enough for the next second and
 * no redstone signal. While lit it lights the area like a lamp, so crops grow at night and
 * underground, and speeds up the first plant found in each column of a 3x3 area below it.
 */
public class GrowLampBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public static class LampEnergyStorage extends EnergyStorage {
        LampEnergyStorage(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }
        void use(int amount) {
            energy = Math.max(0, energy - amount);
        }
        void setStored(int amount) {
            energy = Math.max(0, Math.min(amount, capacity));
        }
    }

    public final LampEnergyStorage energyStorage = new LampEnergyStorage(Config.growLampBuffer, Config.growLampMaxReceive);
    // Plants found under the lamp on the last check, for goggles.
    private int plants = 0;
    private int syncedEnergy = -1;

    public GrowLampBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (Math.floorMod(level.getGameTime() + worldPosition.hashCode(), 20) != 0) return;

        int perSecond = Config.growLampFePerTick * 20;
        boolean lit = !level.hasNeighborSignal(worldPosition) && energyStorage.getEnergyStored() >= perSecond;
        if (lit && perSecond > 0) {
            energyStorage.use(perSecond);
            setChanged();
        }
        BlockState state = getBlockState();
        if (state.getValue(GrowLampBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(GrowLampBlock.LIT, lit), Block.UPDATE_ALL);

        List<BlockPos> found = findPlants();
        if (lit) PlantGrowth.boost(serverLevel, found, Config.growLampGrowthBoost);

        if (found.size() != plants || energyStorage.getEnergyStored() != syncedEnergy) {
            plants = found.size();
            syncedEnergy = energyStorage.getEnergyStored();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    // The first plant going down each column of the 3x3 area under the lamp, stopping at the
    // first solid block (the floor, or a ceiling between the lamp and the plant).
    private List<BlockPos> findPlants() {
        List<BlockPos> found = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 1; dy <= Config.growLampRange; dy++) {
                    BlockPos pos = worldPosition.offset(dx, -dy, dz);
                    if (!level.isLoaded(pos)) break;
                    BlockState state = level.getBlockState(pos);
                    if (PlantGrowth.isPlant(state)) {
                        found.add(pos);
                        break;
                    }
                    if (state.isCollisionShapeFullBlock(level, pos)) break;
                }
            }
        }
        return found;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.grow_lamp_header").forGoggles(tooltip);

        int usage = getBlockState().getValue(GrowLampBlock.LIT) ? Config.growLampFePerTick : 0;
        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(usage)).withStyle(ChatFormatting.GOLD))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.abbreviate(energyStorage.getEnergyStored())
                                + " / " + EnergyFormat.fe(energyStorage.getMaxEnergyStored()))
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.grow_lamp_plants")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(String.valueOf(plants)).withStyle(ChatFormatting.GREEN))
                .forGoggles(tooltip, 1);
        return true;
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
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("plants", plants);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStorage.setStored(tag.getInt("energy"));
        plants = tag.getInt("plants");
    }
}
