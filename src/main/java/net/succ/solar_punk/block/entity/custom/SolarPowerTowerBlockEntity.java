package net.succ.solar_punk.block.entity.custom;

import net.succ.solar_punk.util.GoggleHelper;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.block.custom.SolarPowerTowerBlock;
import net.succ.solar_punk.fluid.ModFluids;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.succ.solar_punk.util.FluidTankNBTHelper;
import net.succ.solar_punk.util.SolarHelper;

public class SolarPowerTowerBlockEntity extends MultiBlockFluidBE<SolarPowerTowerBlockEntity>
        implements IHaveGoggleInformation {

    public  boolean steamMode           = false;
    private float saltAccumulator      = 0f;
    private float steamAccumulator     = 0f;
    private int   cachedMirrorCount    = 0;
    private int   mirrorScanCooldown   = 0;
    private boolean advancementFired   = false;
    private float lastGenerationRate   = 0f;

    // Mirrors that have linked themselves to this tower controller. Membership is
    // decided by this tower (see updateMirrors()), not by the mirrors scanning outward.
    private final Set<BlockPos> registeredMirrors = new LinkedHashSet<>();

    public final FluidTank waterTank = new FluidTank(Config.solarPowerTowerTankPerBlock) {
        @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(Fluids.WATER); }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final FluidTank saltTank = new FluidTank(Config.solarPowerTowerTankPerBlock) {
        @Override public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.MOLTEN_SALT_SOURCE.get());
        }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final FluidTank steamTank = new FluidTank(Config.solarPowerTowerTankPerBlock) {
        @Override public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get());
        }
        @Override protected void onContentsChanged() { setChanged(); sync(); }
    };

    public final IFluidHandler combinedFluidHandler = new IFluidHandler() {
        private SolarPowerTowerBlockEntity ctrl() { return getControllerBE(); }

        @Override public int getTanks() { return 3; }

        @Override public FluidStack getFluidInTank(int tank) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return FluidStack.EMPTY;
            return switch (tank) {
                case 0 -> c.waterTank.getFluid();
                case 1 -> c.saltTank.getFluid();
                case 2 -> c.steamTank.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override public int getTankCapacity(int tank) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return 0;
            return switch (tank) {
                case 0 -> c.waterTank.getCapacity();
                case 1 -> c.saltTank.getCapacity();
                case 2 -> c.steamTank.getCapacity();
                default -> 0;
            };
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(Fluids.WATER);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return 0;
            return resource.getFluid().isSame(Fluids.WATER) ? c.waterTank.fill(resource, action) : 0;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return FluidStack.EMPTY;
            if (c.steamMode && resource.getFluid().isSame(ModFluids.STEAM_SOURCE.get()))
                return c.steamTank.drain(resource, action);
            if (!c.steamMode && resource.getFluid().isSame(ModFluids.MOLTEN_SALT_SOURCE.get()))
                return c.saltTank.drain(resource, action);
            return FluidStack.EMPTY;
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            SolarPowerTowerBlockEntity c = ctrl();
            if (c == null) return FluidStack.EMPTY;
            return c.steamMode ? c.steamTank.drain(maxDrain, action) : c.saltTank.drain(maxDrain, action);
        }
    };

    public SolarPowerTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SolarPowerTowerBlockEntity.class);
    }

    // -------------------------------------------------------------------------
    // IMultiBlockEntityContainer.Fluid
    // -------------------------------------------------------------------------

    @Override
    protected void updatePosition() {
        SolarPowerTowerBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return;
        int yOffset = worldPosition.getY() - ctrl.worldPosition.getY();
        boolean alone  = ctrl.height == 1;
        boolean isBot  = yOffset == 0;
        boolean isTop  = yOffset == ctrl.height - 1;
        SolarPowerTowerBlock.TowerPosition pos = alone  ? SolarPowerTowerBlock.TowerPosition.SINGLE
                                               : isBot  ? SolarPowerTowerBlock.TowerPosition.BOTTOM
                                               : isTop  ? SolarPowerTowerBlock.TowerPosition.TOP
                                                        : SolarPowerTowerBlock.TowerPosition.MIDDLE;
        BlockState state = getBlockState();
        if (state.getValue(SolarPowerTowerBlock.POSITION) != pos)
            level.setBlock(worldPosition, state.setValue(SolarPowerTowerBlock.POSITION, pos), 2);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? maxHeight(width) : MAX_WIDTH;
    }

    // Wider towers can be built taller: 1 wide -> 5, 2 wide -> 10, 3 wide -> 20.
    private static int maxHeight(int width) {
        return switch (width) {
            case 2 -> 10;
            case 3 -> 20;
            default -> 5;
        };
    }

    @Override public int getTankSize(int tank) { return Config.solarPowerTowerTankPerBlock; }

    @Override
    public void setTankSize(int tank, int blocks) {
        int newCap = Config.solarPowerTowerTankPerBlock * Math.max(blocks, 1);
        waterTank.setCapacity(newCap);
        saltTank.setCapacity(newCap);
        steamTank.setCapacity(newCap);
        if (waterTank.getFluidAmount() > newCap) waterTank.setFluid(new FluidStack(waterTank.getFluid().getFluid(), newCap));
        if (saltTank.getFluidAmount()  > newCap) saltTank.setFluid(new FluidStack(saltTank.getFluid().getFluid(), newCap));
        if (steamTank.getFluidAmount() > newCap) steamTank.setFluid(new FluidStack(steamTank.getFluid().getFluid(), newCap));
    }

    @Override public IFluidTank getTank(int tank) { return waterTank; }
    @Override public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti(this);
        }

        if (!isController()) return;

        if (--mirrorScanCooldown <= 0) {
            mirrorScanCooldown = 40;
            updateMirrors();
        }

        if (width < Config.solarPowerTowerMinWidth || height < Config.solarPowerTowerMinHeight) {
            saltAccumulator = 0f;
            steamAccumulator = 0f;
            lastGenerationRate = 0f;
            advancementFired = false;
            setLit(false);
            return;
        }
        if (!advancementFired) {
            advancementFired = true;
            ModTriggers.fireNearby(level, worldPosition, ModTriggers.TOWER_BUILT);
        }

        if (!isSunActive()) {
            saltAccumulator = 0f;
            steamAccumulator = 0f;
            lastGenerationRate = 0f;
            setLit(false);
            return;
        }

        // Rate scales super-linearly with height (exponent 1.5) so a tall tower is always
        // more block-efficient than several short ones. baseRate is 9 at max size (3x3x20,
        // full sun, full mirror field), and the configurable mode multipliers turn it into
        // mB/t: steam defaults to 21 mB/t (one max-height Steam Turbine), salt to 18 mB/t.
        // Salt is only made by day, while a charged Heat Battery drains 1 mB/t around the
        // clock, so 18 mB/t averages out to 9 mB/t: enough for 9 Heat Batteries (a max-size
        // boiler) if the tank has room to bank the daytime surplus.
        float heightFraction = (float) height / maxHeight(width);
        float baseRate = (width * width) * (float) Math.pow(heightFraction, 1.5) * mirrorEfficiency();
        float rate = baseRate * (float) (steamMode ? Config.solarPowerTowerSteamMultiplier : Config.solarPowerTowerSaltMultiplier);
        rate *= SolarHelper.outputMultiplier(level, worldPosition);
        if (steamMode) {
            saltAccumulator = 0f;
            steamAccumulator = produce(steamTank, ModFluids.STEAM_SOURCE.get(), rate, steamAccumulator);
        } else {
            steamAccumulator = 0f;
            saltAccumulator = produce(saltTank, ModFluids.MOLTEN_SALT_SOURCE.get(), rate, saltAccumulator);
        }

        setLit(true);
        if (level.getGameTime() % 20 == 0) sync();
    }

    // Adds rate mB/t of fluid to the output tank, carrying fractions over in the
    // accumulator. Every mB costs one mB of water per footprint block. Returns the new
    // accumulator value. (Fully qualified because IMultiBlockEntityContainer.Fluid
    // shadows the import.)
    private float produce(FluidTank output, net.minecraft.world.level.material.Fluid fluid,
                          float rate, float accumulator) {
        int waterPerMb = width * width;
        boolean hasSpaceForOne = output.fill(new FluidStack(fluid, 1), IFluidHandler.FluidAction.SIMULATE) >= 1;
        boolean hasWaterForOne = waterTank.getFluidAmount() >= waterPerMb;
        boolean running = hasSpaceForOne && hasWaterForOne;
        lastGenerationRate = running ? rate : 0f;
        if (running) accumulator += rate;

        int toAdd = (int) accumulator;
        if (toAdd >= 1) {
            int maxByWater = waterTank.getFluidAmount() / waterPerMb;
            int maxBySpace = output.fill(new FluidStack(fluid, toAdd), IFluidHandler.FluidAction.SIMULATE);
            int actualToAdd = Math.min(toAdd, Math.min(maxByWater, maxBySpace));
            if (actualToAdd >= 1) {
                waterTank.drain(actualToAdd * waterPerMb, IFluidHandler.FluidAction.EXECUTE);
                output.fill(new FluidStack(fluid, actualToAdd), IFluidHandler.FluidAction.EXECUTE);
                accumulator -= actualToAdd;
            }
        }
        return accumulator;
    }

    private boolean isSunActive() {
        if (level == null) return false;
        if (!SolarHelper.isDaytime(level, worldPosition) || SolarHelper.isThundering(level) || SolarHelper.isRaining(level)) return false;
        return SolarHelper.canSeeSky(level, worldPosition.above(height));
    }

    // -------------------------------------------------------------------------
    // Mirror field: registration, occlusion checks, efficiency
    // -------------------------------------------------------------------------

    // How far out this tower will look for mirrors to link, scaling with its own height
    // like a real heliostat field scales with receiver height.
    private int mirrorRadius() {
        int radius = Config.solarPowerTowerMirrorBaseRadius + height * Config.solarPowerTowerMirrorRadiusPerHeight;
        return Math.min(radius, Config.solarPowerTowerMirrorMaxRadius);
    }

    private void updateMirrors() {
        registeredMirrors.removeIf(pos -> !isMirrorStillValid(pos));

        if (registeredMirrors.size() < Config.solarPowerTowerMaxTrackedMirrors) {
            int radius = mirrorRadius();
            int minY = worldPosition.getY() - 4;
            int maxY = worldPosition.getY() + 4;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            outer:
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    for (int y = minY; y <= maxY; y++) {
                        if (registeredMirrors.size() >= Config.solarPowerTowerMaxTrackedMirrors) break outer;
                        cursor.set(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                        if (registeredMirrors.contains(cursor)) continue;
                        tryRegisterMirror(cursor);
                    }
                }
            }
        }

        cachedMirrorCount = registeredMirrors.size();
        setChanged();
        sync(); // the goggle tooltip reads the client's synced copy
    }

    private void tryRegisterMirror(BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) return;
        if (!level.getBlockState(pos).is(ModBlocks.SOLAR_MIRROR.get())) return;
        if (!(level.getBlockEntity(pos) instanceof SolarMirrorBlockEntity mirrorBE)) return;
        BlockPos existingLink = mirrorBE.getLinkedTower();
        if (existingLink != null && !existingLink.equals(worldPosition)) {
            // Only respect the existing claim if that tower still actually exists and is
            // still a controller there - otherwise it's a stale link left over from a
            // tower that was rebuilt/removed, and this tower should be free to take it.
            if (level.isLoaded(existingLink)
                    && level.getBlockEntity(existingLink) instanceof SolarPowerTowerBlockEntity otherTower
                    && otherTower.isController())
                return;
        }
        if (!hasSkyAndLineOfSight(pos)) return;
        registeredMirrors.add(pos.immutable());
        mirrorBE.setLinkedTower(worldPosition);
    }

    private boolean isMirrorStillValid(BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) return true; // unloaded: keep it, recheck once it loads again
        if (!level.getBlockState(pos).is(ModBlocks.SOLAR_MIRROR.get())) return false;
        if (!(level.getBlockEntity(pos) instanceof SolarMirrorBlockEntity mirrorBE)) return false;
        if (!worldPosition.equals(mirrorBE.getLinkedTower())) return false;
        if (!hasSkyAndLineOfSight(pos)) {
            mirrorBE.setLinkedTower(null);
            return false;
        }
        return true;
    }

    private boolean hasSkyAndLineOfSight(BlockPos mirrorPos) {
        // canSeeSky checks the sky light AT the given position, and both of the mirror's
        // cells absorb skylight, so open air starts two cells above its base.
        if (!SolarHelper.canSeeSky(level, mirrorPos.above().above())) return false;

        BlockPos receiverPos = worldPosition.above(Math.max(height - 1, 0));
        BlockPos mirrorTopPos = mirrorPos.above();
        Vec3 from = Vec3.atCenterOf(mirrorPos).add(0, 0.75, 0); // roughly the panel/hinge height
        Vec3 to = Vec3.atCenterOf(receiverPos);

        // The ray starts inside the mirror's own collision box (post plus the plate's swept
        // volume), so skip past hits on the mirror's two cells and keep casting. The plate
        // box is nearly a block wide, so this needs enough steps to clear it.
        for (int i = 0; i < 16; i++) {
            ClipContext ctx = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            HitResult result = level.clip(ctx);
            if (result.getType() == HitResult.Type.MISS) return true;
            if (!(result instanceof BlockHitResult blockHit)) return false;
            BlockPos hitPos = blockHit.getBlockPos();
            if (hitPos.equals(mirrorPos) || hitPos.equals(mirrorTopPos)) {
                from = blockHit.getLocation().add(to.subtract(from).normalize().scale(0.2));
                continue;
            }
            // On a wide tower the ray often hits a different block of this same tower
            // first, which is fine. Anything else is a real obstruction.
            if (!level.getBlockState(hitPos).is(ModBlocks.SOLAR_POWER_TOWER.get())) return false;
            return level.getBlockEntity(hitPos) instanceof SolarPowerTowerBlockEntity hitTower
                    && worldPosition.equals(hitTower.getController());
        }
        return false;
    }

    /** Called by a mirror block when it breaks, so this tower's count updates immediately. */
    public void unregisterMirror(BlockPos pos) {
        if (registeredMirrors.remove(pos)) {
            cachedMirrorCount = registeredMirrors.size();
            setChanged();
            sync();
        }
    }

    private void clearMirrorRegistrations() {
        if (level == null) return;
        for (BlockPos pos : registeredMirrors) {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof SolarMirrorBlockEntity mirrorBE)
                mirrorBE.setLinkedTower(null);
        }
        registeredMirrors.clear();
        cachedMirrorCount = 0;
        setChanged();
        sync();
    }

    // setRemoved() also runs when the chunk just unloads, and the mirrors should stay
    // linked through that
    private boolean chunkUnloading = false;

    @Override
    public void onChunkUnloaded() {
        chunkUnloading = true;
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!chunkUnloading && level != null && !level.isClientSide && isController())
            clearMirrorRegistrations();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level != null && !level.isClientSide && isController())
            clearMirrorRegistrations();
        super.removeController(keepContents);
    }

    // Triangle curve: ramps 0 -> 100% up to the optimal mirror count, then falls back to 0%
    // at 2x optimal. Optimal scales linearly up to the tracked-mirror cap as this tower's
    // field radius approaches the configured max radius.
    private float mirrorEfficiency() {
        if (cachedMirrorCount == 0) return 0f;
        float radiusFraction = (float) mirrorRadius() / Config.solarPowerTowerMirrorMaxRadius;
        int optimal = Math.max(1, Math.round(Config.solarPowerTowerMaxTrackedMirrors * radiusFraction));
        float ratio = cachedMirrorCount / (float) optimal;
        return ratio <= 1f ? ratio : Math.max(0f, 2f - ratio);
    }

    public int getMirrorCount() { return cachedMirrorCount; }
    public float getEfficiency() { return mirrorEfficiency(); }
    public float getGenerationRate() { return lastGenerationRate; }

    // Wrenching switches between Molten Salt and Steam output, discarding whatever the
    // old output tank held. Returns the new mode.
    public boolean toggleSteamMode() {
        steamMode = !steamMode;
        (steamMode ? saltTank : steamTank).setFluid(FluidStack.EMPTY);
        setChanged();
        sync();
        return steamMode;
    }

    private void setLit(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(SolarPowerTowerBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(SolarPowerTowerBlock.LIT, lit), 3);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveMultiblockNBT(tag);
        tag.putBoolean("SteamMode", steamMode);
        tag.putFloat("SaltAccumulator", saltAccumulator);
        tag.putFloat("SteamAccumulator", steamAccumulator);
        tag.putInt("CachedMirrors", cachedMirrorCount);
        if (!registeredMirrors.isEmpty()) {
            long[] mirrors = new long[registeredMirrors.size()];
            int i = 0;
            for (BlockPos pos : registeredMirrors) mirrors[i++] = pos.asLong();
            tag.putLongArray("RegisteredMirrors", mirrors);
        }
        FluidTankNBTHelper.save(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.save(tag, "SaltTank",  saltTank);
        FluidTankNBTHelper.save(tag, "SteamTank", steamTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadMultiblockNBT(tag);
        steamMode        = tag.getBoolean("SteamMode");
        saltAccumulator  = tag.getFloat("SaltAccumulator");
        steamAccumulator = tag.getFloat("SteamAccumulator");
        cachedMirrorCount = tag.getInt("CachedMirrors");
        registeredMirrors.clear();
        if (tag.contains("RegisteredMirrors")) {
            for (long l : tag.getLongArray("RegisteredMirrors")) registeredMirrors.add(BlockPos.of(l));
        }
        if (isController()) {
            int totalBlocks = width * width * height;
            int cap = Config.solarPowerTowerTankPerBlock * totalBlocks;
            waterTank.setCapacity(cap);
            saltTank.setCapacity(cap);
            steamTank.setCapacity(cap);
        }
        FluidTankNBTHelper.load(tag, "WaterTank", waterTank);
        FluidTankNBTHelper.load(tag, "SaltTank",  saltTank);
        FluidTankNBTHelper.load(tag, "SteamTank", steamTank);
    }

    // -------------------------------------------------------------------------
    // Goggle tooltip
    // -------------------------------------------------------------------------

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SolarPowerTowerBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return false;
        if (ctrl != this) return ctrl.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.solar_power_tower_header").forGoggles(tooltip);

        GoggleHelper.tank(tooltip, "water", waterTank, ChatFormatting.AQUA);
        if (ctrl.steamMode)
            GoggleHelper.tank(tooltip, "steam", steamTank, ChatFormatting.WHITE);
        else
            GoggleHelper.tank(tooltip, "molten_salt", saltTank, ChatFormatting.GOLD);

        CreateLang.translate("solar_punk.tooltip.tower_mode")
                .style(ChatFormatting.GRAY)
                .add(Component.translatable(ctrl.steamMode
                        ? "solarpunk.tooltip.tower_mode_steam"
                        : "solarpunk.tooltip.tower_mode_salt")
                        .withStyle(ctrl.steamMode ? ChatFormatting.AQUA : ChatFormatting.GOLD))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.mirrors")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(cachedMirrorCount).style(ChatFormatting.YELLOW).component())
                .forGoggles(tooltip, 1);

        int efficiencyPct = (int) (mirrorEfficiency() * 100);
        CreateLang.translate("solar_punk.tooltip.efficiency")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(efficiencyPct).text("%").style(ChatFormatting.GREEN).component())
                .forGoggles(tooltip, 1);

        return true;
    }
}
