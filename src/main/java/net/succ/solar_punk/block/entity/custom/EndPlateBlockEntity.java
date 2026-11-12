package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.EndPlateBlock;
import net.succ.solar_punk.compat.northstar.NorthstarCompat;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.util.EnergyFormat;
import net.succ.solar_punk.util.FluidTankNBTHelper;
import net.succ.solar_punk.util.GoggleHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One End Plate of a Fuel Cell stack: a straight row of Fuel Cell blocks (axis along the
 * row) with an End Plate facing out at each end. Both plates scan the stack once a second;
 * the one facing north, west or down is the controller and holds the tanks and FE, and the
 * other passes everything through to it.
 *
 * <p>Each cell burns {@code hydrogen_per_tick} mB of Hydrogen and half as much Oxygen per
 * tick (2 H2 + O2 -> 2 H2O), making {@code fe_per_tick} FE and the same amount of Water as
 * Hydrogen. It stops when it's short of either gas, the Water tank or FE buffer is full, or
 * either plate has a redstone signal.
 */
public class EndPlateBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    public enum Status { FORMED, NO_CELLS, NO_PLATE, TOO_LONG }

    public final FluidTank hydrogenTank = tank(stack -> NorthstarCompat.isHydrogen(stack.getFluid()));
    public final FluidTank oxygenTank = tank(stack -> stack.getFluid().isSame(ModFluids.OXYGEN_SOURCE.get()));
    public final FluidTank waterTank = tank(stack -> stack.getFluid().isSame(Fluids.WATER));

    private final IFluidHandler gasIn = new Handler(true, false);
    private final IFluidHandler waterOut = new Handler(false, true);
    private final IFluidHandler allFluids = new Handler(true, true);
    public final IEnergyStorage energyStorage = new Energy();

    private int energy = 0;
    // Hydrogen burned that hasn't used up a whole mB of Oxygen yet (0 or 1).
    private int oxygenRemainder = 0;

    private Status status = Status.NO_CELLS;
    private int cells = 0;
    @Nullable private BlockPos partner;
    private boolean running = false;

    public EndPlateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private FluidTank tank(java.util.function.Predicate<FluidStack> valid) {
        return new FluidTank(Config.fuelCellTank, valid) {
            @Override
            protected void onContentsChanged() {
                setChanged();
            }
        };
    }

    private Direction facing() {
        return getBlockState().getValue(EndPlateBlock.FACING);
    }

    public boolean isController() {
        return facing().getAxisDirection() == Direction.AxisDirection.NEGATIVE;
    }

    public boolean isFormed() {
        return status == Status.FORMED;
    }

    /** The plate that runs the stack: this one, or the formed partner. */
    private EndPlateBlockEntity controller() {
        if (status == Status.FORMED && !isController() && partner != null && level != null
                && level.getBlockEntity(partner) instanceof EndPlateBlockEntity other && other.isController())
            return other;
        return this;
    }

    private int bufferSize() {
        return 20 * Config.fuelCellMaxLength * Config.fuelCellFePerTick;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (level.getGameTime() % 20 == 0) scan();

        if (isController()) {
            if (status == Status.FORMED && partner != null
                    && level.getBlockEntity(partner) instanceof EndPlateBlockEntity other) {
                // Gas piped into the other plate before the stack formed joins the controller's.
                other.handOver(this);
                run(level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(partner));
            } else {
                run(true);
            }
        }
        // Each plate lights itself, so a broken stack can't leave the far plate lit.
        EndPlateBlockEntity c = controller();
        setLit(worldPosition, c == this ? running : c.running);
        pushEnergy();

        if (level.getGameTime() % 20 == 0) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    // Walks from the plate along the cells to the plate at the other end.
    private void scan() {
        Direction in = facing().getOpposite();
        int count = 0;
        Status found = Status.NO_PLATE;
        BlockPos other = null;
        for (int i = 1; i <= Config.fuelCellMaxLength + 1; i++) {
            BlockPos pos = worldPosition.relative(in, i);
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.FUEL_CELL.get()) && state.getValue(BlockStateProperties.AXIS) == in.getAxis()) {
                count++;
                if (count > Config.fuelCellMaxLength) {
                    found = Status.TOO_LONG;
                    break;
                }
                continue;
            }
            if (count > 0 && state.getBlock() instanceof EndPlateBlock && state.getValue(EndPlateBlock.FACING) == in) {
                found = Status.FORMED;
                other = pos;
            }
            break;
        }
        if (count == 0) found = Status.NO_CELLS;
        status = found;
        cells = found == Status.FORMED ? count : 0;
        partner = other;
    }

    private void run(boolean stopped) {
        boolean canRun = false;
        if (!stopped && status == Status.FORMED) {
            int hydrogen = cells * Config.fuelCellHydrogenPerTick;
            int oxygen = (oxygenRemainder + hydrogen) / 2;
            int fe = cells * Config.fuelCellFePerTick;
            canRun = hydrogenTank.getFluidAmount() >= hydrogen
                    && oxygenTank.getFluidAmount() >= oxygen
                    && waterTank.getSpace() >= hydrogen
                    && bufferSize() - energy >= fe;
            if (canRun) {
                hydrogenTank.drain(hydrogen, IFluidHandler.FluidAction.EXECUTE);
                if (oxygen > 0) oxygenTank.drain(oxygen, IFluidHandler.FluidAction.EXECUTE);
                waterTank.fill(new FluidStack(Fluids.WATER, hydrogen), IFluidHandler.FluidAction.EXECUTE);
                oxygenRemainder = (oxygenRemainder + hydrogen) % 2;
                energy += fe;
                setChanged();
            }
        }
        running = canRun;
    }

    private void setLit(BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof EndPlateBlock && state.getValue(EndPlateBlock.LIT) != lit)
            level.setBlock(pos, state.setValue(EndPlateBlock.LIT, lit), 3);
    }

    // Moves this plate's own tanks into the controller's, once it's the passive end of a stack.
    private void handOver(EndPlateBlockEntity to) {
        move(hydrogenTank, to.hydrogenTank);
        move(oxygenTank, to.oxygenTank);
        move(waterTank, to.waterTank);
        if (energy > 0) {
            int moved = Math.min(energy, to.bufferSize() - to.energy);
            energy -= moved;
            to.energy += moved;
        }
    }

    private static void move(FluidTank from, FluidTank to) {
        if (from.isEmpty()) return;
        int filled = to.fill(from.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) from.drain(filled, IFluidHandler.FluidAction.EXECUTE);
    }

    // Like a Brass Panel: hands FE to whatever touches the plate and takes it.
    private void pushEnergy() {
        EndPlateBlockEntity source = controller();
        if (source.energy <= 0) return;
        for (Direction dir : Direction.values()) {
            BlockPos pos = worldPosition.relative(dir);
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.FUEL_CELL.get()) || state.getBlock() instanceof EndPlateBlock) continue;
            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, dir.getOpposite());
            if (neighbor == null || !neighbor.canReceive()) continue;
            int accepted = neighbor.receiveEnergy(source.energy, false);
            if (accepted > 0) {
                source.energy -= accepted;
                source.setChanged();
                if (source.energy <= 0) return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Capabilities
    // -------------------------------------------------------------------------

    /** The gas port takes Hydrogen and Oxygen, the Water port gives Water; null (buckets) does both. */
    @Nullable
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        if (side == null) return allFluids;
        BlockState state = getBlockState();
        if (side == EndPlateBlock.gasSide(state)) return gasIn;
        if (side == EndPlateBlock.waterSide(state)) return waterOut;
        return null;
    }

    // Always works on the controller's tanks, so either plate can be piped to.
    private class Handler implements IFluidHandler {
        private final boolean takesGas, givesWater;

        Handler(boolean takesGas, boolean givesWater) {
            this.takesGas = takesGas;
            this.givesWater = givesWater;
        }

        private FluidTank[] tanks() {
            EndPlateBlockEntity c = controller();
            return new FluidTank[]{c.hydrogenTank, c.oxygenTank, c.waterTank};
        }

        @Override public int getTanks() { return 3; }
        @Override public FluidStack getFluidInTank(int tank) { return tanks()[tank].getFluid(); }
        @Override public int getTankCapacity(int tank) { return tanks()[tank].getCapacity(); }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return takesGas && tank < 2 && tanks()[tank].isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!takesGas) return 0;
            FluidTank[] t = tanks();
            if (t[0].isFluidValid(resource)) return t[0].fill(resource, action);
            if (t[1].isFluidValid(resource)) return t[1].fill(resource, action);
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return givesWater ? tanks()[2].drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return givesWater ? tanks()[2].drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }

    // Gives the stack's FE out of any face; takes none in.
    private class Energy implements IEnergyStorage {
        @Override public int receiveEnergy(int toReceive, boolean simulate) { return 0; }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            EndPlateBlockEntity c = controller();
            int extracted = Math.max(0, Math.min(toExtract, c.energy));
            if (!simulate && extracted > 0) {
                c.energy -= extracted;
                c.setChanged();
            }
            return extracted;
        }

        @Override public int getEnergyStored() { return controller().energy; }
        @Override public int getMaxEnergyStored() { return controller().bufferSize(); }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    // -------------------------------------------------------------------------
    // Goggles
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.fuel_cell_header").forGoggles(tooltip);

        if (status != Status.FORMED) {
            String key = switch (status) {
                case NO_CELLS -> "fuel_cell_no_cells";
                case TOO_LONG -> "fuel_cell_too_long";
                default -> "fuel_cell_no_plate";
            };
            CreateLang.translate("solar_punk.tooltip." + key, Config.fuelCellMaxLength)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
            return true;
        }

        EndPlateBlockEntity c = controller();
        CreateLang.translate("solar_punk.tooltip.fuel_cell_cells")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(cells + " / " + Config.fuelCellMaxLength).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.output")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(c.running ? cells * Config.fuelCellFePerTick : 0))
                        .withStyle(c.running ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        CreateLang.translate("solar_punk.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.fe(c.energy) + " / " + EnergyFormat.fe(c.bufferSize()))
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);
        GoggleHelper.tank(tooltip, "hydrogen", c.hydrogenTank, ChatFormatting.LIGHT_PURPLE);
        GoggleHelper.tank(tooltip, "oxygen", c.oxygenTank, ChatFormatting.AQUA);
        GoggleHelper.tank(tooltip, "water", c.waterTank, ChatFormatting.BLUE);
        return true;
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy);
        tag.putInt("OxygenRemainder", oxygenRemainder);
        tag.putString("Status", status.name());
        tag.putInt("Cells", cells);
        tag.putBoolean("Running", running);
        if (partner != null) tag.put("Partner", NbtUtils.writeBlockPos(partner));
        FluidTankNBTHelper.save(tag, "HydrogenTank", hydrogenTank);
        FluidTankNBTHelper.save(tag, "OxygenTank", oxygenTank);
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("Energy");
        oxygenRemainder = tag.getInt("OxygenRemainder");
        try {
            status = Status.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException e) {
            status = Status.NO_CELLS;
        }
        cells = tag.getInt("Cells");
        running = tag.getBoolean("Running");
        partner = NbtUtils.readBlockPos(tag, "Partner").orElse(null);
        FluidTankNBTHelper.load(tag, "HydrogenTank", hydrogenTank);
        FluidTankNBTHelper.load(tag, "OxygenTank", oxygenTank);
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
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
