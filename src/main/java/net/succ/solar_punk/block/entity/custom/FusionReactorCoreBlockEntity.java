package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.util.AutomationItemHandler;
import net.succ.solar_punk.util.PonderHelper;
import net.neoforged.neoforge.items.IItemHandler;
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
import net.succ.solar_punk.util.FluidTankNBTHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Fusion Reactor centerpiece, sitting at the exact center of a fixed-size voxel sphere:
//  - Casing (solid or glass, the reactor_casing tag) fills the outer surface of the shell
//    at rounded distance CASING_SHELL_DIST, see isCasingPosition().
//  - Blanket modules fill the three great-circle rings of the band at rounded distance
//    BLANKET_BAND_DIST, see isBlanketPosition(). The rest of that band stays open, which
//    keeps the Core's effect visible through Casing Glass.
//  - Everything inside the blanket band is left hollow for the renderer's effect.
// The structure is re-scanned every SCAN_INTERVAL ticks rather than on neighbour
// updates, since the shell is far outside neighbour notification range anyway.
public class FusionReactorCoreBlockEntity extends BlockEntity implements IHaveGoggleInformation {

    // Public so the Ponder scene and schematic build the exact same shape.
    public static final int CASING_SHELL_DIST = 4;
    public static final int BLANKET_BAND_DIST = 3;
    public static final int SCAN_RADIUS = CASING_SHELL_DIST;
    private static final int SCAN_INTERVAL = 40;

    // The three orthogonal great-circle rings (dx==0, dy==0 or dz==0) the Blanket fills.
    public static boolean isRingPosition(int dx, int dy, int dz) {
        return dx == 0 || dy == 0 || dz == 0;
    }

