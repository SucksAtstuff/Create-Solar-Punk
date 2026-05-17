package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class FermentationVatBlockEntity extends BlockEntity
        implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {

    public static final int FERMENTATION_TIME       = 400;
    public static final int WATER_PER_BATCH         = 1000;
    public static final int BIOFUEL_PER_BATCH       = 1000;
    public static final int TANK_CAPACITY_PER_BLOCK = 8000;
    public static final int MAX_WIDTH               = 3;
    public static final int MAX_HEIGHT              = 16;

    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    // --- IMultiBlockEntityContainer state ---
    private BlockPos controller       = null;
    private int      width            = 1;
    private int      height           = 1;
    public boolean updateConnectivity = false;

    // --- Tanks (only filled on controller; non-controllers always empty) ---
    public final FluidTank waterTank = new FluidTank(TANK_CAPACITY_PER_BLOCK) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final FluidTank biofuelTank = new FluidTank(TANK_CAPACITY_PER_BLOCK) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get()); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    // --- Item handler (only meaningful on controller) ---
    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(BIO_FUELS);
        }
        @Override
        public int getSlotLimit(int slot) { return 64; }
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    // External fluid capability — always routes through the controller's tanks.
    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        private FermentationVatBlockEntity ctrl() { return getControllerBE(); }

        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            FermentationVatBlockEntity c = ctrl();
            if (c == null) return FluidStack.EMPTY;
            return tank == 0 ? c.waterTank.getFluid() : c.biofuelTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            FermentationVatBlockEntity c = ctrl();
            if (c == null) return 0;
            return tank == 0 ? c.waterTank.getCapacity() : c.biofuelTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(Fluids.WATER);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            if (c == null) return 0;
            return resource.getFluid().isSame(Fluids.WATER) ? c.waterTank.fill(resource, action) : 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? c.biofuelTank.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? c.biofuelTank.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    };

    private int progress = 0;

    public FermentationVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // -------------------------------------------------------------------------
    // IMultiBlockEntityContainer.Fluid
    // -------------------------------------------------------------------------

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.equals(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    public FermentationVatBlockEntity getControllerBE() {
        if (isController()) return this;
        if (level == null) return this;
        BlockEntity be = level.getBlockEntity(controller);
        return be instanceof FermentationVatBlockEntity vat ? vat : null;
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

    @Override
    public BlockPos getLastKnownPos() {
        return worldPosition;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        setChanged();
        sync();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? MAX_HEIGHT : MAX_WIDTH;
    }

    @Override
    public int getMaxWidth() { return MAX_WIDTH; }

    @Override public int getHeight()            { return height; }
    @Override public void setHeight(int height) { this.height = height; }
    @Override public int getWidth()             { return width; }
    @Override public void setWidth(int width)   { this.width = width; }

    @Override
    public boolean hasTank() { return true; }

    @Override
    public int getTankSize(int tank) { return TANK_CAPACITY_PER_BLOCK; }

    @Override
    public void setTankSize(int tank, int blocks) {
        int newCap = TANK_CAPACITY_PER_BLOCK * blocks;
        waterTank.setCapacity(newCap);
        biofuelTank.setCapacity(newCap);
        if (waterTank.getFluidAmount() > newCap)
            waterTank.setFluid(new FluidStack(waterTank.getFluid().getFluid(), newCap));
        if (biofuelTank.getFluidAmount() > newCap)
            biofuelTank.setFluid(new FluidStack(biofuelTank.getFluid().getFluid(), newCap));
    }

    @Override
    public IFluidTank getTank(int tank) { return waterTank; }

    @Override
    public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti(this);
        }

        if (!isController()) return;

        boolean hasInput   = !itemHandler.getStackInSlot(0).isEmpty();
        boolean hasWater   = waterTank.getFluidAmount() >= WATER_PER_BATCH;
        boolean hasSpace   = biofuelTank.getFluidAmount() + BIOFUEL_PER_BATCH <= biofuelTank.getCapacity();
        boolean canFerment = hasInput && hasWater && hasSpace;

        if (!canFerment) {
            if (progress > 0) { progress = 0; setChanged(); sync(); }
            setLit(false);
            return;
        }

        setLit(true);
        progress++;
        setChanged();
        if (progress % 20 == 0) sync();

        if (progress >= FERMENTATION_TIME) {
            itemHandler.extractItem(0, 1, false);
            waterTank.drain(WATER_PER_BATCH, IFluidHandler.FluidAction.EXECUTE);
            biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), BIOFUEL_PER_BATCH),
                    IFluidHandler.FluidAction.EXECUTE);
            progress = 0;
            setChanged();
        }
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(FermentationVatBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(FermentationVatBlock.LIT, lit), 3);
    }

    private void sync() {
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!isController())
            tag.putLong("Controller", controller.asLong());
        tag.putInt("Width",  width);
        tag.putInt("Height", height);
        if (updateConnectivity)
            tag.putBoolean("Uninitialized", true);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        saveFluidTank(tag, "WaterTank",   waterTank);
        saveFluidTank(tag, "BiofuelTank", biofuelTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = null;
        if (tag.contains("Controller"))
            controller = BlockPos.of(tag.getLong("Controller"));
        width  = tag.getInt("Width");
        height = tag.getInt("Height");
        if (width  == 0) width  = 1;
        if (height == 0) height = 1;
        updateConnectivity = tag.contains("Uninitialized");
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        if (isController()) {
            int totalBlocks = width * width * height;
            waterTank.setCapacity(TANK_CAPACITY_PER_BLOCK * totalBlocks);
            biofuelTank.setCapacity(TANK_CAPACITY_PER_BLOCK * totalBlocks);
        }
        loadFluidTank(tag, "WaterTank",   waterTank);
        loadFluidTank(tag, "BiofuelTank", biofuelTank);
    }

    private static void saveFluidTank(CompoundTag tag, String key, FluidTank tank) {
        if (tank.isEmpty()) return;
        CompoundTag t = new CompoundTag();
        t.putString("Fluid", BuiltInRegistries.FLUID.getKey(tank.getFluid().getFluid()).toString());
        t.putInt("Amount", tank.getFluidAmount());
        tag.put(key, t);
    }

    private static void loadFluidTank(CompoundTag tag, String key, FluidTank tank) {
        if (!tag.contains(key)) return;
        CompoundTag t = tag.getCompound(key);
        BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(t.getString("Fluid")))
                .filter(fluid -> fluid != Fluids.EMPTY)
                .ifPresent(fluid -> tank.setFluid(new FluidStack(fluid, t.getInt("Amount"))));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FermentationVatBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return false;
        if (ctrl != this) return ctrl.addToGoggleTooltip(tooltip, isPlayerSneaking);

        int cap = waterTank.getCapacity();
        CreateLang.translate("solar_punk.tooltip.fermentation_vat_header").forGoggles(tooltip);

        ItemStack input = itemHandler.getStackInSlot(0);
        if (!input.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.fermenting")
                    .style(ChatFormatting.GRAY)
                    .add(input.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .forGoggles(tooltip, 1);
            CreateLang.translate("solar_punk.tooltip.progress")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(progress).text(" / " + FERMENTATION_TIME).style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }

        CreateLang.translate("solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + cap + " mB").style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.biofuel")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(biofuelTank.getFluidAmount())
                        .text(" / " + cap + " mB").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        return true;
    }
}