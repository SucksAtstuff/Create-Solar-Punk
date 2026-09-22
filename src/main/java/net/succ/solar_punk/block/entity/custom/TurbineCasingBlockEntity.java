package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class TurbineCasingBlockEntity extends BlockEntity {

    public TurbineCasingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public IFluidHandler getFluidHandler() {
        if (level == null) return null;
        // The master rotor can be up to MAX_HEIGHT (plus the floor/cap layers) away
        // along whichever axis the turbine actually grows on - Y for a turbine standing
        // up, X or Z for one built on its side (see TurbineRotorBlockEntity#growthPositive/
        // MAX_HEIGHT). This casing block has no stored axis of its own, so it tries all
        // three: the long search range on one axis, the 7-wide footprint range on the
        // other two.
        IFluidHandler handler = searchForMaster(3, 23, 3);  // Y-axis growth (standing)
        if (handler != null) return handler;
        handler = searchForMaster(23, 3, 3);                // X-axis growth (on its side)
        if (handler != null) return handler;
        return searchForMaster(3, 3, 23);                   // Z-axis growth (on its side)
    }

    @Nullable
    private IFluidHandler searchForMaster(int rangeX, int rangeY, int rangeZ) {
        for (int dx = -rangeX; dx <= rangeX; dx++)
            for (int dy = -rangeY; dy <= rangeY; dy++)
                for (int dz = -rangeZ; dz <= rangeZ; dz++) {
                    BlockEntity be = level.getBlockEntity(worldPosition.offset(dx, dy, dz));
                    if (be instanceof TurbineRotorBlockEntity rotor && rotor.isMaster)
                        return rotor.combinedFluidHandler;
                }
        return null;
    }
}
