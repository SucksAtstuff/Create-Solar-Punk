package net.succ.solar_punk.block.entity.custom;

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

public class SolarPowerTowerBlockEntity extends MultiBlockFluidBE<SolarPowerTowerBlockEntity>
        implements IHaveGoggleInformation {

    // Max height per footprint width: index 1→5, 2→10, 3→20
    private static final int[] MAX_HEIGHTS = {0, 5, 10, 20};

    public  boolean steamMode           = false;
    private float saltAccumulator      = 0f;
    private float steamAccumulator     = 0f;
    private int   cachedMirrorCount    = 0;
    private int   mirrorScanCooldown   = 0;
    private boolean advancementFired   = false;

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
        if (longAxis != Direction.Axis.Y) return MAX_WIDTH;
        return switch (width) {
            case 2 -> MAX_HEIGHTS[2];
            case 3 -> MAX_HEIGHTS[3];
            default -> MAX_HEIGHTS[1];
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
            setLit(false);
            return;
        }

        float efficiency = mirrorEfficiency();
        // Rate scales super-linearly with height (exponent 1.5) so taller towers are
        // always more block-efficient than multiple short ones.
        // Steam: 7/3 multiplier → max tower produces 21 mB/t, matching the turbine.
        // Salt:  0.4 multiplier → max tower produces 3.6 mB/t, enough for 9 Heat
        //        Batteries (one per 3×3 base block) to stay superheated 24/7.
        int maxH = switch (width) { case 2 -> MAX_HEIGHTS[2]; case 3 -> MAX_HEIGHTS[3]; default -> MAX_HEIGHTS[1]; };
        float heightFraction = (float) height / maxH;
        float baseRate = (width * width) * (float) Math.pow(heightFraction, 1.5) * efficiency;
        float rate = steamMode ? baseRate * (7f / 3f) : baseRate * 0.4f;
        if (steamMode) {
            steamAccumulator += rate;
            saltAccumulator = 0f;
            int steamToAdd = (int) steamAccumulator;
            if (steamToAdd >= 1 &&
                steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), steamToAdd),
                        IFluidHandler.FluidAction.SIMULATE) == steamToAdd) {
                steamTank.fill(new FluidStack(ModFluids.STEAM_SOURCE.get(), steamToAdd),
                        IFluidHandler.FluidAction.EXECUTE);
                steamAccumulator -= steamToAdd;
            }
        } else {
            saltAccumulator += rate;
            steamAccumulator = 0f;
            int saltToAdd = (int) saltAccumulator;
            if (saltToAdd >= 1) {
                int waterToDrain = saltToAdd * width * width;
                if (waterTank.getFluidAmount() >= waterToDrain &&
                    saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), saltToAdd),
                            IFluidHandler.FluidAction.SIMULATE) == saltToAdd) {
                    waterTank.drain(waterToDrain, IFluidHandler.FluidAction.EXECUTE);
                    saltTank.fill(new FluidStack(ModFluids.MOLTEN_SALT_SOURCE.get(), saltToAdd),
                            IFluidHandler.FluidAction.EXECUTE);
                    saltAccumulator -= saltToAdd;
                }
            }
        }

        setLit(true);
        if (level.getGameTime() % 20 == 0) sync();
    }

    private boolean isSunActive() {
        if (level == null) return false;
        long time = level.getDayTime() % 24000;
        if (time >= 12000 || level.isThundering() || level.isRaining()) return false;
        return level.canSeeSky(worldPosition.above(height));
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
        sync(); // goggle tooltip reads the client's synced copy, not the server BE directly
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
        // canSeeSky checks the sky light value AT the given position. The mirror now
        // occupies two cells (see SolarMirrorBlock's lower/upper halves) and, like any
        // non-fully-transparent block, each one absorbs a point of its own incoming
        // skylight - so genuinely open air only starts two cells above the mirror's base.
        if (!level.canSeeSky(mirrorPos.above().above())) return false;

        BlockPos receiverPos = worldPosition.above(Math.max(height - 1, 0));
        BlockPos mirrorTopPos = mirrorPos.above();
        Vec3 from = Vec3.atCenterOf(mirrorPos).add(0, 0.75, 0); // roughly the panel/hinge height
        Vec3 to = Vec3.atCenterOf(receiverPos);

        // The mirror's collision shape now closely follows its model (post + a wide box
        // approximating the panel's swing), which means a ray leaving from anywhere near
        // the panel is very likely to start inside its own hitbox. Rather than hunting for
        // an origin point that dodges it, just skip past hits on the mirror's own two
        // cells and keep casting from there. The upper half's panel-sweep box is nearly a
        // full block wide, so the nudge needs enough distance and attempts to actually
        // clear it - a couple of tiny steps isn't enough and silently fails every mirror.
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
            // A BLOCK hit is only fine if it landed on this same tower - the ray is aimed
            // at one specific column of the tower's face, but on a footprint wider than 1
            // it will often clip a *different* block of the same structure first.
            // Anything belonging to a different tower or the world is a real obstruction.
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide && isController())
            clearMirrorRegistrations();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level != null && !level.isClientSide && isController())
            clearMirrorRegistrations();
        super.removeController(keepContents);
    }

    // Triangle curve: ramps 0→100% up to the optimal mirror count, then falls back to 0% at 2× optimal.
    // Optimal scales linearly from 0 up to the tracked-mirror cap as this tower's own field radius
    // approaches the configured max radius - a maxed-out tower's field is by design the 100% point.
    // (Scaling off the field's circumference instead would peak well under the cap for any tower,
    // since mirrorRadius() is itself capped independently of the tracked-mirror limit.)
    private float mirrorEfficiency() {
        if (cachedMirrorCount == 0) return 0f;
        float radiusFraction = (float) mirrorRadius() / Config.solarPowerTowerMirrorMaxRadius;
        int optimal = Math.max(1, Math.round(Config.solarPowerTowerMaxTrackedMirrors * radiusFraction));
        float ratio = cachedMirrorCount / (float) optimal;
        return ratio <= 1f ? ratio : Math.max(0f, 2f - ratio);
    }

    public void syncToClients() { sync(); }

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

        int cap = waterTank.getCapacity();
        CreateLang.translate("solar_punk.tooltip.solar_power_tower_header").forGoggles(tooltip);

        CreateLang.translate("solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(waterTank.getFluidAmount())
                        .text(" / " + cap + " mB").style(ChatFormatting.AQUA).component())
                .forGoggles(tooltip, 1);

        if (ctrl.steamMode) {
            CreateLang.translate("solar_punk.tooltip.steam")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(steamTank.getFluidAmount())
                            .text(" / " + cap + " mB").style(ChatFormatting.WHITE).component())
                    .forGoggles(tooltip, 1);
        } else {
            CreateLang.translate("solar_punk.tooltip.molten_salt")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(saltTank.getFluidAmount())
                            .text(" / " + cap + " mB").style(ChatFormatting.GOLD).component())
                    .forGoggles(tooltip, 1);
        }

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
