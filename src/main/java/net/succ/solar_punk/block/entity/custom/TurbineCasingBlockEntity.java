package net.succ.solar_punk.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

// Lets pipes connect to any Turbine Casing by proxying to the master rotor's tanks.
public class TurbineCasingBlockEntity extends BlockEntity {

    // How far along the turbine's axis the master rotor can be: every blade layer and the
    // cap, plus the floor.
    private static final int AXIAL_REACH = TurbineRotorBlockEntity.MAX_LAYERS + 2;
    // The rotor is at the center of the 7x7 cross-section.
    private static final int RADIAL_REACH = 3;

    public TurbineCasingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public IFluidHandler getFluidHandler() {
        if (level == null) return null;
        // The casing doesn't know which axis its turbine was built along, so try all three.
        IFluidHandler handler = searchForMaster(RADIAL_REACH, AXIAL_REACH, RADIAL_REACH);  // standing (Y)
        if (handler != null) return handler;
        handler = searchForMaster(AXIAL_REACH, RADIAL_REACH, RADIAL_REACH);                // on its side (X)
        if (handler != null) return handler;
        return searchForMaster(RADIAL_REACH, RADIAL_REACH, AXIAL_REACH);                   // on its side (Z)
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
