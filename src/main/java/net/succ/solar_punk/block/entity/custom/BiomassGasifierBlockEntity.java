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
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;

import java.util.List;

public class BiomassGasifierBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    private static final TagKey<Item> BIO_FUELS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "bio_fuels"));

    private static final float RPM      = 8f;
    private static final float CAPACITY = 128f;
    public static final int BURN_TIME   = 300;

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

    private int burnTimeRemaining = 0;

    public BiomassGasifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        return burnTimeRemaining > 0 ? RPM : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = burnTimeRemaining > 0 ? CAPACITY : 0;
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

        if (burnTimeRemaining == 0 && !itemHandler.getStackInSlot(0).isEmpty()) {
            itemHandler.extractItem(0, 1, false);
            burnTimeRemaining = BURN_TIME;
            setChanged();
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

        if (burnTimeRemaining > 0) {
            CreateLang.translate("solar_punk.tooltip.burn_time")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(burnTimeRemaining).text(" t").style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }

        return true;
    }
}