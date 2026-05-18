package net.succ.solar_punk.block.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import org.jetbrains.annotations.Nullable;

public class FermentationVatBlock extends Block implements EntityBlock, IWrenchable {

    public enum VatPosition implements StringRepresentable {
        SINGLE, BOTTOM, MIDDLE, TOP;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<VatPosition> POSITION = EnumProperty.create("position", VatPosition.class);

    public FermentationVatBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LIT, false)
                .setValue(POSITION, VatPosition.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POSITION);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.getBlock() == state.getBlock()) return;
        if (movedByPiston) return;
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof FermentationVatBlockEntity be)
            ConnectivityHandler.formMulti(be);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (entity instanceof Player player && player.getPersistentData().contains("SilenceVatSound"))
            return FluidTankBlock.SILENCED_METAL;
        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FermentationVatBlockEntity be) {
                if (be.isController()) {
                    for (int i = 0; i < be.itemHandler.getSlots(); i++)
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                                be.itemHandler.getStackInSlot(i));
                }
                ConnectivityHandler.splitMulti(be);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermentationVatBlockEntity(ModBlockEntities.FERMENTATION_VAT.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ModBlockEntities.FERMENTATION_VAT.get())
            return (l, p, s, be) -> ((FermentationVatBlockEntity) be).tick();
        return null;
    }
}
