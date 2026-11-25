package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.offbyonelib.util.FluidTankNBTHelper;
import net.succ.offbyonelib.util.PonderHelper;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.custom.GeyserVentBlock;
import net.succ.solar_punk.block.custom.GeyserWellBlock;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.particle.SeasonalSteam;
import net.succ.solar_punk.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Geyser Well: the Steam option for a natural Geyser Vent, instead of the Geyser Cap's SU.
 * Water piped into either port boils into Steam (1:1) at {@code steam_per_tick}, more on a
 * Nether vent, for the Steam Turbine. Only runs on a vent, and like the Cap it stops the vent
 * erupting. The two gauges on its sides show the vent's strength ({@link #ventStrength()});
 * the needles ease towards it on the client for {@code GeyserWellRenderer}.
 */
public class GeyserWellBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public final FluidTank waterTank = new FluidTank(Config.geyserWellTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public final FluidTank steamTank = new FluidTank(Config.geyserWellTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    /** Water in, Steam out. Exposed on the two ports (and to buckets). */
    public final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? steamTank.getFluid() : waterTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? steamTank.getCapacity() : waterTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? steamTank.isFluidValid(stack) : waterTank.isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            return waterTank.isFluidValid(resource) ? waterTank.fill(resource, action) : 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return steamTank.drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return steamTank.drain(maxDrain, action);
        }
    };

    // Client only: gauge needle position (0 to 1), this tick and last
    public float needle, lastNeedle;

    public GeyserWellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The fluid handler for a face: only the two ports along FACING have one. */
    @Nullable
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        if (side == null) return fluidHandler;
        return side.getAxis() == getBlockState().getValue(GeyserWellBlock.FACING).getAxis() ? fluidHandler : null;
    }

    private boolean hasVent() {
        return level != null && level.getBlockState(worldPosition.below()).getBlock() instanceof GeyserVentBlock;
    }

    /** mB of Steam made per tick while running: scaled by the vent's strength, more on a Nether vent. */
    public int steamRate() {
        if (level == null) return 0;
        float multiplier = GeyserVentBlockEntity.outputMultiplier(level, worldPosition.below());
        return multiplier <= 0 ? 0 : Math.max(1, Math.round(Config.geyserWellSteamPerTick * multiplier));
    }

    /**
     * How hard the vent below pushes, for the gauges: 0 with no vent, half on a normal Overworld
     * vent, a quarter on a weak one, and full on a strong one or a Nether one (default numbers).
     */
    public float ventStrength() {
        if (level == null) return 0;
        return Math.min(1, 0.5f * GeyserVentBlockEntity.outputMultiplier(level, worldPosition.below()));
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            tickClient();
            return;
        }

        int rate = steamRate();
        boolean running = rate > 0
                && waterTank.getFluidAmount() >= rate
                && steamTank.getFluidAmount() + rate <= steamTank.getCapacity();
        if (running) {
            waterTank.drain(rate, IFluidHandler.FluidAction.EXECUTE);
            steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), rate), IFluidHandler.FluidAction.EXECUTE);
        }

        BlockState state = getBlockState();
        if (state.getValue(GeyserWellBlock.LIT) != running)
            level.setBlock(worldPosition, state.setValue(GeyserWellBlock.LIT, running), Block.UPDATE_ALL);

        long time = level.getGameTime();
        if (running && time % 40 == 0) {
            double cx = worldPosition.getX() + 0.5, cy = worldPosition.getY() + 0.9, cz = worldPosition.getZ() + 0.5;
            SeasonalSteam.send((ServerLevel) level, cx, cy, cz, 4, 0.15, 0.05, 0.15, 0.03);
        }
        if (time % 20 == 0)
            level.sendBlockUpdated(worldPosition, state, state, 2);
    }

    private void tickClient() {
        lastNeedle = needle;
        needle += (ventStrength() - needle) * 0.15f;

        // The vent's puff, like a Geyser Cap's, while boiling
        if (level.getGameTime() % 40 == 0 && getBlockState().getValue(GeyserWellBlock.LIT) && !PonderHelper.isPonder(level))
            level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    ModSounds.GEYSER_PUFF.get(), SoundSource.BLOCKS, 0.15f, 1.1f + level.random.nextFloat() * 0.2f, false);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.geyser_well_header").forGoggles(tooltip);
        if (!hasVent())
            CreateLang.translate("solar_punk.tooltip.geyser_well_no_vent").style(ChatFormatting.RED).forGoggles(tooltip, 1);
        SolarPunk.GOGGLES.tank(tooltip, "water", waterTank, ChatFormatting.AQUA);
        SolarPunk.GOGGLES.tank(tooltip, "steam", steamTank, ChatFormatting.AQUA);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.save(tag, "SteamTank", steamTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.load(tag, "SteamTank", steamTank);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
