package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.succ.solar_punk.client.sound.BioreactorSoundInstance;
import net.succ.solar_punk.client.sound.MachineSoundInstance;
import net.succ.solar_punk.sound.ModSounds;
import net.succ.solar_punk.util.PonderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.ModTags;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.custom.BioreactorBlock;
import net.succ.solar_punk.block.custom.GrowLampBlock;
import net.succ.solar_punk.block.custom.FermentationVatBlock.VatPosition;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;
import net.succ.solar_punk.pollution.PollutionSavedData;
import net.succ.solar_punk.util.AutomationItemHandler;
import net.succ.solar_punk.util.FluidTankNBTHelper;
import net.succ.solar_punk.util.GoggleHelper;
import net.succ.solar_punk.util.MachineInteractions;
import net.succ.solar_punk.util.SolarHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Bioreactor: a glass tank of Water on a layer of Bioreactor Base blocks. Kelp or Seagrass
 * turns some of the Water into Algae Culture, which spreads through the rest in sunlight and
 * turns into Biomass and Oxygen at a rate set by the top surface area. Fertilizer or Rich
 * Fertilizer speeds it up, and while running it takes pollution out of its own chunk.
 *
 * <p>Water and culture share the tank's capacity; culture sits at the bottom, Water above it.
 * The controller (the lowest north-west block) holds everything.
 */
