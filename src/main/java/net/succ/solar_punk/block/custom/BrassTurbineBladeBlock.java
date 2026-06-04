package net.succ.solar_punk.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BrassTurbineBladeBlock extends Block implements IWrenchable {

    public static final BooleanProperty HIDDEN = BooleanProperty.create("hidden");

    public BrassTurbineBladeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HIDDEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HIDDEN);
    }
}