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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.FusionReactorCoreBlock;
import net.succ.solar_punk.client.sound.FusionReactorSoundInstance;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;
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

    // Goggle tooltip stats - see addToGoggleTooltip() and plan_for_fusion.md's
    // Advancement/goggle-tooltip/JEI section. steam/deuterium/lithium are the actual
    // amounts runPowerLoop() moved *this tick* (0 whenever it's idle for any reason -
    // unformed, unthrottled, out of fuel, or a full steam tank), not a theoretical max,
    // so goggling a stalled reactor honestly reads "nothing is happening" rather than
    // showing what it could produce under ideal conditions. breedingEfficiencyPercent
    // is different: it's a property of the blanket composition alone (the
    // Config-interpolated breeder_floor_ratio), so it's set in scanStructure() and
    // stays meaningful even while the power loop itself is idle.
    public int steamOutputPerTick = 0;
    public int deuteriumUsedPerTick = 0;
    public int lithiumUsedPerTick = 0;
    public float breedingEfficiencyPercent = 0f;

    private int scanCooldown = 1;

    // Highest redstone signal found on any exterior Casing block during the last
    // structure scan - see scanStructure()'s CASING_SHELL_DIST branch and
    // runPowerLoop()'s throttle. Refreshed on the same SCAN_INTERVAL cadence as the
    // structure scan itself, not read live every tick, since scanning all 210 shell
    // positions for redstone every single tick would be wasteful.
    private int cachedShellSignal = 0;

    // -------------------------------------------------------------------------
    // Power loop - see plan_for_fusion.md's Output/Fuel chain/Config knobs sections.
    // Direct steam output, fed by Deuterium (fluid) + Lithium (item), converted
    // internally at a rate the Config knobs interpolate by blanketEfficiency. There is
    // no Tritium item/fluid - the Lithium->Tritium conversion only ever exists as the
    // math in runPowerLoop() below.
    // -------------------------------------------------------------------------

    // 1 Lithium Dust's worth of mB-equivalent fuel value, matching the mod's existing
    // item<->mB convention (Solar Heater's water_per_salt_mb). Not a Config knob itself -
    // it's just the unit conversion the five real knobs are expressed in.
    private static final int LITHIUM_DUST_MB_EQUIVALENT = 250;

    public final FluidTank steamTank = new FluidTank(Config.fusionReactorSteamTank) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    public final FluidTank deuteriumTank = new FluidTank(Config.fusionReactorDeuteriumTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.DEUTERIUM_SOURCE.get());
        }
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    // Slot 0: Lithium Dust input. No output slot - Lithium is consumed internally, it
    // never comes back out as anything.
    public final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.LITHIUM_DUST.get());
        }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? steamTank.getFluid() : deuteriumTank.getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? steamTank.getCapacity() : deuteriumTank.getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 1 && deuteriumTank.isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!resource.getFluid().isSame(ModFluids.DEUTERIUM_SOURCE.get())) return 0;
            return deuteriumTank.fill(resource, action);
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(ModFluids.STEAM_SOURCE.get()))
                return steamTank.drain(resource, action);
            return FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return steamTank.drain(maxDrain, action);
        }
    };

    // mB-equivalent of Lithium currently "loaded" from consumed Dust items, topped up
    // a whole item at a time (furnace-fuel style) since Lithium can't be drained
    // fractionally the way Deuterium can.
    private float lithiumBufferMb = 0f;

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

        if (--scanCooldown <= 0) {
            scanCooldown = SCAN_INTERVAL;

            boolean wasFormed = formed;
            scanStructure();

            if (formed != wasFormed) {
                setChanged();
                // Flips the block's own FORMED property so its model swaps between the
                // visible placeholder cube and no geometry at all (see
                // FusionReactorCoreBlock/ModBlockStateProvider) - separate from the BE
                // data sync below, which pushes formed/blanketEfficiency/module counts
                // for the renderer and goggle tooltip to read.
                level.setBlock(worldPosition, getBlockState().setValue(FusionReactorCoreBlock.FORMED, formed), 3);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                if (formed) ModTriggers.fireNearby(level, worldPosition, ModTriggers.REACTOR_BUILT);
            }
        }

        // Runs every tick, not just on the (infrequent) structure-scan cadence above -
        // otherwise steam would only ever get produced once every SCAN_INTERVAL ticks.
        runPowerLoop();

        // Plain BlockEntity, not a SmartBlockEntity - unlike the Turbine/extractors it
        // has no built-in periodic client sync, so without this the new goggle-tooltip
        // stats (steamOutputPerTick etc.) would only ever reach the client once, on
        // chunk load or the next `formed` toggle, and then sit stale forever.
        if (level.getGameTime() % 20 == 0)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    // The actual power loop: Deuterium + Lithium in, Steam out, rate interpolated by
    // blanketEfficiency between the pure-Lithium and pure-Beryllium extremes the five
    // Config knobs describe. Throttles to whichever fuel is scarcer this tick rather
    // than assuming both are available, and never overflows the steam tank.
    private void runPowerLoop() {
        int totalModules = lithiumModuleCount + berylliumModuleCount;
        if (!formed || totalModules <= 0) { stallPowerLoop(); return; }

        int steamRoom = steamTank.getCapacity() - steamTank.getFluidAmount();
        if (steamRoom <= 0) { stallPowerLoop(); return; }

        // Fuel-injection-rate throttle (plan_for_fusion.md's secondary, independent
        // lever on top of the blanket-ratio knob) - a redstone signal anywhere on the
        // exterior Casing shell, not a new block/GUI (and not the Core itself, which is
        // buried at dead center and unreachable once built - see scanStructure()). No
        // signal at all means the reactor idles at 0% and pulls no fuel; a lever or
        // comparator at full strength (15) means full throttle.
        float throttle = cachedShellSignal / 15f;
        if (throttle <= 0) { stallPowerLoop(); return; }

        float t = blanketEfficiency;
        double steamPerModule = Config.reactorSteamPerLayerPerTick
                * (Config.reactorLithiumOutputRatio + (1 - Config.reactorLithiumOutputRatio) * t);
        int desiredSteam = Math.min(steamRoom, (int) Math.round(totalModules * steamPerModule * throttle));
        if (desiredSteam <= 0) { stallPowerLoop(); return; }

        double fuelPerSteam = Config.reactorFuelPerSteamMb
                * (1 + (Config.reactorBerylliumFuelWasteMultiplier - 1) * t);
        double breederRatio = 1 - (1 - Config.reactorBreederFloorRatio) * t;
        // mB of Deuterium / Lithium-equivalent needed per mB of steam at this ratio -
        // split so Deuterium and the Lithium that actually converts are drawn 1:1, with
        // the rest of the drawn Lithium accounted for as waste (breederRatio < 1).
        double deuteriumPerSteam = fuelPerSteam / (1 + 1 / breederRatio);
        double lithiumPerSteam = deuteriumPerSteam / breederRatio;

        // Keep the Lithium buffer topped up a whole Dust at a time - Lithium is a
        // discrete item, not a fluid, so it can't be drained fractionally like Deuterium.
        while (lithiumBufferMb < desiredSteam * lithiumPerSteam && !itemHandler.getStackInSlot(0).isEmpty()) {
            itemHandler.extractItem(0, 1, false);
            lithiumBufferMb += LITHIUM_DUST_MB_EQUIVALENT;
        }

        double byDeuterium = deuteriumPerSteam > 0 ? deuteriumTank.getFluidAmount() / deuteriumPerSteam : Double.MAX_VALUE;
        double byLithium = lithiumPerSteam > 0 ? lithiumBufferMb / lithiumPerSteam : Double.MAX_VALUE;
        int actualSteam = (int) Math.min(desiredSteam, Math.floor(Math.min(byDeuterium, byLithium)));
        if (actualSteam <= 0) { stallPowerLoop(); return; }

        int deuteriumUsed = (int) Math.round(actualSteam * deuteriumPerSteam);
        int lithiumUsed = (int) Math.round(actualSteam * lithiumPerSteam);
        deuteriumTank.drain(deuteriumUsed, IFluidHandler.FluidAction.EXECUTE);
        lithiumBufferMb -= actualSteam * lithiumPerSteam;
        steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), actualSteam), IFluidHandler.FluidAction.EXECUTE);

        steamOutputPerTick = actualSteam;
        deuteriumUsedPerTick = deuteriumUsed;
        lithiumUsedPerTick = lithiumUsed;
        setChanged();
    }

    // Zeroes the goggle-tooltip production stats whenever the loop can't run this tick
    // for any reason, so a stalled reactor honestly reads as producing nothing instead
    // of showing the last tick it actually ran.
    private void stallPowerLoop() {
        if (steamOutputPerTick == 0 && deuteriumUsedPerTick == 0 && lithiumUsedPerTick == 0) return;
        steamOutputPerTick = 0;
        deuteriumUsedPerTick = 0;
        lithiumUsedPerTick = 0;
        setChanged();
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
        int shellSignal = 0;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    int rounded = Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
                    if (rounded == CASING_SHELL_DIST) {
                        casingRequired++;
                        BlockPos pos = worldPosition.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(ModTags.Blocks.REACTOR_CASING)) {
                            casingFound++;
                            // Goggle-tooltip proxy (see FusionReactorCasingBlockEntity) -
                            // pushed here rather than each Casing block searching for its
                            // own Core, since this loop already visits every one of them.
                            if (level.getBlockEntity(pos) instanceof FusionReactorCasingBlockEntity casingBE)
                                casingBE.setCorePos(worldPosition);
                        }
                        // Fuel-injection-rate throttle input (see runPowerLoop()) - the
                        // Core itself sits buried at dead center, sealed off by this
                        // exact shell, so it can never be a place a player can put a
                        // lever. Checking every exterior Casing block instead means a
                        // lever/comparator anywhere on the outside of the finished
                        // sphere works, which is the only surface actually reachable
                        // once the reactor is built.
                        int sig = level.getBestNeighborSignal(pos);
                        if (sig > shellSignal) shellSignal = sig;
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
        cachedShellSignal = shellSignal;

        // A property of the blanket composition alone, not of runPowerLoop()'s
        // instantaneous output - see the field comment above.
        breedingEfficiencyPercent = formed
                ? (float) (1 - (1 - Config.reactorBreederFloorRatio) * blanketEfficiency) * 100f
                : 0f;
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
        FluidTankNBTHelper.save(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.save(tag, "DeuteriumTank", deuteriumTank);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putFloat("LithiumBufferMb", lithiumBufferMb);
        tag.putInt("CachedShellSignal", cachedShellSignal);
        tag.putInt("SteamOutputPerTick", steamOutputPerTick);
        tag.putInt("DeuteriumUsedPerTick", deuteriumUsedPerTick);
        tag.putInt("LithiumUsedPerTick", lithiumUsedPerTick);
        tag.putFloat("BreedingEfficiencyPercent", breedingEfficiencyPercent);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        formed = tag.getBoolean("Formed");
        lithiumModuleCount = tag.getInt("LithiumModules");
        berylliumModuleCount = tag.getInt("BerylliumModules");
        blanketEfficiency = tag.getFloat("BlanketEfficiency");
        FluidTankNBTHelper.load(tag, "SteamTank", steamTank);
        FluidTankNBTHelper.load(tag, "DeuteriumTank", deuteriumTank);
        if (tag.contains("Inventory")) itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        lithiumBufferMb = tag.getFloat("LithiumBufferMb");
        cachedShellSignal = tag.getInt("CachedShellSignal");
        steamOutputPerTick = tag.getInt("SteamOutputPerTick");
        deuteriumUsedPerTick = tag.getInt("DeuteriumUsedPerTick");
        lithiumUsedPerTick = tag.getInt("LithiumUsedPerTick");
        breedingEfficiencyPercent = tag.getFloat("BreedingEfficiencyPercent");
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

        if (!formed) return true;

        CreateLang.translate("solar_punk.tooltip.reactor_steam_output")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(steamOutputPerTick).text(" mB/t").style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.reactor_fuel_draw")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(deuteriumUsedPerTick + " D / " + lithiumUsedPerTick + " Li mB/t")
                        .withStyle(ChatFormatting.YELLOW))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.reactor_breeding_efficiency")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number((int) breedingEfficiencyPercent).text("%").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        return true;
    }
}
