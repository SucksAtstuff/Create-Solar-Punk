package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// The Core sits buried at the center of the sealed sphere, so once the reactor is built
// nothing can reach it. Every Casing block remembers where its Core is (set by the
// Core's scanStructure()) and proxies goggles, fluids (Deuterium in, Steam out) and
// items (Lithium Dust in) through to it.
public class FusionReactorCasingBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    private BlockPos corePos = null;

    public FusionReactorCasingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setCorePos(BlockPos pos) {
        if (pos.equals(corePos)) return;
        corePos = pos;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Nullable
    public FusionReactorCoreBlockEntity core() {
        if (level == null || corePos == null) return null;
        return level.getBlockEntity(corePos) instanceof FusionReactorCoreBlockEntity core ? core : null;
    }

    @Nullable
    public IFluidHandler getFluidHandler() {
        FusionReactorCoreBlockEntity core = core();
        return core != null ? core.combinedFluidHandler : null;
    }

    @Nullable
    public IItemHandler getItemHandler() {
        FusionReactorCoreBlockEntity core = core();
        return core != null ? core.itemHandler : null;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FusionReactorCoreBlockEntity core = core();
        return core != null && core.addToGoggleTooltip(tooltip, isPlayerSneaking);
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
        if (corePos != null) tag.putLong("CorePos", corePos.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        corePos = tag.contains("CorePos") ? BlockPos.of(tag.getLong("CorePos")) : null;
    }
}
