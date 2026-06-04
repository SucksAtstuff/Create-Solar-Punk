package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MultiBlockFluidBE<T extends MultiBlockFluidBE<T>> extends BlockEntity
        implements IMultiBlockEntityContainer.Fluid {

    public static final int MAX_WIDTH = 3;

    protected BlockPos controller       = null;
    protected int      width            = 1;
    protected int      height           = 1;
    public boolean     updateConnectivity = false;

    private final Class<T> selfType;

    protected MultiBlockFluidBE(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<T> selfType) {
        super(type, pos, state);
        this.selfType = selfType;
    }

    protected abstract void updatePosition();

    // IMultiBlockEntityContainer.Fluid shared implementations

    @Override public BlockPos getController() { return isController() ? worldPosition : controller; }
    @Override public boolean isController() { return controller == null || worldPosition.equals(controller); }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends BlockEntity & IMultiBlockEntityContainer> C getControllerBE() {
        if (isController()) return (C) this;
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(controller);
        return selfType.isInstance(be) ? (C) selfType.cast(be) : null;
    }

    @Override
    public void setController(BlockPos pos) {
        if (level != null && level.isClientSide) return;
        if (pos.equals(controller)) return;
        this.controller = pos;
        setChanged();
        invalidateCapabilities();
        sync();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level != null && level.isClientSide) return;
        updateConnectivity = true;
        if (!keepContents) setTankSize(0, 1);
        controller = null;
        width  = 1;
        height = 1;
        setChanged();
        invalidateCapabilities();
        sync();
    }

    @Override public BlockPos getLastKnownPos()          { return worldPosition; }
    @Override public void preventConnectivityUpdate()    { updateConnectivity = false; }
    @Override public Direction.Axis getMainConnectionAxis() { return Direction.Axis.Y; }
    @Override public int getMaxWidth()                   { return MAX_WIDTH; }
    @Override public int getHeight()                     { return height; }
    @Override public void setHeight(int height)          { this.height = height; }
    @Override public int getWidth()                      { return width; }
    @Override public void setWidth(int width)            { this.width = width; }
    @Override public boolean hasTank()                   { return true; }

    @Override
    public void notifyMultiUpdated() {
        setChanged();
        sync();
        if (level != null && !level.isClientSide)
            updatePosition();
    }

    protected void sync() {
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // NBT helpers for the shared multiblock state

    protected void saveMultiblockNBT(CompoundTag tag) {
        if (!isController()) tag.putLong("Controller", controller.asLong());
        tag.putInt("Width",  width);
        tag.putInt("Height", height);
        if (updateConnectivity) tag.putBoolean("Uninitialized", true);
    }

    protected void loadMultiblockNBT(CompoundTag tag) {
        controller = null;
        if (tag.contains("Controller")) controller = BlockPos.of(tag.getLong("Controller"));
        width  = tag.getInt("Width");
        height = tag.getInt("Height");
        if (width  == 0) width  = 1;
        if (height == 0) height = 1;
        updateConnectivity = tag.contains("Uninitialized");
    }

    // Packet sync shared implementations

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        requestModelDataUpdate();
        if (level != null && level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
    }
}
