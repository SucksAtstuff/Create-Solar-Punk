package net.succ.solar_punk.block.entity.custom;

import net.neoforged.neoforge.items.IItemHandler;
import net.succ.solar_punk.client.sound.MachineSoundInstance;
import net.succ.solar_punk.util.GeneratorHelper;
import net.succ.solar_punk.util.AutomationItemHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.BiomassGasifierBlock;
import net.succ.solar_punk.client.sound.BiomassGasifierSoundInstance;
import net.succ.solar_punk.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiomassGasifierBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    ScrollOptionBehaviour<RotationDirection> rotationDirection;

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

    // Hoppers and funnels put fuel in and take the Biochar out
    public final IItemHandler automationHandler = AutomationItemHandler.inputOutput(itemHandler, 0, 1);

    private int burnTimeRemaining = 0;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private BiomassGasifierSoundInstance soundInstance;

    public BiomassGasifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // Every side except the front (FACING) and the bottom (the shaft)
        rotationDirection = GeneratorHelper.rotationDirection(this, 8, (state, direction) ->
                direction != state.getValue(BiomassGasifierBlock.FACING) && direction != Direction.DOWN);
        behaviours.add(rotationDirection);
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
        if (burnTimeRemaining <= 0) return 0;
        return GeneratorHelper.directed(Config.gasifierRpm, rotationDirection);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = burnTimeRemaining > 0 ? Config.gasifierSu : 0;
        // Keep lastCapacityProvided in sync like KineticBlockEntity's base impl does.
        // Without it, the network gains phantom SU on every world reload.
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        // Create's KineticBlockEntity.tick() calls this too, so Ponder's silence is
        // handled in keepPlaying() rather than at the call site
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance,
                getBlockState().getValue(BiomassGasifierBlock.LIT), () -> new BiomassGasifierSoundInstance(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

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

        // Goggles read the client's copy (fuel, Biochar, burn time)
        if (level.getGameTime() % 20 == 0) sendData();

        boolean active = burnTimeRemaining > 0;
        if (active != wasActive) {
            updateGeneratedRotation();
            BlockState state = level.getBlockState(worldPosition);
            if (!(state.getBlock() instanceof BiomassGasifierBlock)) return;
            if (state.getValue(BiomassGasifierBlock.LIT) != active)
                level.setBlock(worldPosition, state.setValue(BiomassGasifierBlock.LIT, active), 3);
            if (active)
                ModTriggers.fireNearby(level, worldPosition, ModTriggers.GASIFIER_RUNNING);
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