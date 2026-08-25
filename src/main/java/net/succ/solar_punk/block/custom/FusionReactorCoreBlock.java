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

// The reactor's centerpiece. Per plan_for_fusion.md's Visuals section, all the real
// visual interest comes from FusionReactorCoreRenderer's code-only glowing-sphere-and-
// rings effect once the shell/blanket scan reports the structure formed, plus the
// crackle particles spawned here. The block itself is a plain visible cube while the
// structure is incomplete (so there's something to see/place/aim at), but swaps to a
// no-geometry model the moment it's formed (see FORMED/ModBlockStateProvider) - a solid
// cube around the Core would otherwise entomb the glow effect instead of framing it.
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

        // Doc-Ock-style energy crackle: textureless colored dust particles orbiting the
        // core, tied to the blanket ratio the same way the renderer's rings are -
        // calmer blue-white at Lithium-heavy composition, hotter orange-white at
        // Beryllium-heavy. Client-only (animateTick is never called server-side), so no
        // extra ticker plumbing is needed just for this.
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
        // Ticks on both sides now (unlike most of this mod's BEs) - the server side
        // still does the structure scan, but the client side also needs its own ticker
        // to drive the startup/loop/shutdown ambience sequencing in
        // FusionReactorCoreBlockEntity#tickAudio.
        if (type != ModBlockEntities.FUSION_REACTOR_CORE.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<FusionReactorCoreBlockEntity>) (l, p, s, be) -> be.tick();
    }
}
