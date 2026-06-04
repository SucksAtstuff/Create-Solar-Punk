package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock.VatPosition;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.List;

public class FermentationVatBlockEntity extends MultiBlockFluidBE<FermentationVatBlockEntity>
        implements IHaveGoggleInformation {

    public static final int MAX_HEIGHT = 16;

    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    public final FluidTank waterTank = new FluidTank(Config.fermentationVatTankPerBlock) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final FluidTank biofuelTank = new FluidTank(Config.fermentationVatTankPerBlock) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get()); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return stack.is(BIO_FUELS); }
        @Override
        public int getSlotLimit(int slot) { return 64; }
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

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

    public int progress = 0;

    public FermentationVatBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, FermentationVatBlockEntity.class);
    }

    // -------------------------------------------------------------------------
    // IMultiBlockEntityContainer.Fluid
    // -------------------------------------------------------------------------

    @Override
    protected void updatePosition() {
        FermentationVatBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return;
        int yOffset = worldPosition.getY() - ctrl.worldPosition.getY();
        boolean alone = ctrl.height == 1;
        boolean isBot = yOffset == 0;
        boolean isTop = yOffset == ctrl.height - 1;
        VatPosition vPos = alone ? VatPosition.SINGLE
                : isBot  ? VatPosition.BOTTOM
                : isTop  ? VatPosition.TOP
                         : VatPosition.MIDDLE;
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(FermentationVatBlock.POSITION) != vPos)
            level.setBlock(worldPosition, state.setValue(FermentationVatBlock.POSITION, vPos), 2);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? MAX_HEIGHT : MAX_WIDTH;
    }

    @Override public int getTankSize(int tank) { return Config.fermentationVatTankPerBlock; }

    @Override
    public void setTankSize(int tank, int blocks) {
        int newCap = Config.fermentationVatTankPerBlock * blocks;
        waterTank.setCapacity(newCap);
        biofuelTank.setCapacity(newCap);
        if (waterTank.getFluidAmount()   > newCap) waterTank.setFluid(new FluidStack(waterTank.getFluid().getFluid(), newCap));
        if (biofuelTank.getFluidAmount() > newCap) biofuelTank.setFluid(new FluidStack(biofuelTank.getFluid().getFluid(), newCap));
    }

    @Override public IFluidTank getTank(int tank) { return waterTank; }
    @Override public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

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

        if (width < Config.fermentationVatMinWidth || height < Config.fermentationVatMinHeight) {
            if (progress > 0) { progress = 0; setChanged(); sync(); }
            setLit(false);
            return;
        }

        int batchScale    = width * width;
        float heightMultiplier = (float) Math.pow((double) height / MAX_HEIGHT, 1.5);
        int waterNeeded   = Math.max(1, (int)(Config.fermentationWaterPerBatch  * batchScale * heightMultiplier));
        int biofuelOutput = Math.max(1, (int)(Config.fermentationBiofuelPerBatch * batchScale * heightMultiplier));

        boolean hasInput   = itemHandler.getStackInSlot(0).getCount() >= batchScale;
        boolean hasWater   = waterTank.getFluidAmount() >= waterNeeded;
        boolean hasSpace   = biofuelTank.getFluidAmount() + biofuelOutput <= biofuelTank.getCapacity();
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

        if (progress >= Config.fermentationTicks) {
            itemHandler.extractItem(0, batchScale, false);
            waterTank.drain(waterNeeded, IFluidHandler.FluidAction.EXECUTE);
            biofuelTank.fill(new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), biofuelOutput),
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

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveMultiblockNBT(tag);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        FluidTankNBTHelper.save(tag, "WaterTank",   waterTank);
        FluidTankNBTHelper.save(tag, "BiofuelTank", biofuelTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadMultiblockNBT(tag);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        if (isController()) {
            int totalBlocks = width * width * height;
            waterTank.setCapacity(Config.fermentationVatTankPerBlock * totalBlocks);
            biofuelTank.setCapacity(Config.fermentationVatTankPerBlock * totalBlocks);
        }
        FluidTankNBTHelper.load(tag, "WaterTank",   waterTank);
        FluidTankNBTHelper.load(tag, "BiofuelTank", biofuelTank);
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

        if (ctrl.width < Config.fermentationVatMinWidth) {
            CreateLang.translate("solar_punk.tooltip.vat_too_small")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
            return true;
        }
        if (ctrl.height < Config.fermentationVatMinHeight) {
            CreateLang.translate("solar_punk.tooltip.vat_too_short")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
            return true;
        }

        int batchScale = ctrl.width * ctrl.width;
        float heightMultiplier = (float) Math.pow((double) ctrl.height / MAX_HEIGHT, 1.5);
        int biofuelPerBatch = Math.max(1, (int)(Config.fermentationBiofuelPerBatch * batchScale * heightMultiplier));
        CreateLang.translate("solar_punk.tooltip.vat_batch_scale")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(batchScale).style(ChatFormatting.WHITE).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.vat_biofuel_output")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(biofuelPerBatch).text(" mB").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        ItemStack input = itemHandler.getStackInSlot(0);
        if (!input.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.fermenting")
                    .style(ChatFormatting.GRAY)
                    .add(input.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                    .forGoggles(tooltip, 1);
            CreateLang.translate("solar_punk.tooltip.progress")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(progress).text(" / " + Config.fermentationTicks).style(ChatFormatting.YELLOW).component())
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