public class BioreactorBlockEntity extends MultiBlockFluidBE<BioreactorBlockEntity>
        implements IHaveGoggleInformation {

    public static final int MAX_HEIGHT = 8;
    public static final int OUTPUT_SLOT = 0, SEED_SLOT = 1;
    /** Inset of the rendered fluid (and particles) from the glass, in blocks. */
    public static final float HULL = 1 / 16f;

    public final FluidTank waterTank = tank(Config.bioreactorTankPerBlock, Fluids.WATER);
    public final FluidTank cultureTank = tank(Config.bioreactorTankPerBlock, ModFluids.ALGAE_CULTURE_SOURCE.get());
    public final FluidTank oxygenTank = tank(Config.bioreactorTank, ModFluids.OXYGEN_SOURCE.get());
    // Fertilizer or Rich Fertilizer, one at a time.
    public final FluidTank nutrientTank = new FluidTank(Config.bioreactorTank) {
        @Override public boolean isFluidValid(FluidStack stack) { return boostFor(stack.getFluid()) > 1; }
        @Override protected void onContentsChanged() { setChanged(); }
    };

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == SEED_SLOT ? stack.is(ModTags.Items.ALGAE_SEEDS) : stack.is(ModItems.BIOMASS.get());
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    // Hoppers and funnels put seeds in and take Biomass out.
    public final IItemHandler automationHandler = new AutomationItemHandler(inventory,
            slot -> slot == SEED_SLOT, (slot, stack) -> slot == OUTPUT_SLOT);

    /** Glass: Water or culture in, culture out (to seed another Bioreactor). */
    public final IFluidHandler tankFluidHandler = new FluidPort(this::getControllerBE, false);
    /** Base: Water, culture and nutrients in, Oxygen out. */
    public final IFluidHandler baseFluidHandler = new FluidPort(() -> this, true);

    private float progress;           // towards the next Biomass
    // Last second's readings, synced for goggles and rendering
    private float sunlight;           // 0-1, from the sun or Grow Lamps, whichever is brighter
    private int growLamps;            // lit Grow Lamps around or above the tank
    private boolean sunny;            // the light is the sun's, not the lamps'
    private int cultureSignal = -1, biomassSignal = -1;   // last comparator outputs
    private boolean hasBase;
    private boolean outputFull;       // the Biomass slot is full
    private boolean venting;          // the Oxygen tank is full, so the extra goes into the air
    private long absorbed;

    public BioreactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, BioreactorBlockEntity.class);
    }

    private FluidTank tank(int capacity, net.minecraft.world.level.material.Fluid accepted) {
        return new FluidTank(capacity) {
            @Override public boolean isFluidValid(FluidStack stack) { return stack.getFluid().isSame(accepted); }
            @Override protected void onContentsChanged() { setChanged(); }
        };
    }

    /** The controller of the tank standing on the Base block at {@code basePos}, if any. */
    @Nullable
    public static BioreactorBlockEntity above(Level level, BlockPos basePos) {
        if (level == null) return null;
        return level.getBlockEntity(basePos.above()) instanceof BioreactorBlockEntity be ? be.getControllerBE() : null;
    }

    private static float boostFor(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid.isSame(ModFluids.FERTILIZER_SOURCE.get())) return (float) Config.bioreactorFertilizerBoost;
        if (fluid.isSame(ModFluids.RICH_FERTILIZER_SOURCE.get())) return (float) Config.bioreactorRichFertilizerBoost;
        return 1;
    }

    // -------------------------------------------------------------------------
    // IMultiBlockEntityContainer.Fluid
    // -------------------------------------------------------------------------

    @Override
    protected void updatePosition() {
        BioreactorBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return;
        int yOffset = worldPosition.getY() - ctrl.worldPosition.getY();
        VatPosition vPos = ctrl.height == 1 ? VatPosition.SINGLE
                : yOffset == 0 ? VatPosition.BOTTOM
                : yOffset == ctrl.height - 1 ? VatPosition.TOP
                : VatPosition.MIDDLE;
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(BioreactorBlock.POSITION) != vPos)
            level.setBlock(worldPosition, state.setValue(BioreactorBlock.POSITION, vPos), 2);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return longAxis == Direction.Axis.Y ? MAX_HEIGHT : MAX_WIDTH;
    }

    @Override public int getTankSize(int tank) { return Config.bioreactorTankPerBlock; }

    @Override
    public void setTankSize(int tank, int blocks) {
        int capacity = Config.bioreactorTankPerBlock * Math.max(blocks, 1);
        waterTank.setCapacity(capacity);
        cultureTank.setCapacity(capacity);
        // Culture first: it's the part that takes a while to grow back.
        clamp(cultureTank, capacity);
        clamp(waterTank, capacity - cultureTank.getFluidAmount());
    }

    // The Oxygen and nutrient tanks scale with the footprint. Create calls setTankSize before
    // setWidth when forming, so they're sized here, once the new width is known.
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        resizeSideTanks();
    }

    @Override
    public void removeController(boolean keepContents) {
        super.removeController(keepContents);
        resizeSideTanks();
    }

    private void resizeSideTanks() {
        int capacity = Config.bioreactorTank * width * width;
        oxygenTank.setCapacity(capacity);
        nutrientTank.setCapacity(capacity);
        clamp(oxygenTank, capacity);
        clamp(nutrientTank, capacity);
    }

    private static void clamp(FluidTank tank, int max) {
        if (tank.getFluidAmount() > max) tank.setFluid(tank.getFluid().copyWithAmount(max));
    }

    // Only the Water is handed on when the tank is split or joined, like the Fermentation
    // Vat's output.
    @Override public IFluidTank getTank(int tank) { return waterTank; }
    @Override public FluidStack getFluid(int tank) { return waterTank.getFluid().copy(); }

    /** Room left for Water and culture together. */
    private int space() {
        return waterTank.getCapacity() - waterTank.getFluidAmount() - cultureTank.getFluidAmount();
    }

    // -------------------------------------------------------------------------
    // Tick (server only)
    // -------------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private BioreactorSoundInstance soundInstance;

    // Ticks until the next bubbling one-shot (client only).
    private int bubbleDelay;

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        boolean running = isController() && getBlockState().getValue(BioreactorBlock.LIT);
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance, running,
                () -> new BioreactorSoundInstance(this));
        if (!running || PonderHelper.isPonder(level)) {
            bubbleDelay = 0;
            return;
        }
        // Every 8 to 20 seconds, from the middle of the tank. The first one waits a full delay.
        if (bubbleDelay <= 0) {
            bubbleDelay = 160 + level.random.nextInt(240);
        } else if (--bubbleDelay == 0) {
            level.playLocalSound(worldPosition.getX() + getWidth() / 2.0, worldPosition.getY() + getHeight() / 2.0,
                    worldPosition.getZ() + getWidth() / 2.0, ModSounds.BIOREACTOR_BUBBLING.get(), SoundSource.BLOCKS,
                    0.15f, 0.85f + level.random.nextFloat() * 0.3f, false);
        }
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        if (updateConnectivity) {
            updateConnectivity = false;
            ConnectivityHandler.formMulti(this);
        }
        if (!isController() || level.getGameTime() % 20 != 0) return;

        hasBase = hasBase();
        float sun = hasBase ? sunlight() : 0;
        growLamps = hasBase ? countGrowLamps() : 0;
        float lamps = Math.min((float) Config.bioreactorGrowLampLight,
                growLamps * (float) Config.bioreactorGrowLampLight / (width * width));
        sunny = sun >= lamps;
        sunlight = Math.max(sun, lamps);
        absorbed = 0;
        outputFull = false;
        int vented = 0;

        seed();
        boolean running = sunlight > 0 && !cultureTank.isEmpty();
        if (running) {
            grow();
            float boost = nutrientTank.getFluidAmount() >= Config.bioreactorNutrientPerBiomass
                    ? boostFor(nutrientTank.getFluid().getFluid()) : 1;
            progress += width * width * sunlight * boost * 20f / Config.bioreactorTicksPerBiomass;
            while (progress >= 1) {
                if (cultureTank.getFluidAmount() < Config.bioreactorCulturePerBiomass) { progress = 1; break; }
                if (!canOutput()) { progress = 1; outputFull = true; break; }
                vented += output(boost > 1);
                progress -= 1;
                ModTriggers.fireNearby(level, worldPosition, ModTriggers.BIOREACTOR_BIOMASS);
            }
            absorbPollution();
        }
        // Stays set between Biomass, so goggles don't flicker while the tank stays full.
        if (vented > 0) { venting = true; vent(); }
        else if (!running || oxygenTank.getFluidAmount() < oxygenTank.getCapacity()) venting = false;

        setLit(running && !outputFull);
        updateComparators();
        setChanged();
        sync();
    }

    /**
     * Grow Lamps stand in for the sun: lit ones touching the sides of the tank or hanging
     * above its top. Each lights one block of the top surface at {@code grow_lamp_light}, so
     * one lamp per block of footprint runs it at that level all night.
     */
    private int countGrowLamps() {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x0 = worldPosition.getX(), y0 = worldPosition.getY(), z0 = worldPosition.getZ();
        for (int y = 0; y < height; y++)
            for (int i = 0; i < width; i++) {
                count += litLamp(pos.set(x0 - 1, y0 + y, z0 + i));
                count += litLamp(pos.set(x0 + width, y0 + y, z0 + i));
                count += litLamp(pos.set(x0 + i, y0 + y, z0 - 1));
                count += litLamp(pos.set(x0 + i, y0 + y, z0 + width));
            }
        for (int dx = 0; dx < width; dx++)
            for (int dz = 0; dz < width; dz++)
                count += litLamp(pos.set(x0 + dx, y0 + height, z0 + dz));
        return count;
    }

    private int litLamp(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.GROW_LAMP.get()) && state.getValue(GrowLampBlock.LIT) ? 1 : 0;
    }

    // -------------------------------------------------------------------------
    // Comparators: the glass reads how much of the tank is culture, the Base how full the
    // Biomass slot is.
    // -------------------------------------------------------------------------

    public int cultureSignal() {
        int capacity = cultureTank.getCapacity();
        if (capacity <= 0 || cultureTank.isEmpty()) return 0;
        return 1 + Math.round(14f * cultureTank.getFluidAmount() / capacity);
    }

    public int biomassSignal() {
        ItemStack stack = inventory.getStackInSlot(OUTPUT_SLOT);
        if (stack.isEmpty()) return 0;
        return 1 + Math.round(14f * stack.getCount() / inventory.getSlotLimit(OUTPUT_SLOT));
    }

    private void updateComparators() {
        int culture = cultureSignal(), biomass = biomassSignal();
        boolean cultureChanged = culture != cultureSignal, biomassChanged = biomass != biomassSignal;
        cultureSignal = culture;
        biomassSignal = biomass;
        if (!cultureChanged && !biomassChanged) return;
        for (int dx = 0; dx < width; dx++)
            for (int dz = 0; dz < width; dz++) {
                BlockPos column = worldPosition.offset(dx, 0, dz);
                if (biomassChanged)
                    level.updateNeighbourForOutputSignal(column.below(), ModBlocks.BIOREACTOR_BASE.get());
                if (cultureChanged)
                    for (int y = 0; y < height; y++)
                        level.updateNeighbourForOutputSignal(column.above(y), ModBlocks.BIOREACTOR.get());
            }
    }

    // Display Link readouts
    public float getLight() { return sunlight; }
    public float getCultureLevel() {
        return cultureTank.getCapacity() <= 0 ? 0 : cultureTank.getFluidAmount() / (float) cultureTank.getCapacity();
    }
    public float getBiomassPerMinute() { return hasBase && !cultureTank.isEmpty() ? biomassPerMinute() : 0; }

    /** Whether every block under the tank is a Bioreactor Base. */
    private boolean hasBase() {
        BlockPos below = worldPosition.below();
        for (int dx = 0; dx < width; dx++)
            for (int dz = 0; dz < width; dz++)
                if (!level.getBlockState(below.offset(dx, 0, dz)).is(ModBlocks.BIOREACTOR_BASE.get())) return false;
        return true;
    }

    /** 0-1: the top layer's average sunlight, with seasons and space applied. */
    private float sunlight() {
        BlockPos top = worldPosition.above(height - 1);
        float sum = 0;
        for (int dx = 0; dx < width; dx++)
            for (int dz = 0; dz < width; dz++)
                sum += SolarHelper.byPhase(level, top.offset(dx, 0, dz), 0.5f, 1f);
        return sum / (width * width) * SolarHelper.outputMultiplier(level, top);
    }

    // A Kelp or Seagrass turns some Water into culture, once there's Water and no culture yet.
    private void seed() {
        if (!hasBase || !cultureTank.isEmpty() || inventory.getStackInSlot(SEED_SLOT).isEmpty()) return;
        int amount = Math.min(Config.bioreactorSeedMb, cultureTank.getCapacity());
        if (waterTank.getFluidAmount() < amount) return;
        inventory.extractItem(SEED_SLOT, 1, false);
        convert(amount);
    }

    // The culture spreads through the Water, faster the more of it there is.
    private void grow() {
        int amount = Math.max(1, Math.round(cultureTank.getFluidAmount() * (float) Config.bioreactorGrowthPercent / 100f * sunlight));
        convert(Math.min(amount, waterTank.getFluidAmount()));
    }

    private void convert(int amount) {
        if (amount <= 0) return;
        waterTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        cultureTank.fill(new FluidStack(ModFluids.ALGAE_CULTURE_SOURCE.get(), amount), IFluidHandler.FluidAction.EXECUTE);
    }

    // Only a full Biomass slot stops it. Oxygen that doesn't fit is vented.
    private boolean canOutput() {
        return inventory.insertItem(OUTPUT_SLOT, new ItemStack(ModItems.BIOMASS.get()), true).isEmpty();
    }

    /** Makes one Biomass. Returns the mB of Oxygen that didn't fit and was vented. */
    private int output(boolean boosted) {
        inventory.insertItem(OUTPUT_SLOT, new ItemStack(ModItems.BIOMASS.get()), false);
        cultureTank.drain(Config.bioreactorCulturePerBiomass, IFluidHandler.FluidAction.EXECUTE);
        if (boosted) nutrientTank.drain(Config.bioreactorNutrientPerBiomass, IFluidHandler.FluidAction.EXECUTE);
        int oxygen = Config.bioreactorOxygenPerBiomass;
        if (oxygen <= 0) return 0;
        return oxygen - oxygenTank.fill(new FluidStack(ModFluids.OXYGEN_SOURCE.get(), oxygen), IFluidHandler.FluidAction.EXECUTE);
    }

    // A hiss and a puff of air from the top while the Oxygen tank is full.
    private void vent() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double x = worldPosition.getX() + width / 2.0;
        double y = worldPosition.getY() + height + 0.05;
        double z = worldPosition.getZ() + width / 2.0;
        serverLevel.playSound(null, x, y, z, ModSounds.BIOREACTOR_VENT.get(), SoundSource.BLOCKS,
                0.2f, 0.9f + level.random.nextFloat() * 0.2f);
        serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 3 + width, width * 0.25, 0.05, width * 0.25, 0.01);
    }

    // Like the Biofilter, but only in its own chunk, and only while the algae have light.
    private void absorbPollution() {
        if (!Config.globalWarmingEnabled || !(level instanceof ServerLevel serverLevel)) return;
        long amount = Math.round(width * width * sunlight * Config.bioreactorPollutionPerBlock);
        if (amount <= 0) return;
        absorbed = PollutionSavedData.get(serverLevel).reducePollution(new ChunkPos(worldPosition), amount);
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (state.getValue(BioreactorBlock.LIT) != lit)
            level.setBlock(worldPosition, state.setValue(BioreactorBlock.LIT, lit), 3);
    }

    // -------------------------------------------------------------------------
    // Interaction and rendering
    // -------------------------------------------------------------------------

    /** Buckets go through {@code fluids}, Kelp and Seagrass into the seed slot. */
    public ItemInteractionResult useItem(Level level, Player player, InteractionHand hand, ItemStack stack, IFluidHandler fluids) {
        BioreactorBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        ItemInteractionResult result = MachineInteractions.useItem(level, player, hand, stack, ctrl, fluids, ctrl.inventory, SEED_SLOT);
        if (result == ItemInteractionResult.SUCCESS && !level.isClientSide) ctrl.sync();
        return result;
    }

    /** How deep the culture is, in blocks (client side, from the last sync). */
    public float cultureDepth() {
        return fillHeight(cultureTank.getFluidAmount());
    }

    /** How tall {@code amount} mB stands in this tank, in blocks. */
    public float fillHeight(int amount) {
        int capacity = waterTank.getCapacity();
        return capacity <= 0 ? 0 : (height - 2 * HULL) * Math.min(1f, amount / (float) capacity);
    }

    public AABB renderBox() {
        return new AABB(worldPosition).expandTowards(width - 1, height - 1, width - 1);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveMultiblockNBT(tag);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putFloat("Progress", progress);
        tag.putFloat("Sunlight", sunlight);
        tag.putInt("GrowLamps", growLamps);
        tag.putBoolean("Sunny", sunny);
        tag.putBoolean("HasBase", hasBase);
        tag.putBoolean("OutputFull", outputFull);
        tag.putBoolean("Venting", venting);
        tag.putLong("Absorbed", absorbed);
        FluidTankNBTHelper.save(tag, "Water", waterTank);
        FluidTankNBTHelper.save(tag, "Culture", cultureTank);
        FluidTankNBTHelper.save(tag, "Oxygen", oxygenTank);
        FluidTankNBTHelper.save(tag, "Nutrients", nutrientTank);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadMultiblockNBT(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress = tag.getFloat("Progress");
        sunlight = tag.getFloat("Sunlight");
        growLamps = tag.getInt("GrowLamps");
        sunny = tag.getBoolean("Sunny");
        hasBase = tag.getBoolean("HasBase");
        outputFull = tag.getBoolean("OutputFull");
        venting = tag.getBoolean("Venting");
        absorbed = tag.getLong("Absorbed");
        if (isController()) {
            waterTank.setCapacity(Config.bioreactorTankPerBlock * width * width * height);
            cultureTank.setCapacity(Config.bioreactorTankPerBlock * width * width * height);
            oxygenTank.setCapacity(Config.bioreactorTank * width * width);
            nutrientTank.setCapacity(Config.bioreactorTank * width * width);
        }
        FluidTankNBTHelper.load(tag, "Water", waterTank);
        FluidTankNBTHelper.load(tag, "Culture", cultureTank);
        FluidTankNBTHelper.load(tag, "Oxygen", oxygenTank);
        FluidTankNBTHelper.load(tag, "Nutrients", nutrientTank);
    }

    // -------------------------------------------------------------------------
    // Goggles
    // -------------------------------------------------------------------------

    /** Biomass per minute at the current light and nutrients. */
    private float biomassPerMinute() {
        float boost = nutrientTank.getFluidAmount() >= Config.bioreactorNutrientPerBiomass
                ? boostFor(nutrientTank.getFluid().getFluid()) : 1;
        return width * width * sunlight * boost * 1200f / Config.bioreactorTicksPerBiomass;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BioreactorBlockEntity ctrl = getControllerBE();
        if (ctrl == null) return false;
        if (ctrl != this) return ctrl.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.bioreactor_header").forGoggles(tooltip);
        CreateLang.translate("solar_punk.tooltip.bioreactor_size")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(width + "x" + width + "x" + height).withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip, 1);

        if (!hasBase) {
            CreateLang.translate("solar_punk.tooltip.bioreactor_no_base").style(ChatFormatting.RED).forGoggles(tooltip, 1);
        } else if (sunlight <= 0) {
            CreateLang.translate("solar_punk.tooltip.bioreactor_no_sun").style(ChatFormatting.RED).forGoggles(tooltip, 1);
        } else {
            CreateLang.translate(sunny ? "solar_punk.tooltip.bioreactor_sunlight" : "solar_punk.tooltip.bioreactor_lamplight")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(Math.round(sunlight * 100)).text("%").style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);
        }
        if (growLamps > 0)
            CreateLang.translate("solar_punk.tooltip.bioreactor_grow_lamps")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(growLamps).style(ChatFormatting.YELLOW).component())
                    .forGoggles(tooltip, 1);

        if (cultureTank.isEmpty()) {
            CreateLang.translate("solar_punk.tooltip.bioreactor_no_culture").style(ChatFormatting.GOLD).forGoggles(tooltip, 1);
        } else if (hasBase && sunlight > 0) {
            CreateLang.translate("solar_punk.tooltip.bioreactor_biomass_rate")
                    .style(ChatFormatting.GRAY)
                    .add(Component.literal(String.format("%.1f", biomassPerMinute())).append("/min").withStyle(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
        }
        if (outputFull)
            CreateLang.translate("solar_punk.tooltip.bioreactor_full").style(ChatFormatting.RED).forGoggles(tooltip, 1);
        if (venting)
            CreateLang.translate("solar_punk.tooltip.bioreactor_venting").style(ChatFormatting.GOLD).forGoggles(tooltip, 1);
        if (absorbed > 0)
            CreateLang.translate("solar_punk.tooltip.bioreactor_absorbing")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(absorbed).text("/s").style(ChatFormatting.GREEN).component())
                    .forGoggles(tooltip, 1);

        int tankCapacity = waterTank.getCapacity();
        GoggleHelper.tank(tooltip, "water", waterTank.getFluidAmount(), tankCapacity, ChatFormatting.AQUA);
        GoggleHelper.tank(tooltip, "algae_culture", cultureTank.getFluidAmount(), tankCapacity, ChatFormatting.DARK_GREEN);
        GoggleHelper.tank(tooltip, "oxygen", oxygenTank, ChatFormatting.WHITE);
        GoggleHelper.namedTank(tooltip, "nutrients", nutrientTank, ChatFormatting.GREEN);
        ItemStack biomass = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!biomass.isEmpty())
            CreateLang.translate("solar_punk.tooltip.biomass_stored")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(biomass.getCount()).style(ChatFormatting.GREEN).component())
                    .forGoggles(tooltip, 1);
        return true;
    }

    // -------------------------------------------------------------------------
    // Capabilities
    // -------------------------------------------------------------------------

    /**
     * A fluid handler that finds the controller on every call, so it keeps working while the
     * tank forms and splits (pipes cache the handler object, and Base blocks have no block
     * entity to invalidate it). The glass takes Water or culture and gives culture; the Base
     * takes Water, culture and nutrients and gives Oxygen, so a pump on the Base pulls Oxygen.
     */
    public static class FluidPort implements IFluidHandler {
        private final Supplier<BioreactorBlockEntity> controller;
        private final boolean base;

        public FluidPort(Supplier<BioreactorBlockEntity> controller, boolean base) {
            this.controller = controller;
            this.base = base;
        }

        private FluidTank[] tanks(BioreactorBlockEntity c) {
            return base ? new FluidTank[]{c.waterTank, c.nutrientTank, c.oxygenTank}
                        : new FluidTank[]{c.waterTank, c.cultureTank};
        }

        @Override public int getTanks() { return base ? 3 : 2; }

        @Override public FluidStack getFluidInTank(int tank) {
            BioreactorBlockEntity c = controller.get();
            return c == null ? FluidStack.EMPTY : tanks(c)[tank].getFluid();
        }

        @Override public int getTankCapacity(int tank) {
            BioreactorBlockEntity c = controller.get();
            return c == null ? 0 : tanks(c)[tank].getCapacity();
        }

        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            BioreactorBlockEntity c = controller.get();
            return c != null && tanks(c)[tank] != c.oxygenTank && tanks(c)[tank].isFluidValid(stack);
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            BioreactorBlockEntity c = controller.get();
            if (c == null || resource.isEmpty()) return 0;
            if (base && c.nutrientTank.isFluidValid(resource)) return c.nutrientTank.fill(resource, action);
            FluidTank target = c.waterTank.isFluidValid(resource) ? c.waterTank
                    : c.cultureTank.isFluidValid(resource) ? c.cultureTank : null;
            if (target == null) return 0;
            int amount = Math.min(resource.getAmount(), c.space());
            return amount <= 0 ? 0 : target.fill(resource.copyWithAmount(amount), action);
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            BioreactorBlockEntity c = controller.get();
            if (c == null) return FluidStack.EMPTY;
            return (base ? c.oxygenTank : c.cultureTank).drain(resource, action);
        }

        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            BioreactorBlockEntity c = controller.get();
            if (c == null) return FluidStack.EMPTY;
            return (base ? c.oxygenTank : c.cultureTank).drain(maxDrain, action);
        }
    }

    /** Item access through a Base block, finding the controller on every call. */
    public static class ItemPort implements IItemHandler {
        private final Supplier<BioreactorBlockEntity> controller;

        public ItemPort(Supplier<BioreactorBlockEntity> controller) { this.controller = controller; }

        private IItemHandler handler() {
            BioreactorBlockEntity c = controller.get();
            return c == null ? null : c.automationHandler;
        }

        @Override public int getSlots() { IItemHandler h = handler(); return h == null ? 0 : h.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { IItemHandler h = handler(); return h == null ? ItemStack.EMPTY : h.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { IItemHandler h = handler(); return h == null ? stack : h.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { IItemHandler h = handler(); return h == null ? ItemStack.EMPTY : h.extractItem(slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { IItemHandler h = handler(); return h == null ? 0 : h.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { IItemHandler h = handler(); return h != null && h.isItemValid(slot, stack); }
    }
}
