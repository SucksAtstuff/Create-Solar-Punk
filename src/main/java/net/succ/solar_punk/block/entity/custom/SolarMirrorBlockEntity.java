package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A heliostat mirror. Holds no logic of its own beyond remembering which Solar Power
 * Tower controller it is currently reporting to - the tower drives discovery, occlusion
 * checks, and counting (see {@link SolarPowerTowerBlockEntity}). The renderer uses
 * {@link #getLinkedTower()} to aim the mirror plate at the tower's receiver each frame.
 */
public class SolarMirrorBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos linkedTower = null;

    public SolarMirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public BlockPos getLinkedTower() {
        return linkedTower;
    }

    /** Called by the owning tower controller when this mirror is accepted into / dropped from its field. */
    public void setLinkedTower(@Nullable BlockPos tower) {
        if (java.util.Objects.equals(linkedTower, tower)) return;
        linkedTower = tower;
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
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
        if (linkedTower != null) tag.putLong("LinkedTower", linkedTower.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        linkedTower = tag.contains("LinkedTower") ? BlockPos.of(tag.getLong("LinkedTower")) : null;
    }
}