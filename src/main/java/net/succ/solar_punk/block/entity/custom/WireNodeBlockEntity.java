package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.wire.WireNetwork;
import net.succ.solar_punk.wire.WireTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Anything wires attach to. Each wire runs from one of this block's terminals to a terminal of
 * another node; a Wire Connector has one terminal (0), a Wire Switch two (one per post).
 *
 * <p>Both ends of a wire list it. The wire's items are dropped once, by whichever end is
 * broken; the other end just forgets the wire, now or when it's next loaded.
 */
public abstract class WireNodeBlockEntity extends BlockEntity {

    public record Connection(int terminal, BlockPos other, int otherTerminal, WireTier tier, int wires) {}

    private final List<Connection> connections = new ArrayList<>();
    private boolean validated = false;

    protected WireNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Whether wires on different terminals are joined right now (a closed switch). */
    public boolean joinsTerminals() {
        return false;
    }

    public int terminals() {
        return 1;
    }

    public List<Connection> connections() {
        return Collections.unmodifiableList(connections);
    }

    public int countOn(int terminal) {
        int count = 0;
        for (Connection c : connections) if (c.terminal() == terminal) count++;
        return count;
    }

    public boolean isConnectedTo(int terminal, BlockPos other, int otherTerminal) {
        for (Connection c : connections)
            if (c.terminal() == terminal && c.otherTerminal() == otherTerminal && c.other().equals(other)) return true;
        return false;
    }

    public void addConnection(int terminal, BlockPos other, int otherTerminal, WireTier tier, int wires) {
        connections.add(new Connection(terminal, other, otherTerminal, tier, wires));
        changed();
    }

    private void removeConnection(Connection connection) {
        if (connections.remove(connection)) changed();
    }

    private void removeConnectionsTo(BlockPos other) {
        if (connections.removeIf(c -> c.other().equals(other))) changed();
    }

    protected void changed() {
        WireNetwork.changed();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Drops every wire on this node and removes them from the other ends. */
    public void cutAll() {
        if (level == null || level.isClientSide) return;
        for (Connection c : List.copyOf(connections)) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, new ItemStack(c.tier().wireItem(), c.wires()));
            if (level.isLoaded(c.other()) && level.getBlockEntity(c.other()) instanceof WireNodeBlockEntity other)
                other.removeConnectionsTo(worldPosition);
        }
        connections.clear();
        changed();
    }

    /** Forgets wires whose other end was broken while this one was unloaded. Call every server tick. */
    protected void validateWires() {
        if (validated || level == null) return;
        validated = true;
        for (Connection c : List.copyOf(connections)) {
            if (!level.isLoaded(c.other())) continue;
            if (!(level.getBlockEntity(c.other()) instanceof WireNodeBlockEntity other)
                    || !other.isConnectedTo(c.otherTerminal(), worldPosition, c.terminal()))
                removeConnection(c);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        WireNetwork.changed();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        WireNetwork.changed();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        WireNetwork.changed();
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
        ListTag list = new ListTag();
        for (Connection c : connections) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", c.other().asLong());
            entry.putString("tier", c.tier().getSerializedName());
            entry.putInt("wires", c.wires());
            // Left out when 0, which is also what wires saved before switches existed read as.
            if (c.terminal() != 0) entry.putInt("terminal", c.terminal());
            if (c.otherTerminal() != 0) entry.putInt("other_terminal", c.otherTerminal());
            list.add(entry);
        }
        tag.put("connections", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        connections.clear();
        for (Tag t : tag.getList("connections", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            connections.add(new Connection(entry.getInt("terminal"), BlockPos.of(entry.getLong("pos")),
                    entry.getInt("other_terminal"), WireTier.byName(entry.getString("tier")), entry.getInt("wires")));
        }
        WireNetwork.changed();
    }
}
