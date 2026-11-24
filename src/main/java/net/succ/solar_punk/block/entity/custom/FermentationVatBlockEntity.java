package net.succ.solar_punk.block.entity.custom;

import net.succ.offbyonelib.multiblock.MultiBlockFluidBE;
import net.succ.solar_punk.SolarPunk;
import net.succ.offbyonelib.client.sound.MachineSoundInstance;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.FermentationVatBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock.VatPosition;
import net.succ.solar_punk.client.sound.FermentationVatSoundInstance;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.offbyonelib.util.FluidTankNBTHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FermentationVatBlockEntity extends MultiBlockFluidBE<FermentationVatBlockEntity>
        implements IHaveGoggleInformation {

    private boolean positionChecked;

    public static final int MAX_HEIGHT = 16;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private FermentationVatSoundInstance soundInstance;

    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    public final FluidTank waterTank = new FluidTank(Config.fermentationVatTankPerBlock) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    // Biofuel from plant matter, or Rich Fertilizer from waste (FERMENTATION_WASTE). One fluid at
    // a time: a batch waits until the tank is empty of the other one.
    public final FluidTank outputTank = new FluidTank(Config.fermentationVatTankPerBlock) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get())
                    || stack.getFluid().isSame(ModFluids.RICH_FERTILIZER_SOURCE.get());
        }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    // Aerobic mode: Oxygen speeds up Rich Fertilizer (composting is aerobic). Biofuel is
    // anaerobic, so Oxygen is only taken in while waste is loaded (acceptsOxygen()).
    public final FluidTank oxygenTank = new FluidTank(Config.fermentationVatOxygenTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(ModFluids.OXYGEN_SOURCE.get()); }
        @Override
        protected void onContentsChanged() { setChanged(); sync(); }
    };

    // Whether the running batch has Oxygen this second (saved for Goggles)
    private boolean aerobic;

    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(BIO_FUELS) || stack.is(ModTags.Items.FERMENTATION_WASTE);
        }
        @Override
        public int getSlotLimit(int slot) { return 64; }
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        private FermentationVatBlockEntity ctrl() { return getControllerBE(); }

        @Override public int getTanks() { return 3; }

        private FluidTank tankAt(FermentationVatBlockEntity c, int tank) {
            return tank == 0 ? c.waterTank : tank == 1 ? c.outputTank : c.oxygenTank;
        }

        @Override public FluidStack getFluidInTank(int tank) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? tankAt(c, tank).getFluid() : FluidStack.EMPTY;
        }

        @Override public int getTankCapacity(int tank) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? tankAt(c, tank).getCapacity() : 0;
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? stack.getFluid().isSame(Fluids.WATER)
                    : tank == 2 && stack.getFluid().isSame(ModFluids.OXYGEN_SOURCE.get());
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            if (c == null) return 0;
            if (resource.getFluid().isSame(Fluids.WATER)) return c.waterTank.fill(resource, action);
            if (c.acceptsOxygen()) return c.oxygenTank.fill(resource, action);
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? c.outputTank.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            FermentationVatBlockEntity c = ctrl();
            return c != null ? c.outputTank.drain(maxDrain, action) : FluidStack.EMPTY;
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
        // This block's own height, not the controller's: Create sets the parts' size
        // before the controller's, so the controller still has its old height here.
        FermentationVatBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return;
        int yOffset = worldPosition.getY() - ctrl.worldPosition.getY();
        boolean alone = height == 1;
        boolean isBot = yOffset == 0;
        boolean isTop = yOffset == height - 1;
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
        int newCap = Config.fermentationVatTankPerBlock * Math.max(blocks, 1);
        waterTank.setCapacity(newCap);
        outputTank.setCapacity(newCap);
        if (waterTank.getFluidAmount()   > newCap) waterTank.setFluid(new FluidStack(waterTank.getFluid().getFluid(), newCap));
        if (outputTank.getFluidAmount() > newCap) outputTank.setFluid(new FluidStack(outputTank.getFluid().getFluid(), newCap));
    }

    @Override public IFluidTank getTank(int tank) { return waterTank; }
    @Override public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance,
                isController() && getBlockState().getValue(FermentationVatBlock.LIT),
                () -> new FermentationVatSoundInstance(this));
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti(this);
        }
        // Tanks formed before 2.2.1 could have the wrong POSITION on every block but the
        // controller, so each block checks its own once after loading.
        if (!positionChecked) {
            positionChecked = true;
            updatePosition();
        }

        if (!isController()) return;

        if (width < Config.fermentationVatMinWidth || height < Config.fermentationVatMinHeight) {
            if (progress > 0) { progress = 0; setChanged(); sync(); }
            setLit(false);
            return;
        }

        int batchScale    = width * width;
        int waterNeeded   = scaledBatchAmount(Config.fermentationWaterPerBatch);
        FluidStack output  = batchOutput(itemHandler.getStackInSlot(0));

        boolean hasInput   = itemHandler.getStackInSlot(0).getCount() >= batchScale;
        boolean hasWater   = waterTank.getFluidAmount() >= waterNeeded;
        boolean hasSpace   = outputTank.fill(output, IFluidHandler.FluidAction.SIMULATE) == output.getAmount();
        boolean canFerment = hasInput && hasWater && hasSpace;

        if (!canFerment) {
            if (progress > 0) { progress = 0; setChanged(); sync(); }
            if (aerobic) { aerobic = false; sync(); }
            setLit(false);
            return;
        }

        setLit(true);
        // Once a second, a Rich Fertilizer batch takes a second's Oxygen if there's enough
        if (level.getGameTime() % 20 == 0) {
            int cost = Config.fermentationAerobicOxygen * batchScale;
            boolean now = makesFertilizer(itemHandler.getStackInSlot(0)) && oxygenTank.getFluidAmount() >= cost;
            if (now) oxygenTank.drain(cost, IFluidHandler.FluidAction.EXECUTE);
            if (now != aerobic) { aerobic = now; sync(); }
        }
        progress += aerobic ? Config.fermentationAerobicSpeed : 1;
        setChanged();
        if (progress % 20 == 0) sync();

        if (progress >= Config.fermentationTicks) {
            itemHandler.extractItem(0, batchScale, false);
            waterTank.drain(waterNeeded, IFluidHandler.FluidAction.EXECUTE);
            outputTank.fill(output, IFluidHandler.FluidAction.EXECUTE);
            // "The Brewer" is for Biofuel, not Rich Fertilizer.
            if (output.getFluid().isSame(ModFluids.BIOFUEL_SOURCE.get()))
                ModTriggers.fireNearby(level, worldPosition, ModTriggers.FERMENTATION_DONE);
            progress = 0;
            setChanged();
        }
    }

    private static boolean makesFertilizer(ItemStack input) {
        return input.is(ModTags.Items.FERMENTATION_WASTE);
    }

    // Oxygen only goes in while waste is loaded, so pipes don't fill the Vat with it
    // during Biofuel batches
    public boolean acceptsOxygen() {
        return makesFertilizer(itemHandler.getStackInSlot(0));
    }

    // What a batch of `input` makes: Rich Fertilizer from waste, Biofuel from anything else.
    private FluidStack batchOutput(ItemStack input) {
        return makesFertilizer(input)
                ? new FluidStack(ModFluids.RICH_FERTILIZER_SOURCE.get(), scaledBatchAmount(Config.fermentationRichFertilizerPerBatch))
                : new FluidStack(ModFluids.BIOFUEL_SOURCE.get(), scaledBatchAmount(Config.fermentationBiofuelPerBatch));
    }

    // A batch consumes one item per footprint block. Fluid amounts scale with the
    // footprint and with height (taller vats are disproportionately better).
    private int scaledBatchAmount(int perBlock) {
        float heightMultiplier = (float) Math.pow((double) height / MAX_HEIGHT, 1.5);
        return Math.max(1, (int) (perBlock * width * width * heightMultiplier));
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
        FluidTankNBTHelper.save(tag, "BiofuelTank", outputTank);
        FluidTankNBTHelper.save(tag, "OxygenTank",  oxygenTank);
        tag.putBoolean("Aerobic", aerobic);
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
            outputTank.setCapacity(Config.fermentationVatTankPerBlock * totalBlocks);
        }
        FluidTankNBTHelper.load(tag, "WaterTank",   waterTank);
        FluidTankNBTHelper.load(tag, "BiofuelTank", outputTank);
        FluidTankNBTHelper.load(tag, "OxygenTank",  oxygenTank);
        aerobic = tag.getBoolean("Aerobic");
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FermentationVatBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return false;
        if (ctrl != this) return ctrl.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.fermentation_vat_header").forGoggles(tooltip);

        if (width < Config.fermentationVatMinWidth) {
            CreateLang.translate("solar_punk.tooltip.vat_too_small")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
            return true;
        }
        if (height < Config.fermentationVatMinHeight) {
            CreateLang.translate("solar_punk.tooltip.vat_too_short")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
            return true;
        }

        int batchScale = width * width;
        int outputPerBatch = batchOutput(itemHandler.getStackInSlot(0)).getAmount();
        CreateLang.translate("solar_punk.tooltip.vat_batch_scale")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(batchScale).style(ChatFormatting.WHITE).component())
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.vat_biofuel_output")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(outputPerBatch).text(" mB").style(ChatFormatting.GREEN).component())
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

        SolarPunk.GOGGLES.tank(tooltip, "water", waterTank, ChatFormatting.AQUA);
        boolean fertilizer = outputTank.getFluid().getFluid().isSame(ModFluids.RICH_FERTILIZER_SOURCE.get());
        SolarPunk.GOGGLES.tank(tooltip, fertilizer ? "rich_fertilizer" : "biofuel", outputTank, ChatFormatting.GREEN);
        if (!oxygenTank.isEmpty() || acceptsOxygen())
            SolarPunk.GOGGLES.tank(tooltip, "oxygen", oxygenTank, ChatFormatting.AQUA);
        if (aerobic) {
            CreateLang.translate("solar_punk.tooltip.vat_aerobic")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.text(Config.fermentationAerobicSpeed + "x speed").style(ChatFormatting.AQUA).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }
}
