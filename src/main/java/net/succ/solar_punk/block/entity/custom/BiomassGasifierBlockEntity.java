package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.item.ModItems;

import java.util.List;

public class BiomassGasifierBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    // Slot 0 = fuel input, Slot 1 = biochar output
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.is(BIO_FUELS);
        }
        @Override
        public int getSlotLimit(int slot) { return 64; }
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            super.deserializeNBT(provider, nbt);
            if (getSlots() < 2) setSize(2);
        }
    };

    private int burnTimeRemaining = 0;

    public BiomassGasifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static int getBurnTicks(ItemStack stack) {
        if (stack.is(ModItems.BIOMASS_PELLET.get())) return Config.pelletBurnTicks;
        return Config.gasifierBurnTicks;
    }

    private static int getBiocharAmount(ItemStack stack) {
        if (stack.is(ModItems.BIOMASS_PELLET.get())) return Config.pelletBiocharAmount;
        return 1;
    }

    @Override
    public float getGeneratedSpeed() {
        return burnTimeRemaining > 0 ? Config.gasifierRpm : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = burnTimeRemaining > 0 ? Config.gasifierSu : 0;
        // Must mirror KineticBlockEntity's base impl: keep lastCapacityProvided in sync.
        // Without this, addSilently() on reload fails to subtract our contribution from
        // unloadedCapacity, causing +1024 SU to accumulate on every world reload.
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        boolean wasActive = burnTimeRemaining > 0;

        if (burnTimeRemaining > 0) {
            burnTimeRemaining--;
            setChanged();
        }

        if (burnTimeRemaining == 0) {
            ItemStack fuel = itemHandler.getStackInSlot(0);
            ItemStack charOutput = itemHandler.getStackInSlot(1);
            int biocharAmount = getBiocharAmount(fuel);
            boolean outputHasRoom = charOutput.isEmpty()
                    || (charOutput.is(ModItems.BIOCHAR.get()) && charOutput.getCount() + biocharAmount <= charOutput.getMaxStackSize());
            if (!fuel.isEmpty() && outputHasRoom) {
                itemHandler.extractItem(0, 1, false);
                burnTimeRemaining = getBurnTicks(fuel);
                if (charOutput.isEmpty()) {
                    itemHandler.setStackInSlot(1, new ItemStack(ModItems.BIOCHAR.get(), biocharAmount));
                } else {
                    charOutput.grow(biocharAmount);
                }
                setChanged();
            }
        }

        boolean active = burnTimeRemaining > 0;
        if (active != wasActive) {
            updateGeneratedRotation();
            BlockState state = level.getBlockState(worldPosition);
            if (state.getValue(BiomassGasifierBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(BiomassGasifierBlock.LIT, active), 3);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("BurnTime", burnTimeRemaining);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        burnTimeRemaining = tag.getInt("BurnTime");
        super.read(tag, registries, clientPacket);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.gasifier_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        int stored = itemHandler.getStackInSlot(0).getCount();
        CreateLang.translate("solar_punk.tooltip.biomass_stored")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(stored).text(" / 64").style(ChatFormatting.WHITE).component())
                .forGoggles(tooltip, 1);

        int biochar = itemHandler.getStackInSlot(1).getCount();
        CreateLang.translate("solar_punk.tooltip.biochar_stored")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(biochar).text(" / 64").style(ChatFormatting.DARK_GREEN).component())
                .forGoggles(tooltip, 1);

        if (burnTimeRemaining > 0) {
            CreateLang.translate("solar_punk.tooltip.burn_time")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(burnTimeRemaining).text(" t").style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }
}