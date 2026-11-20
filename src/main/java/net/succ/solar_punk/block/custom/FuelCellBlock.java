package net.succ.solar_punk.block.custom;

import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.entity.custom.EndPlateBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * One cell of a Fuel Cell stack. It has no block entity: goggles and FE go through the
 * stack's controlling End Plate, so a Wire Connector can sit on any cell.
 */
public class FuelCellBlock extends RotatedPillarBlock implements IProxyHoveringInformation {

    public FuelCellBlock(Properties properties) {
        super(properties);
    }

    /**
     * The controlling plate of the formed stack this cell is in, or null. The controller is
     * the plate at the negative end of the row, facing that way.
     */
    @Nullable
    public static EndPlateBlockEntity controller(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FuelCellBlock)) return null;
        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        Direction toController = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
        for (int i = 1; i <= Config.fuelCellMaxLength; i++) {
            BlockPos p = pos.relative(toController, i);
            if (!level.isLoaded(p)) return null;
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof FuelCellBlock && s.getValue(BlockStateProperties.AXIS) == axis) continue;
            if (s.getBlock() instanceof EndPlateBlock && s.getValue(EndPlateBlock.FACING) == toController
                    && level.getBlockEntity(p) instanceof EndPlateBlockEntity plate && plate.isFormed())
                return plate;
            return null;
        }
        return null;
    }

    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        EndPlateBlockEntity plate = controller(level, pos);
        return plate != null ? plate.getBlockPos() : pos;
    }

    /**
     * The stack's FE, looked up on every call. Capability caches (like the Wire Connector's)
     * keep this object while the cell stays, so it has to follow the stack forming and breaking.
     */
    public record Energy(Level level, BlockPos pos) implements IEnergyStorage {
        @Nullable
        private IEnergyStorage stack() {
            EndPlateBlockEntity plate = controller(level, pos);
            return plate != null ? plate.energyStorage : null;
        }

        @Override public int receiveEnergy(int toReceive, boolean simulate) { return 0; }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            IEnergyStorage s = stack();
            return s != null ? s.extractEnergy(toExtract, simulate) : 0;
        }

        @Override
        public int getEnergyStored() {
            IEnergyStorage s = stack();
            return s != null ? s.getEnergyStored() : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            IEnergyStorage s = stack();
            return s != null ? s.getMaxEnergyStored() : 0;
        }

        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }
}
