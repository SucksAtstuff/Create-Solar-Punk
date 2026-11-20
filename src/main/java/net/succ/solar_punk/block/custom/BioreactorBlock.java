package net.succ.solar_punk.block.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.custom.FermentationVatBlock.VatPosition;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.BioreactorBlockEntity;
import net.succ.solar_punk.particle.AlgaeParticles;
import org.jetbrains.annotations.Nullable;

/**
 * The glass culture tank of the Bioreactor: a Create-style multiblock (max 3 wide, 8 tall)
 * that sits on a layer of Bioreactor Base blocks. The controller holds everything; see
 * {@link BioreactorBlockEntity}.
 */
public class BioreactorBlock extends Block implements EntityBlock, IWrenchable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<VatPosition> POSITION = FermentationVatBlock.POSITION;

    // Placing a whole layer at once plays one sound, not one per block (see BioreactorItem).
    private static final SoundType SILENCED_GLASS = new SoundType(0.1f, 1.5f, SoundEvents.GLASS_BREAK,
            SoundEvents.GLASS_STEP, SoundEvents.GLASS_PLACE, SoundEvents.GLASS_HIT, SoundEvents.GLASS_FALL);

    public BioreactorBlock(Properties properties) {
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
        if (level.getBlockEntity(pos) instanceof BioreactorBlockEntity be)
            ConnectivityHandler.formMulti(be);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BioreactorBlockEntity be) {
                if (be.isController())
                    ItemHelper.dropContents(level, pos, be.inventory);
                ConnectivityHandler.splitMulti(be);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (entity instanceof Player player && player.getPersistentData().contains("SilenceVatSound"))
            return SILENCED_GLASS;
        return super.getSoundType(state, level, pos, entity);
    }

    // A bucket fills or empties the culture tank; Kelp or Seagrass seeds it.
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof BioreactorBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return be.useItem(level, player, hand, stack, be.tankFluidHandler);
    }

    // Glass: let light and the view through, like Create's tank windows.
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    // How much of the tank is Algae Culture.
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BioreactorBlockEntity be && be.getControllerBE() instanceof BioreactorBlockEntity ctrl
                ? ctrl.cultureSignal() : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (!(level.getBlockEntity(pos) instanceof BioreactorBlockEntity be) || !be.isController()) return;
        AlgaeParticles.spawn(level, pos.getX(), pos.getZ(), pos.getY() + BioreactorBlockEntity.HULL,
                be.getWidth(), be.cultureDepth());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BioreactorBlockEntity(ModBlockEntities.BIOREACTOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Ticks on both sides: the client side drives the bubbling sound.
        if (type != ModBlockEntities.BIOREACTOR.get()) return null;
        return (l, p, s, be) -> ((BioreactorBlockEntity) be).tick();
    }
}
