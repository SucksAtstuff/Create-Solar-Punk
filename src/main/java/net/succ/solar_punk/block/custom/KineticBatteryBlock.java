package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.KineticBatteryBlockEntity;

public class KineticBatteryBlock extends KineticBlock implements IBE<KineticBatteryBlockEntity> {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public KineticBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(AXIS, context.getClickedFace().getAxis())
                .setValue(LIT, false);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.KINETIC_BATTERY.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<KineticBatteryBlockEntity>) (l, p, s, be) -> be.tick();
    }

    @Override
    public Class<KineticBatteryBlockEntity> getBlockEntityClass() {
        return KineticBatteryBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBatteryBlockEntity> getBlockEntityType() {
        return ModBlockEntities.KINETIC_BATTERY.get();
    }
}
