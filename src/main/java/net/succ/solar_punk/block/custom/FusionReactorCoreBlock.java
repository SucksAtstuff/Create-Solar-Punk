package net.succ.solar_punk.block.custom;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.succ.solar_punk.block.entity.ModBlockEntities;
import net.succ.solar_punk.block.entity.custom.FusionReactorCoreBlockEntity;
import org.joml.Vector3f;

// The reactor's centerpiece. Once formed, the visuals come from FusionReactorCoreRenderer
// plus the crackle particles spawned here. The block is a plain cube while the structure
// is incomplete, and swaps to an empty model once formed (see FORMED and
// ModBlockStateProvider) so it doesn't hide the glow effect.
public class FusionReactorCoreBlock extends Block implements IBE<FusionReactorCoreBlockEntity> {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public FusionReactorCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof FusionReactorCoreBlockEntity be) || !be.formed) return;

        // Energy crackle: colored dust orbiting the core, tinted by blanket ratio like the
        // renderer's rings (blue-white when Lithium-heavy, orange-white when Beryllium-heavy).
        float t = be.blanketEfficiency;
        float r = 0.5f + 0.5f * t;
        float g = 0.65f - 0.15f * t;
        float b = 1.0f - 0.55f * t;
        float scale = 0.7f + 0.4f * t;
        DustParticleOptions options = new DustParticleOptions(new Vector3f(r, g, b), scale);

        double angle = random.nextDouble() * Math.PI * 2;
        double orbitRadius = 1.1 + random.nextDouble() * 0.7;
        double x = pos.getX() + 0.5 + Math.cos(angle) * orbitRadius;
        double z = pos.getZ() + 0.5 + Math.sin(angle) * orbitRadius;
        double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 1.6;
        level.addParticle(options, x, y, z, 0, 0, 0);
    }

    @Override
    public Class<FusionReactorCoreBlockEntity> getBlockEntityClass() {
        return FusionReactorCoreBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FusionReactorCoreBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FUSION_REACTOR_CORE.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Ticks on both sides: the server scans the structure, the client drives the
        // startup/loop/shutdown sounds (see FusionReactorCoreBlockEntity#tickAudio).
        if (type != ModBlockEntities.FUSION_REACTOR_CORE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<FusionReactorCoreBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