    private static int roundedDist(int dx, int dy, int dz) {
        return Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz));
    }

    // Casing shell = the outer surface of the rounded-distance-4 band only, i.e. the
    // same hollow sphere WorldEdit's //hsphere or an online sphere generator gives.
    // Band positions with no face neighbour outside it (the 24 at offsets like (0,2,3))
    // aren't required, but Casing there is still allowed.
    public static boolean isCasingPosition(int dx, int dy, int dz) {
        if (roundedDist(dx, dy, dz) != CASING_SHELL_DIST) return false;
        return roundedDist(dx + 1, dy, dz) > CASING_SHELL_DIST || roundedDist(dx - 1, dy, dz) > CASING_SHELL_DIST
                || roundedDist(dx, dy + 1, dz) > CASING_SHELL_DIST || roundedDist(dx, dy - 1, dz) > CASING_SHELL_DIST
                || roundedDist(dx, dy, dz + 1) > CASING_SHELL_DIST || roundedDist(dx, dy, dz - 1) > CASING_SHELL_DIST;
    }

    public static boolean isBlanketPosition(int dx, int dy, int dz) {
        return roundedDist(dx, dy, dz) == BLANKET_BAND_DIST && isRingPosition(dx, dy, dz);
    }

    public boolean formed = false;
    public int lithiumModuleCount = 0;
    public int berylliumModuleCount = 0;
    // 0 = pure Lithium blanket (calm, efficient), 1 = pure Beryllium blanket (hot, max
    // output). Drives the power loop's rates, the renderer's color/speed and the
    // particle color.
    public float blanketEfficiency = 0f;

    // Goggle tooltip stats. steam/deuterium/lithium are what runPowerLoop() actually
    // moved this tick (0 whenever it's idle), so a stalled reactor reads as stalled.
    // breedingEfficiencyPercent depends only on the blanket composition, so it's set in
    // scanStructure() and stays meaningful while idle.
    public int steamOutputPerTick = 0;
    public int deuteriumUsedPerTick = 0;
    public int lithiumUsedPerTick = 0;
    public float breedingEfficiencyPercent = 0f;

    private int scanCooldown = 1;

    // Highest redstone signal on any Casing block during the last structure scan, used as
    // runPowerLoop()'s throttle. Only refreshed every SCAN_INTERVAL, since checking all
    // 186 shell positions every tick would be wasteful.
    private int cachedShellSignal = 0;

    // -------------------------------------------------------------------------
    // Power loop: Deuterium (fluid) + Lithium or Tritium (items) in, Steam out, at rates
    // the Config knobs interpolate by blanketEfficiency. Lithium is bred into Tritium
    // inside the reactor (math in runPowerLoop()); Tritium Capsules from the Particle
    // Accelerator skip that step, and the Breeder Modules slowly fill Empty Fuel Capsules.
    // -------------------------------------------------------------------------

    // How many mB of fuel one Lithium Dust counts as.
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

    public static final int SLOT_LITHIUM = 0, SLOT_TRITIUM = 1, SLOT_EMPTY_CAPSULES = 2, SLOT_BRED_TRITIUM = 3;

    // Lithium Dust in, Tritium Capsules in, Empty Fuel Capsules (burnt ones come out
    // here, and the breeder refills the ones sitting here), and bred Tritium Capsules out.
    public final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_LITHIUM -> stack.is(ModTags.Items.DUSTS_LITHIUM);
                case SLOT_TRITIUM, SLOT_BRED_TRITIUM -> stack.is(ModItems.TRITIUM_CAPSULE.get());
                case SLOT_EMPTY_CAPSULES -> stack.is(ModItems.EMPTY_FUEL_CAPSULE.get());
                default -> false;
            };
        }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    // What pipes, funnels and the Casing see: fuel and empty capsules can be put in, and
    // empty capsules and bred Tritium can be taken out.
    public final IItemHandler itemHandler = new AutomationItemHandler(inventory,
            slot -> slot != SLOT_BRED_TRITIUM,
            (slot, stack) -> slot == SLOT_EMPTY_CAPSULES || slot == SLOT_BRED_TRITIUM);

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

    // mB of Lithium loaded from consumed Dust, topped up a whole item at a time
    // (furnace-fuel style) since an item can't be drained fractionally like Deuterium.
    private float lithiumBufferMb = 0f;
    // Same for Tritium, a whole capsule at a time.
    private float tritiumBufferMb = 0f;
    // Tritium the Breeder Modules have captured towards the next capsule.
    private float bredTritiumMb = 0f;
    // Which fuel the power loop burned last tick, for the goggles.
    public boolean burningTritium = false;

    // Why the power loop did or didn't run on its last tick - read by the Status display
    // source so a stalled reactor says what to fix instead of just reading 0 mB/t.
    // Server-side only and recomputed every tick, so it isn't saved or synced.
    public enum Status { RUNNING, INCOMPLETE, NO_BLANKET, NO_SIGNAL, LOW_SIGNAL, STEAM_FULL, NO_DEUTERIUM, NO_LITHIUM }

    private Status status = Status.INCOMPLETE;

    public Status getStatus() { return status; }

    public int getThrottlePercent() { return Math.round(cachedShellSignal / 15f * 100f); }

    // Buffered Lithium plus every Lithium Dust still waiting in the input slot, in mB.
    public float getLithiumSupplyMb() {
        return lithiumBufferMb + inventory.getStackInSlot(SLOT_LITHIUM).getCount() * LITHIUM_DUST_MB_EQUIVALENT;
    }

    // Buffered Tritium plus every Tritium Capsule waiting in the input slot, in mB.
    public float getTritiumSupplyMb() {
        return tritiumBufferMb + inventory.getStackInSlot(SLOT_TRITIUM).getCount() * (float) Config.reactorTritiumCapsuleMb;
    }

    // Client-only ambience state - see tickAudio(). Not saved/synced; each client just
    // re-derives it from the synced `formed` field as chunks load in.
    @OnlyIn(Dist.CLIENT)
    @Nullable
    private FusionReactorSoundInstance activeSound;
    @OnlyIn(Dist.CLIENT)
    private boolean audioInStartupPhase;
    @OnlyIn(Dist.CLIENT)
    private boolean clientWasFormed;

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
                // FORMED swaps the block model between the cube and no geometry (see
                // FusionReactorCoreBlock). The BE data is synced separately below.
                level.setBlock(worldPosition, getBlockState().setValue(FusionReactorCoreBlock.FORMED, formed), 3);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
                if (formed) ModTriggers.fireNearby(level, worldPosition, ModTriggers.REACTOR_BUILT);
            }
        }

        runPowerLoop();

        // Plain BlockEntity with no built-in client sync, so push the goggle stats
        // periodically.
        if (level.getGameTime() % 20 == 0)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    // The actual power loop: Deuterium + Lithium (or Tritium) in, Steam out, rate
    // interpolated by blanketEfficiency between the pure-Lithium and pure-Beryllium
    // extremes the Config knobs describe. Throttles to whichever fuel is scarcer this tick rather
    // than assuming both are available, and never overflows the steam tank.
    private void runPowerLoop() {
        int totalModules = lithiumModuleCount + berylliumModuleCount;
        if (!formed) { stallPowerLoop(Status.INCOMPLETE); return; }
        if (totalModules <= 0) { stallPowerLoop(Status.NO_BLANKET); return; }

        int steamRoom = steamTank.getCapacity() - steamTank.getFluidAmount();
        if (steamRoom <= 0) { stallPowerLoop(Status.STEAM_FULL); return; }

        // Fuel injection throttle: a redstone signal anywhere on the Casing shell. No
        // signal idles the reactor at 0%, full strength (15) is full throttle.
        float throttle = cachedShellSignal / 15f;
        if (throttle <= 0) { stallPowerLoop(Status.NO_SIGNAL); return; }

        float t = blanketEfficiency;
        double steamPerModule = Config.reactorSteamPerLayerPerTick
                * (Config.reactorLithiumOutputRatio + (1 - Config.reactorLithiumOutputRatio) * t);
        int desiredSteam = Math.min(steamRoom, (int) Math.round(totalModules * steamPerModule * throttle));
        if (desiredSteam <= 0) { stallPowerLoop(Status.LOW_SIGNAL); return; }

        double fuelPerSteam = Config.reactorFuelPerSteamMb
                * (1 + (Config.reactorBerylliumFuelWasteMultiplier - 1) * t);
        double breederRatio = 1 - (1 - Config.reactorBreederFloorRatio) * t;
        // mB of Deuterium / Lithium needed per mB of steam. Deuterium and the Lithium that
        // actually breeds are drawn 1:1; the rest of the drawn Lithium is waste
        // (breederRatio < 1).
        double deuteriumPerSteam = fuelPerSteam / (1 + 1 / breederRatio);
        double lithiumPerSteam = deuteriumPerSteam / breederRatio;

        // Tritium Capsules go first. Tritium is what Lithium breeds into, so it burns 1:1
        // with Deuterium, with none of the breeding waste. A leftover too small for even
        // 1 mB of steam doesn't count, or the reactor would stall on it instead of moving
        // on to Lithium; it's kept for when the next capsule loads.
        boolean tritium = tritiumBufferMb >= deuteriumPerSteam || canLoadTritiumCapsule();
        double fuelPerSteam2 = tritium ? deuteriumPerSteam : lithiumPerSteam;

        // Top the buffer up a whole item at a time.
        if (tritium) {
            while (tritiumBufferMb < desiredSteam * fuelPerSteam2 && canLoadTritiumCapsule()) {
                inventory.extractItem(SLOT_TRITIUM, 1, false);
                // hand the capsule back, empty
                inventory.insertItem(SLOT_EMPTY_CAPSULES, new ItemStack(ModItems.EMPTY_FUEL_CAPSULE.get()), false);
                tritiumBufferMb += Config.reactorTritiumCapsuleMb;
            }
        } else {
            while (lithiumBufferMb < desiredSteam * fuelPerSteam2 && !inventory.getStackInSlot(SLOT_LITHIUM).isEmpty()) {
                inventory.extractItem(SLOT_LITHIUM, 1, false);
                lithiumBufferMb += LITHIUM_DUST_MB_EQUIVALENT;
            }
        }
        float buffer = tritium ? tritiumBufferMb : lithiumBufferMb;

        double byDeuterium = deuteriumPerSteam > 0 ? deuteriumTank.getFluidAmount() / deuteriumPerSteam : Double.MAX_VALUE;
        double byFuel = fuelPerSteam2 > 0 ? buffer / fuelPerSteam2 : Double.MAX_VALUE;
        int actualSteam = (int) Math.min(desiredSteam, Math.floor(Math.min(byDeuterium, byFuel)));
        if (actualSteam <= 0) {
            stallPowerLoop(byDeuterium < 1 ? Status.NO_DEUTERIUM : Status.NO_LITHIUM);
            return;
        }

        int deuteriumUsed = (int) Math.round(actualSteam * deuteriumPerSteam);
        int fuelUsed = (int) Math.round(actualSteam * fuelPerSteam2);
        deuteriumTank.drain(deuteriumUsed, IFluidHandler.FluidAction.EXECUTE);
        if (tritium) {
            tritiumBufferMb -= actualSteam * fuelPerSteam2;
        } else {
            lithiumBufferMb -= actualSteam * fuelPerSteam2;
            // Lithium bred into Tritium, 1:1 with the Deuterium it fuses with
            captureBredTritium(actualSteam * deuteriumPerSteam, totalModules);
        }
        steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), actualSteam), IFluidHandler.FluidAction.EXECUTE);

        steamOutputPerTick = actualSteam;
        deuteriumUsedPerTick = deuteriumUsed;
        lithiumUsedPerTick = fuelUsed;
        burningTritium = tritium;
        status = Status.RUNNING;
        setChanged();
    }

    // A capsule can be loaded when there's one waiting and room to hand the empty back.
    private boolean canLoadTritiumCapsule() {
        if (inventory.getStackInSlot(SLOT_TRITIUM).isEmpty()) return false;
        ItemStack empties = inventory.getStackInSlot(SLOT_EMPTY_CAPSULES);
        return empties.isEmpty() || empties.getCount() < empties.getMaxStackSize();
    }

    // The Breeder Modules capture a little of the Tritium the blanket breeds into Empty
    // Fuel Capsules sitting in the reactor - a slow trickle, scaled by how much of the
    // blanket is Lithium Breeder Modules.
    private void captureBredTritium(double bredMb, int totalModules) {
        if (lithiumModuleCount <= 0 || Config.reactorBreederCaptureRatio <= 0) return;
        int capsuleMb = Config.reactorTritiumCapsuleMb;
        bredTritiumMb = (float) Math.min(capsuleMb,
                bredTritiumMb + bredMb * Config.reactorBreederCaptureRatio * lithiumModuleCount / totalModules);
        if (bredTritiumMb < capsuleMb || inventory.getStackInSlot(SLOT_EMPTY_CAPSULES).isEmpty()) return;
        ItemStack capsule = new ItemStack(ModItems.TRITIUM_CAPSULE.get());
        if (!inventory.insertItem(SLOT_BRED_TRITIUM, capsule, true).isEmpty()) return;
        inventory.extractItem(SLOT_EMPTY_CAPSULES, 1, false);
        inventory.insertItem(SLOT_BRED_TRITIUM, capsule, false);
        bredTritiumMb -= capsuleMb;
        ModTriggers.fireNearby(level, worldPosition, ModTriggers.TRITIUM_BRED);
    }

    // Zeroes the goggle stats whenever the loop can't run, so a stalled reactor reads as
    // producing nothing instead of showing the last tick it ran.
    private void stallPowerLoop(Status reason) {
        status = reason;
        if (steamOutputPerTick == 0 && deuteriumUsedPerTick == 0 && lithiumUsedPerTick == 0) return;
        steamOutputPerTick = 0;
        deuteriumUsedPerTick = 0;
        lithiumUsedPerTick = 0;
        setChanged();
    }

    // Startup -> loop -> shutdown sequencing for the Core's ambience. The loop starts
    // once the startup clip has finished (SoundManager#isActive), and unforming at any
    // point cuts off whatever is playing and plays the shutdown clip. Volumes are
    // matched to the other machine sounds (see MachineSoundInstance); the loop is the
    // loudest clip and runs continuously, so it's the quietest.
    @OnlyIn(Dist.CLIENT)
    private void tickAudio() {
        if (PonderHelper.isPonder(level)) return;  // Ponder scenes stay silent
        if (level == null) return;
        var soundManager = Minecraft.getInstance().getSoundManager();

        if (formed && !clientWasFormed) {
            if (activeSound != null) activeSound.requestStop();
            activeSound = new FusionReactorSoundInstance(ModSounds.FUSION_REACTOR_STARTUP.get(), this, false, false, 0.08f);
            soundManager.play(activeSound);
            audioInStartupPhase = true;
        } else if (!formed && clientWasFormed) {
            if (activeSound != null) activeSound.requestStop();
            activeSound = new FusionReactorSoundInstance(ModSounds.FUSION_REACTOR_SHUTDOWN.get(), this, false, false, 0.06f);
            soundManager.play(activeSound);
            audioInStartupPhase = false;
        } else if (formed && audioInStartupPhase && activeSound != null && !soundManager.isActive(activeSound)) {
            audioInStartupPhase = false;
            activeSound = new FusionReactorSoundInstance(ModSounds.REACTOR_ON_LOOP.get(), this, true, true, 0.05f);
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
                    if (isCasingPosition(dx, dy, dz)) {
                        casingRequired++;
                        BlockPos pos = worldPosition.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(ModTags.Blocks.REACTOR_CASING)) {
                            casingFound++;
                            // Tell the Casing where its Core is (see
                            // FusionReactorCasingBlockEntity), since we visit them all anyway.
                            if (level.getBlockEntity(pos) instanceof FusionReactorCasingBlockEntity casingBE)
                                casingBE.setCorePos(worldPosition);
                        }
                        // Throttle input for runPowerLoop(). The Casing is the only part
                        // reachable once the reactor is built.
                        int sig = level.getBestNeighborSignal(pos);
                        if (sig > shellSignal) shellSignal = sig;
                    } else if (isBlanketPosition(dx, dy, dz)) {
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
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putFloat("LithiumBufferMb", lithiumBufferMb);
        tag.putFloat("TritiumBufferMb", tritiumBufferMb);
        tag.putFloat("BredTritiumMb", bredTritiumMb);
        tag.putBoolean("BurningTritium", burningTritium);
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
        if (tag.contains("Inventory")) {
            // Read into a copy first: reactors saved before Tritium had a 1-slot inventory,
            // and deserializing straight in would shrink this one to 1 slot.
            ItemStackHandler saved = new ItemStackHandler();
            saved.deserializeNBT(registries, tag.getCompound("Inventory"));
            for (int slot = 0; slot < inventory.getSlots(); slot++)
                inventory.setStackInSlot(slot, slot < saved.getSlots() ? saved.getStackInSlot(slot) : ItemStack.EMPTY);
        }
        lithiumBufferMb = tag.getFloat("LithiumBufferMb");
        tritiumBufferMb = tag.getFloat("TritiumBufferMb");
        bredTritiumMb = tag.getFloat("BredTritiumMb");
        burningTritium = tag.getBoolean("BurningTritium");
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
                .add(Component.literal(deuteriumUsedPerTick + " D / " + lithiumUsedPerTick
                                + (burningTritium ? " T mB/t" : " Li mB/t"))
                        .withStyle(ChatFormatting.YELLOW))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.reactor_breeding_efficiency")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number((int) breedingEfficiencyPercent).text("%").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        return true;
    }
}
