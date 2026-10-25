package net.succ.solar_punk.block.entity.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.block.custom.AcceleratorRing;
import net.succ.solar_punk.client.sound.AcceleratorHumSoundInstance;
import net.succ.solar_punk.recipe.ModRecipeTypes;
import net.succ.solar_punk.recipe.ParticleAcceleratorRecipe;
import net.succ.solar_punk.sound.ModSounds;
import net.succ.solar_punk.util.PonderHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// The Particle Accelerator's controller. Rescans its ring every 2 seconds, picks a recipe
// from what's in its input slots (Basin-style: any slots, any order), draws stress for it
// and puts the result in its output slots. Items go in and come out through the side cap,
// where the filter slot also sits.
public class ParticleAcceleratorInputBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    public static final int SLOTS = 9;
    /** Block event sent when a craft finishes: the beams collide in the Input. */
    public static final int COLLISION_EVENT = 1;
    /** The beams collide in the middle of the Input, half a block along its tube. */
    public static final double COLLISION_POINT = 0.5;
    public static final int FLASH_TICKS = 12;
    // Average beam speed (blocks per tick) at the recipe's minimum speed; sets the lap count
    private static final double AVERAGE_BEAM_SPEED = 1.5;
    private static final int SCAN_INTERVAL = 40;
    private static final int RECIPE_CHECK_INTERVAL = 10;

    public final ItemStackHandler inputInv = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            contentsChanged = true;
            setChanged();
        }
    };
    public final ItemStackHandler outputInv = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    /** What the side cap exposes: insert into the input slots, extract from the output slots. */
    public final IItemHandler capHandler = new CapHandler();

    private FilteringBehaviour filtering;

    // Ring state (synced for goggles)
    private AcceleratorRing.Status ringStatus = AcceleratorRing.Status.OPEN;
    private int diameter;
    private int scanTimer;
    // The ring's blocks in loop order (synced, so the client can draw the particle)
    private List<BlockPos> ringPath = List.of();

    // Client only: the two beams. The tube's centre line they follow, how far the craft
    // is (last tick and this tick) which places them, and the collision flash.
    private List<Vec3> particlePath = List.of();
    private double particleLength;
    private boolean particlePathDirty;
    private float beamProgress, beamPrevProgress;
    private int flashTicks;
    @OnlyIn(Dist.CLIENT)
    @Nullable
    private AcceleratorHumSoundInstance humSound;

    // Current recipe (the result is synced for goggles)
    @Nullable private RecipeHolder<ParticleAcceleratorRecipe> recipe;
    // Saved with the world, so a reload picks the same recipe back up without losing progress
    @Nullable private ResourceLocation recipeId;
    private ItemStack recipeResult = ItemStack.EMPTY;
    private int processingTime;
    private int minSpeed;
    private float progress;
    // Smallest ring a recipe the items already match would need, when this ring is too
    // small for it (0 otherwise). Shown on the goggles so an idle Input says why.
    private int neededDiameter;
    private boolean contentsChanged = true;
    private int recipeTimer;
    // Stress per RPM the current recipe draws on this ring
    private float stressImpact;

    public ParticleAcceleratorInputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new FilteringBehaviour(this, new CapSlot())
                .withCallback(filter -> contentsChanged = true)
                .forRecipes();
        behaviours.add(filtering);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            tickParticle();
            return;
        }

        if (--scanTimer <= 0) {
            scanTimer = SCAN_INTERVAL;
            scanRing();
        }

        if (contentsChanged || --recipeTimer <= 0) {
            contentsChanged = false;
            recipeTimer = RECIPE_CHECK_INTERVAL;
            updateRecipe();
        }

        process();
    }

    private void scanRing() {
        AcceleratorRing.Result result = AcceleratorRing.scan(level, worldPosition);
        // An unloaded part of the ring pauses it rather than counting as broken
        if (result.status() == ringStatus && result.path().equals(ringPath)) return;
        ringStatus = result.status();
        diameter = result.diameter();
        ringPath = result.path();
        contentsChanged = true;
        sendData();
    }

    /** Picks the first recipe the input slots cover that this ring is big enough for (and the filter allows). */
    private void updateRecipe() {
        RecipeHolder<ParticleAcceleratorRecipe> found = null;
        int needed = 0;
        if (ringStatus == AcceleratorRing.Status.FORMED) {
            ParticleAcceleratorRecipe.Input input = recipeInput();
            for (RecipeHolder<ParticleAcceleratorRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.PARTICLE_ACCELERATOR.get())) {
                ParticleAcceleratorRecipe r = holder.value();
                if (!filtering.getFilter().isEmpty() && !filtering.test(r.result())) continue;
                if (!r.matches(input, level)) continue;
                if (diameter < r.minDiameter()) {
                    needed = needed == 0 ? r.minDiameter() : Math.min(needed, r.minDiameter());
                    continue;
                }
                found = holder;
                break;
            }
        }
        if (found != null) needed = 0;
        if (needed != neededDiameter) {
            neededDiameter = needed;
            sendData();
        }

        if (found == null ? recipe == null && recipeId == null : found.id().equals(recipeId)) {
            if (found != null) {
                // the ring may have changed size, which changes the stress
                setStressImpact(found.value().stressImpactFor(diameter));
                if (recipe == null) {
                    // first check after loading: same recipe as before, keep its progress
                    recipe = found;
                    recipeResult = found.value().result().copy();
                    processingTime = found.value().processingTime();
                    minSpeed = found.value().minSpeed();
                    sendData();
                }
            }
            return;
        }

        recipe = found;
        recipeId = found == null ? null : found.id();
        progress = 0;
        recipeResult = found == null ? ItemStack.EMPTY : found.value().result().copy();
        processingTime = found == null ? 0 : found.value().processingTime();
        minSpeed = found == null ? 0 : found.value().minSpeed();
        setStressImpact(found == null ? 0 : found.value().stressImpactFor(diameter));
        sendData();
    }

    private void setStressImpact(float impact) {
        if (impact == stressImpact) return;
        stressImpact = impact;
        if (hasNetwork())
            getOrCreateNetwork().updateStressFor(this, impact);
    }

    private void process() {
        if (recipe == null || ringStatus != AcceleratorRing.Status.FORMED) return;
        ParticleAcceleratorRecipe r = recipe.value();
        float speed = Math.abs(getSpeed());
        if (speed < r.minSpeed() || isOverStressed()) return;

        // Faster than the minimum speed crafts faster (and draws more stress), like Create's machines
        progress += speed / r.minSpeed();
        if (progress < r.processingTime()) {
            if (level.getGameTime() % 20 == 0) sendData();
            return;
        }

        ItemStack result = r.assemble(recipeInput(), level.registryAccess());
        if (!ItemHandlerHelper.insertItem(outputInv, result, true).isEmpty()) {
            progress = r.processingTime();  // wait for room in the output slots
            return;
        }
        consumeIngredients(r);
        ItemHandlerHelper.insertItem(outputInv, result, false);
        level.blockEvent(worldPosition, getBlockState().getBlock(), COLLISION_EVENT, 0);
        progress = 0;
        contentsChanged = true;
        sendData();
    }

    /** Takes each ingredient's count from the input slots, in the same order matches() counts them. */
    private void consumeIngredients(ParticleAcceleratorRecipe r) {
        for (SizedIngredient ingredient : r.ingredients()) {
            int needed = ingredient.count();
            for (int slot = 0; slot < SLOTS && needed > 0; slot++) {
                ItemStack stack = inputInv.getStackInSlot(slot);
                if (stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
                needed -= inputInv.extractItem(slot, needed, false).getCount();
            }
        }
    }

    private ParticleAcceleratorRecipe.Input recipeInput() {
        List<ItemStack> items = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++)
            items.add(inputInv.getStackInSlot(slot));
        return new ParticleAcceleratorRecipe.Input(items);
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = stressImpact;
        return stressImpact;
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, inputInv);
        ItemHelper.dropContents(level, worldPosition, outputInv);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Input", inputInv.serializeNBT(registries));
        tag.put("Output", outputInv.serializeNBT(registries));
        tag.putInt("RingStatus", ringStatus.ordinal());
        tag.putInt("Diameter", diameter);
        tag.putFloat("Progress", progress);
        tag.putFloat("StressImpact", stressImpact);
        if (recipeId != null) tag.putString("Recipe", recipeId.toString());
        if (clientPacket) {
            tag.putLongArray("RingPath", ringPath.stream().mapToLong(BlockPos::asLong).toArray());
            tag.put("Result", recipeResult.saveOptional(registries));
            tag.putInt("ProcessingTime", processingTime);
            tag.putInt("MinSpeed", minSpeed);
            tag.putInt("NeededDiameter", neededDiameter);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inputInv.deserializeNBT(registries, tag.getCompound("Input"));
        outputInv.deserializeNBT(registries, tag.getCompound("Output"));
        ringStatus = AcceleratorRing.Status.values()[tag.getInt("RingStatus")];
        diameter = tag.getInt("Diameter");
        progress = tag.getFloat("Progress");
        stressImpact = tag.getFloat("StressImpact");
        recipeId = tag.contains("Recipe") ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
        if (clientPacket) {
            List<BlockPos> path = Arrays.stream(tag.getLongArray("RingPath")).mapToObj(BlockPos::of).toList();
            if (!path.equals(ringPath)) {
                ringPath = path;
                particlePathDirty = true;
            }
            recipeResult = ItemStack.parseOptional(registries, tag.getCompound("Result"));
            processingTime = tag.getInt("ProcessingTime");
            minSpeed = tag.getInt("MinSpeed");
            neededDiameter = tag.getInt("NeededDiameter");
        }
    }

    // --- Ponder ---
    // Ponder's scene world is client-only, so the server side (ring scan, recipes) never
    // runs there. These set up what it would sync, so the scene shows the real beams.

    /** Starts a craft's beams: scans the ring and runs them for {@code time} ticks at {@code rpm}. */
    public void ponderStartCraft(ItemStack result, int time, int rpm) {
        AcceleratorRing.Result ring = AcceleratorRing.scan(level, worldPosition, false);
        ringStatus = ring.status();
        diameter = ring.diameter();
        ringPath = ring.path();
        particlePathDirty = true;
        recipeResult = result;
        processingTime = time;
        minSpeed = rpm;
        progress = 0;
        beamProgress = beamPrevProgress = 0;
    }

    /** Ends the craft: the beams collide (flash and sparks) and stop. */
    public void ponderFinishCraft() {
        triggerEvent(COLLISION_EVENT, 0);
        recipeResult = ItemStack.EMPTY;
        progress = 0;
    }

    // --- Display Link readouts (server side) ---

    /** One-word state of the accelerator, saying what's wrong when it isn't crafting. */
    public MutableComponent getStatusLine() {
        return switch (ringStatus) {
            case OPEN -> Component.literal("Ring not closed").withStyle(ChatFormatting.RED);
            case UNLOADED -> Component.literal("Ring partly unloaded").withStyle(ChatFormatting.YELLOW);
            case MULTIPLE_INPUTS -> Component.literal("More than one Input").withStyle(ChatFormatting.RED);
            case FORMED -> {
                if (recipe == null)
                    yield neededDiameter > 0
                            ? Component.literal("Ring too small").withStyle(ChatFormatting.RED)
                            : Component.literal("Idle").withStyle(ChatFormatting.GRAY);
                if (Math.abs(getSpeed()) < recipe.value().minSpeed())
                    yield Component.literal("Too slow").withStyle(ChatFormatting.RED);
                if (isOverStressed())
                    yield Component.literal("Overstressed").withStyle(ChatFormatting.RED);
                yield Component.literal("Crafting").withStyle(ChatFormatting.GREEN);
            }
        };
    }

    /** What it's making, or empty. */
    public ItemStack getRecipeResult() {
        return recipeResult;
    }

    /** How far the current craft is, 0 to 1. */
    public float getProgressFraction() {
        return processingTime > 0 ? Math.min(1, progress / processingTime) : 0;
    }

    /** Whether the ring is crafting right now (the beams only run then). */
    public boolean isRunning() {
        return ringStatus == AcceleratorRing.Status.FORMED && !recipeResult.isEmpty()
                && minSpeed > 0 && Math.abs(getSpeed()) >= minSpeed && !isOverStressed();
    }

    private void tickParticle() {
        // Rebuild the path when the ring changes; retry once a second if some of the ring
        // isn't loaded on this client yet (the path comes back empty then)
        boolean retry = particlePath.isEmpty() && !ringPath.isEmpty() && level.getGameTime() % 20 == 0;
        if (particlePathDirty || retry) {
            particlePathDirty = false;
            particlePath = AcceleratorRing.tubePath(level, ringPath);
            particleLength = 0;
            for (int i = 0; i < particlePath.size(); i++)
                particleLength += particlePath.get(i).distanceTo(particlePath.get((i + 1) % particlePath.size()));
        }
        if (flashTicks > 0) flashTicks--;
        beamPrevProgress = beamProgress;
        if (!isRunning() || particleLength <= 0) return;

        // Follow the server's progress between its updates, the same way it counts
        if (processingTime > 0)
            progress = Math.min(processingTime, progress + Math.abs(getSpeed()) / minSpeed);
        beamProgress = processingTime > 0 ? progress / processingTime : 0;
        if (beamProgress < beamPrevProgress)
            beamPrevProgress = beamProgress;  // a new craft started
    }

    // Create's KineticBlockEntity.tick() calls this every client tick. Starts the hum when
    // a craft runs; the sound itself follows the beams and fades out when it stops.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        if (PonderHelper.isPonder(level)) return;  // Ponder scenes stay silent
        super.tickAudio();
        if (isRunning() && (humSound == null || humSound.isStopped())) {
            humSound = new AcceleratorHumSoundInstance(this);
            Minecraft.getInstance().getSoundManager().play(humSound);
        }
    }

    /** Whole laps each beam makes per craft, so the average speed stays about the same on any ring. */
    private int laps() {
        return Math.max(1, (int) Math.round(AVERAGE_BEAM_SPEED * processingTime / particleLength));
    }

    /**
     * How far each beam is from the collision point, along the tube. It comes from the
     * craft's progress, not a clock: progress squared so the beams speed up, times whole
     * laps, so at 100% both are back at the Input from opposite sides as the craft finishes.
     */
    public double getBeamDistance(float partialTicks) {
        float done = beamPrevProgress + (beamProgress - beamPrevProgress) * partialTicks;
        return particleLength * laps() * done * done;
    }

    /** How far through the craft the beams are, 0 to 1. */
    public float getBeamProgress(float partialTicks) {
        return beamPrevProgress + (beamProgress - beamPrevProgress) * partialTicks;
    }

    /** The collision flash, 1 right after the collision fading to 0. */
    public float getFlash(float partialTicks) {
        return Math.max(0, (flashTicks - partialTicks) / FLASH_TICKS);
    }

    public List<Vec3> getParticlePath() { return particlePath; }
    public double getParticleLength() { return particleLength; }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != COLLISION_EVENT) return super.triggerEvent(id, param);
        if (level != null && level.isClientSide && !particlePath.isEmpty()) {
            flashTicks = FLASH_TICKS;
            Vec3 p = pointAlongPath(COLLISION_POINT);
            if (!PonderHelper.isPonder(level))
                level.playLocalSound(p.x, p.y, p.z, ModSounds.PARTICLE_COLLISION.get(), SoundSource.BLOCKS,
                        0.1f, 0.95f + level.random.nextFloat() * 0.1f, false);
            RandomSource random = level.random;
            for (int i = 0; i < 16; i++)
                level.addParticle(ParticleTypes.END_ROD, p.x, p.y, p.z,
                        (random.nextDouble() - 0.5) * 0.25, (random.nextDouble() - 0.5) * 0.25,
                        (random.nextDouble() - 0.5) * 0.25);
            for (int i = 0; i < 10; i++)
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z,
                        (random.nextDouble() - 0.5) * 0.6, (random.nextDouble() - 0.5) * 0.6,
                        (random.nextDouble() - 0.5) * 0.6);
        }
        return true;
    }

    /** Point on the tube's centre line at a distance along it (wraps around the ring). */
    public Vec3 pointAlongPath(double distance) {
        int n = particlePath.size();
        distance = ((distance % particleLength) + particleLength) % particleLength;
        for (int i = 0; i < n; i++) {
            Vec3 a = particlePath.get(i), b = particlePath.get((i + 1) % n);
            double segment = a.distanceTo(b);
            if (distance <= segment && segment > 0)
                return a.lerp(b, distance / segment);
            distance -= segment;
        }
        return particlePath.get(0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.accelerator_header").forGoggles(tooltip);

        Component status = switch (ringStatus) {
            case FORMED -> Component.literal("Formed").withStyle(ChatFormatting.GREEN);
            case UNLOADED -> Component.literal("Partly unloaded").withStyle(ChatFormatting.YELLOW);
            case MULTIPLE_INPUTS -> Component.literal("More than one Input").withStyle(ChatFormatting.RED);
            case OPEN -> Component.literal("Not closed").withStyle(ChatFormatting.RED);
        };
        CreateLang.translate("solar_punk.tooltip.accelerator_ring")
                .style(ChatFormatting.GRAY)
                .add(status)
                .forGoggles(tooltip, 1);

        if (ringStatus == AcceleratorRing.Status.FORMED)
            CreateLang.translate("solar_punk.tooltip.accelerator_diameter")
                    .style(ChatFormatting.GRAY)
                    .add(Component.literal(diameter + " blocks").withStyle(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.accelerator_recipe")
                .style(ChatFormatting.GRAY)
                .add(recipeResult.isEmpty()
                        ? Component.literal("None").withStyle(ChatFormatting.DARK_GRAY)
                        : recipeResult.getHoverName().copy().withStyle(ChatFormatting.GOLD))
                .forGoggles(tooltip, 1);

        // Why it isn't crafting, if it isn't
        if (recipeResult.isEmpty() && neededDiameter > 0)
            CreateLang.translate("solar_punk.tooltip.accelerator_too_small", neededDiameter)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        else if (!recipeResult.isEmpty() && Math.abs(getSpeed()) < minSpeed)
            CreateLang.translate("solar_punk.tooltip.accelerator_too_slow", minSpeed)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        else if (!recipeResult.isEmpty() && isOverStressed())
            CreateLang.translate("solar_punk.tooltip.accelerator_overstressed")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);

        if (!recipeResult.isEmpty() && processingTime > 0)
            CreateLang.translate("solar_punk.tooltip.accelerator_progress")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.number(Math.min(100, (int) (progress * 100 / processingTime))).text("%")
                            .style(ChatFormatting.AQUA).component())
                    .forGoggles(tooltip, 1);

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }

    /** The side cap: inputs can only be inserted, outputs only extracted (so funnels take the result). */
    private class CapHandler implements IItemHandler {
        @Override public int getSlots() { return SLOTS * 2; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < SLOTS ? inputInv.getStackInSlot(slot) : outputInv.getStackInSlot(slot - SLOTS);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot < SLOTS ? inputInv.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot < SLOTS ? ItemStack.EMPTY : outputInv.extractItem(slot - SLOTS, amount, simulate);
        }

        @Override public int getSlotLimit(int slot) { return 64; }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < SLOTS;
        }
    }

    /** The filter slot, on the face of the item cap. */
    private static class CapSlot extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            // The cap's face is 1 px in from the block's edge
            return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 8, 15.5 - 1),
                    AngleHelper.horizontalAngle(AcceleratorRing.capSide(state)), Direction.Axis.Y);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            TransformStack.of(ms).rotateYDegrees(AngleHelper.horizontalAngle(AcceleratorRing.capSide(state)) + 180);
        }
    }
}
