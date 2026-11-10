package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.WireConnectorBlock;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.wire.WireNetwork;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Wire Connector. Holds no energy: whatever goes into it (pushed in by the block it's on, or
 * pulled out of that block if it's a generator that only gives energy) is handed straight on
 * to the blocks the other connectors in its network sit on, limited by each wire's rate.
 */
public class WireConnectorBlockEntity extends WireNodeBlockEntity implements IHaveGoggleInformation {

    public final IEnergyStorage energyStorage = new ConnectorEnergy();

    @Nullable private BlockCapabilityCache<IEnergyStorage, Direction> attachedCache;
    private List<WireNetwork.Endpoint> endpoints = List.of();
    private int endpointsVersion = -1;
    // Rotates which endpoint gets offered energy first, so a small supply is shared out.
    private int nextEndpoint = 0;
    private int movedThisSecond = 0;
    // FE/t moved through this connector over the last second, for goggles.
    private int throughput = 0;

    public WireConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // The face of this connector that touches the block it's on.
    public Direction back() {
        return getBlockState().getValue(WireConnectorBlock.FACING).getOpposite();
    }

    public BlockPos attachedPos() {
        return worldPosition.relative(back());
    }

    public void tick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        validateWires();

        // Generators that only give energy (and don't push it out themselves) get drained.
        IEnergyStorage attached = attached(serverLevel);
        if (attached != null && attached.canExtract() && !attached.canReceive() && !connections().isEmpty()) {
            int available = attached.extractEnergy(Integer.MAX_VALUE, true);
            int accepted = route(available, true);
            if (accepted > 0) route(attached.extractEnergy(accepted, false), false);
        }

        if (level.getGameTime() % 20 == 0) {
            int now = movedThisSecond / 20;
            movedThisSecond = 0;
            if (now != throughput) {
                throughput = now;
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    @Nullable
    private IEnergyStorage attached(ServerLevel serverLevel) {
        if (attachedCache == null)
            attachedCache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, serverLevel,
                    attachedPos(), back().getOpposite());
        return attachedCache.getCapability();
    }

    // Hands up to amount FE to the blocks under the other connectors. Returns how much they took.
    private int route(int amount, boolean simulate) {
        if (amount <= 0 || !(level instanceof ServerLevel serverLevel) || WireNetwork.isRouting()) return 0;
        if (endpointsVersion != WireNetwork.topologyVersion()) {
            endpointsVersion = WireNetwork.topologyVersion();
            endpoints = WireNetwork.findEndpoints(level, this);
        }
        if (endpoints.isEmpty()) return 0;

        WireNetwork.setRouting(true);
        int remaining = amount;
        try {
            int count = endpoints.size();
            int start = Math.floorMod(nextEndpoint, count);
            for (int i = 0; i < count && remaining > 0; i++) {
                WireNetwork.Endpoint endpoint = endpoints.get((start + i) % count);
                if (!(level.getBlockEntity(endpoint.pos()) instanceof WireConnectorBlockEntity target)) continue;
                if (target.attachedPos().equals(attachedPos())) continue;
                IEnergyStorage sink = target.attached(serverLevel);
                if (sink == null || sink instanceof ConnectorEnergy || !sink.canReceive()) continue;
                int capacity = WireNetwork.capacity(level, endpoint.path());
                if (capacity <= 0) continue;
                int moved = sink.receiveEnergy(Math.min(remaining, capacity), simulate);
                if (moved <= 0) continue;
                remaining -= moved;
                if (!simulate) {
                    WireNetwork.use(level, endpoint.path(), moved);
                    target.movedThisSecond += moved;
                }
            }
            if (!simulate) nextEndpoint = start + 1;
        } finally {
            WireNetwork.setRouting(false);
        }
        int moved = amount - remaining;
        if (!simulate) movedThisSecond += moved;
        return moved;
    }

    // What the block the connector is on sees: it can push energy in, but never take any out.
    private class ConnectorEnergy implements IEnergyStorage {
        @Override public int receiveEnergy(int toReceive, boolean simulate) { return route(toReceive, simulate); }
        @Override public int extractEnergy(int toExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return !connections().isEmpty(); }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.wire_connector_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.wires")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(connections().size() + " / " + Config.wireMaxConnections)
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.throughput")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(throughput))
                        .withStyle(throughput > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("throughput", throughput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        throughput = tag.getInt("throughput");
    }
}
