package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.client.sound.FusionReactorSoundInstance;
import net.succ.solar_punk.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Fusion Reactor centerpiece. Unlike the Turbine's stacking-rotor scan, this is a
// single fixed-size voxel-sphere shell scan (Mekanism SPS-inspired, see
// plan_for_fusion.md): the Core sits at the exact center, FusionReactorCasingBlock /
// _GLASS must fill the *entire* shell at rounded-distance CASING_SHELL_DIST (the
// player can already choose the see-through Casing Glass variant there if they want a
// view in - it's in the same reactor_casing tag this scan checks), while the two
// blanket module types only need to fill the three great-circle rings of the band at
// rounded-distance BLANKET_BAND_DIST - see isRingPosition(). The blanket band has no
// glass equivalent, so leaving most of it open is what actually keeps the Core and its
// floating-ring effect visible from outside, regardless of which Casing variant walls
// off the rest. Everything strictly closer than the blanket band is left hollow for
// the renderer's effect. No neighbour-changed plumbing (a radius-4 shell is far outside
// vanilla neighbour notification range anyway) - just a periodic re-scan, same
// convention as TurbineRotorBlockEntity's SCAN_INTERVAL.
public class FusionReactorCoreBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    // Public so FusionReactorScenes/ModPonderProvider can reuse the exact same
    // shell-shape check the BE's own scan uses, instead of each place duplicating (and
    // risking drifting from) this geometry.
    public static final int SCAN_RADIUS = 4;
    public static final int CASING_SHELL_DIST = 4;
    public static final int BLANKET_BAND_DIST = 3;
    private static final int SCAN_INTERVAL = 40;

    // Only the Blanket band is gated to its three orthogonal great-circle rings
    // (dx==0, dy==0, or dz==0) - the Casing shell is a full sphere, since Casing Glass
    // already gives the player a see-through option there without needing gaps in the
    // required positions.
    public static boolean isRingPosition(int dx, int dy, int dz) {
        return dx == 0 || dy == 0 || dz == 0;
    }

    public boolean formed = false;
    public int lithiumModuleCount = 0;
    public int berylliumModuleCount = 0;
    // 0 = pure Lithium blanket (calm, efficient), 1 = pure Beryllium blanket (hot, max
    // output) - see the Tunable knob section of plan_for_fusion.md. Drives the
    // renderer's ring color/pulse and the animateTick particle color.
    public float blanketEfficiency = 0f;

    private int scanCooldown = 1;

    // Client-only ambience state - see tickAudio(). Not saved/synced; each client just
    // re-derives it from the synced `formed` field as chunks load in.
    @OnlyIn(Dist.CLIENT)
    @Nullable
    private FusionReactorSoundInstance activeSound;
    @OnlyIn(Dist.CLIENT)
    private boolean audioInStartupPhase = false;
    @OnlyIn(Dist.CLIENT)
    private boolean clientWasFormed = false;

    public FusionReactorCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        if (--scanCooldown > 0) return;
        scanCooldown = SCAN_INTERVAL;

        boolean wasFormed = formed;
        scanStructure();

        if (formed != wasFormed) {
            setChanged();
            // Flips the block's own FORMED property so its model swaps between the
            // visible placeholder cube and no geometry at all (see
            // FusionReactorCoreBlock/ModBlockStateProvider) - separate from the BE data
            // sync below, which pushes formed/blanketEfficiency/module counts for the
            // renderer and goggle tooltip to read.
            level.setBlock(worldPosition, getBlockState().setValue(FusionReactorCoreBlock.FORMED, formed), 3);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    // Startup -> loop -> shutdown sequencing for the Core's ambience. The startup and
    // shutdown clips are one-shots that just play out; the loop only starts once the
    // startup clip has genuinely finished on its own (checked via SoundManager#isActive,
    // not a hardcoded duration) rather than being started alongside it, and switching to
    // formed=false at any point - mid-startup or mid-loop alike - cuts off whatever's
    // currently playing and plays the shutdown clip instead.
    @OnlyIn(Dist.CLIENT)
    private void tickAudio() {
        if (level == null) return;
        var soundManager = Minecraft.getInstance().getSoundManager();

        if (formed && !clientWasFormed) {
            if (activeSound != null) activeSound.requestStop();
            activeSound = new FusionReactorSoundInstance(ModSounds.FUSION_REACTOR_STARTUP.get(), this, false, false);
            soundManager.play(activeSound);
            audioInStartupPhase = true;
        } else if (!formed && clientWasFormed) {
            if (activeSound != null) activeSound.requestStop();
            activeSound = new FusionReactorSoundInstance(ModSounds.FUSION_REACTOR_SHUTDOWN.get(), this, false, false);
            soundManager.play(activeSound);
            audioInStartupPhase = false;
        } else if (formed && audioInStartupPhase && activeSound != null && !soundManager.isActive(activeSound)) {
            audioInStartupPhase = false;
            activeSound = new FusionReactorSoundInstance(ModSounds.REACTOR_ON_LOOP.get(), this, true, true);
            soundManager.play(activeSound);
        }

        clientWasFormed = formed;
    }

    private void scanStructure() {
        int casingFound = 0, casingRequired = 0;
        int blanketRequired = 0, lithiumFound = 0, berylliumFound = 0;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    int rounded = Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
                    if (rounded == CASING_SHELL_DIST) {
                        casingRequired++;
                        BlockState state = level.getBlockState(worldPosition.offset(dx, dy, dz));
                        if (state.is(ModTags.Blocks.REACTOR_CASING)) casingFound++;
                    } else if (rounded == BLANKET_BAND_DIST && isRingPosition(dx, dy, dz)) {
                        blanketRequired++;
                        BlockState state = level.getBlockState(worldPosition.offset(dx, dy, dz));
                        if (state.is(ModTags.Blocks.REACTOR_BLANKET_LITHIUM)) lithiumFound++;
                        else if (state.is(ModTags.Blocks.REACTOR_BLANKET_BERYLLIUM)) berylliumFound++;
                    }
                }
            }
        }

        lithiumModuleCount = lithiumFound;
        berylliumModuleCount = berylliumFound;
        int blanketFound = lithiumFound + berylliumFound;
        blanketEfficiency = blanketFound > 0 ? (float) berylliumFound / blanketFound : 0f;
        formed = blanketRequired > 0 && casingFound == casingRequired && blanketFound == blanketRequired;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Formed", formed);
        tag.putInt("LithiumModules", lithiumModuleCount);
        tag.putInt("BerylliumModules", berylliumModuleCount);
        tag.putFloat("BlanketEfficiency", blanketEfficiency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        formed = tag.getBoolean("Formed");
        lithiumModuleCount = tag.getInt("LithiumModules");
        berylliumModuleCount = tag.getInt("BerylliumModules");
        blanketEfficiency = tag.getFloat("BlanketEfficiency");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.fusion_reactor_core_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.reactor_status")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(formed ? "Formed" : "Incomplete")
                        .withStyle(formed ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.reactor_blanket")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(lithiumModuleCount + " Li / " + berylliumModuleCount + " Be")
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        return true;
    }
}
