package net.succ.solar_punk.block.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.SolarPowerTowerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class SolarPowerTowerBlock extends Block implements EntityBlock, IWrenchable {

    public enum TowerPosition implements StringRepresentable {
        SINGLE, BOTTOM, MIDDLE, TOP;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<TowerPosition> POSITION = EnumProperty.create("position", TowerPosition.class);

    public SolarPowerTowerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LIT, false)
                .setValue(POSITION, TowerPosition.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POSITION);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (entity instanceof Player player && player.getPersistentData().contains("SilenceTowerSound"))
            return FluidTankBlock.SILENCED_METAL;
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.getBlock() == state.getBlock()) return;
        if (movedByPiston) return;
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof SolarPowerTowerBlockEntity be)
            ConnectivityHandler.formMulti(be);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SolarPowerTowerBlockEntity be)
                ConnectivityHandler.splitMulti(be);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof SolarPowerTowerBlockEntity be))
            return InteractionResult.PASS;
        SolarPowerTowerBlockEntity ctrl = be.getControllerBE();
        if (ctrl == null) return InteractionResult.PASS;
        ctrl.steamMode = !ctrl.steamMode;
        if (ctrl.steamMode) ctrl.saltTank.setFluid(FluidStack.EMPTY);
        else                ctrl.steamTank.setFluid(FluidStack.EMPTY);
        ctrl.setChanged();
        ctrl.syncToClients();
        Player player = context.getPlayer();
        if (player != null) {
            String key = ctrl.steamMode
                    ? "solarpunk.tooltip.tower_mode_steam"
                    : "solarpunk.tooltip.tower_mode_salt";
            player.displayClientMessage(Component.translatable(key), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPowerTowerBlockEntity(ModBlockEntities.SOLAR_POWER_TOWER.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ModBlockEntities.SOLAR_POWER_TOWER.get())
            return (l, p, s, be) -> ((SolarPowerTowerBlockEntity) be).tick();
        return null;
    }
}