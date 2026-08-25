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

// Casing/Casing Glass have no plumbing or logic of their own (see
// FusionReactorCasingBlock's own comment) - but the reactor's actual
// FusionReactorCoreBlockEntity, and every tank/slot it owns, sits buried at the exact
// center of the sealed sphere and is never reachable by a player, a pipe, or a hopper
// once the reactor is built. Every Casing position sits on the sphere's outer surface
// and *is* reachable, so this BE just remembers where its Core is - pushed to it by the
// Core's own scanStructure(), which already visits every Casing position each scan
// anyway, rather than each Casing block brute-force searching for its Core on every
// capability/goggle query - and proxies goggle info, fluid handling (Deuterium in,
// Steam out), and item handling (Lithium Dust in) all through to it. Same
// "non-controller cell proxies to the controller" shape FermentationVatBlockEntity
// already uses, just for reachability instead of multiblock cell identity.
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
    private FusionReactorCoreBlockEntity core() {
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
