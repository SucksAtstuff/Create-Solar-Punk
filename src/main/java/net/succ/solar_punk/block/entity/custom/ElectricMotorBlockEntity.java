package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.ElectricMotorBlock;
import net.succ.solar_punk.client.sound.ElectricMotorSoundInstance;
import net.succ.solar_punk.client.sound.MachineSoundInstance;
import net.succ.solar_punk.util.EnergyFormat;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Electric Motor: FE in, rotation out, at a speed set with a scroll value like Create's
 * Creative Motor. It uses FE for its set speed whatever the load (Create doesn't tell a
 * generator how much of its capacity is used), so a slower setting is how players save FE.
 *
 * <p>It keeps about a second of FE. It runs while there's a tick's worth, stops when it runs
 * dry, and only restarts once the buffer is half full again, so a weak supply doesn't start
 * and stop it every tick (each change re-solves the whole kinetic network).
 */
public class ElectricMotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {

    private static final int RESTART_TICKS = 10;

    public ScrollValueBehaviour speed;
    public final IEnergyStorage energyStorage = new MotorEnergy();
    private int energy = 0;
    private boolean running = false;
    private int receivedThisSecond = 0;
    // FE/t that came in over the last second, for goggles.
    private int input = 0;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private ElectricMotorSoundInstance soundInstance;

    public ElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        int max = Config.motorMaxRpm;
        speed = new KineticScrollValueBehaviour(CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"),
                this, new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() {
                        return VecHelper.voxelSpace(8, 8, 15.5);
                    }

                    // Every side but the shaft and the base plate.
                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        return direction != Direction.DOWN && direction != state.getValue(ElectricMotorBlock.FACING);
                    }
                }) {
            // "16 ⟳ · 64 FE/t": the FE cost of each speed, shown while choosing it. Uses the
            // client's config values, which match the server's unless the server changed them.
            // Create's board always runs to 256 RPM; this one stops at the motor's max.
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                ValueSettingsBoard board = super.createBoard(player, hitResult);
                int max = Config.motorMaxRpm;
                return new ValueSettingsBoard(board.title(), max, Math.max(1, max / 4), board.rows(),
                        new ValueSettingsFormatter(this::formatSettings));
            }

            @Override
            public MutableComponent formatSettings(ValueSettings settings) {
                int rpm = Math.max(1, Math.abs(settings.value()));
                return super.formatSettings(settings)
                        .append(Component.literal(" · " + EnergyFormat.feRate((long) rpm * Config.motorFePerRpm))
                                .withStyle(ChatFormatting.YELLOW));
            }
        };
        speed.between(-max, max);
        speed.value = Math.min(16, max);
        speed.withCallback(i -> updateGeneratedRotation());
        behaviours.add(speed);
    }

    private int setRpm() {
        return Math.max(-Config.motorMaxRpm, Math.min(Config.motorMaxRpm, speed.getValue()));
    }

    /** FE per tick at the set speed. */
    public int feUse() {
        return Math.abs(setRpm()) * Config.motorFePerRpm;
    }

    public boolean isRunning() {
        return running;
    }

    private int bufferSize() {
        return 20 * Config.motorMaxRpm * Config.motorFePerRpm;
    }

    @Override
    public float getGeneratedSpeed() {
        return running ? setRpm() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = running ? Config.motorSu : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;
        if (level.isClientSide) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.tickAudio());
            return;
        }

        int use = feUse();
        if (running) {
            if (use > 0 && energy >= use) energy -= use;
            else setRunning(false);
        } else if (use > 0 && energy >= use * RESTART_TICKS) {
            energy -= use;
            setRunning(true);
        }
        if (level.getGameTime() % 20 == 0) {
            input = receivedThisSecond / 20;
            receivedThisSecond = 0;
            sendData();
        }
    }

    private void setRunning(boolean value) {
        running = value;
        updateGeneratedRotation();
        BlockState state = getBlockState();
        if (state.getBlock() instanceof ElectricMotorBlock && state.getValue(ElectricMotorBlock.LIT) != value)
            level.setBlock(worldPosition, state.setValue(ElectricMotorBlock.LIT, value), 3);
        setChanged();
    }

    @OnlyIn(Dist.CLIENT)
    public void tickAudio() {
        soundInstance = MachineSoundInstance.keepPlaying(level, soundInstance,
                getBlockState().getValue(ElectricMotorBlock.LIT), () -> new ElectricMotorSoundInstance(this));
    }

    // Takes FE from any side; never gives any back.
    private class MotorEnergy implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = Math.max(0, Math.min(toReceive, bufferSize() - energy));
            if (!simulate && received > 0) {
                energy += received;
                receivedThisSecond += received;
                setChanged();
            }
            return received;
        }

        @Override public int extractEnergy(int toExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return energy; }
        @Override public int getMaxEnergyStored() { return bufferSize(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Energy", energy);
        tag.putBoolean("Running", running);
        tag.putInt("Input", input);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        energy = tag.getInt("Energy");
        running = tag.getBoolean("Running");
        input = tag.getInt("Input");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.electric_motor_header").forGoggles(tooltip);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        CreateLang.translate("solar_punk.tooltip.consumption")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(running ? feUse() : 0))
                        .withStyle(running ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        CreateLang.translate("solar_punk.tooltip.stored")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.fe(energy) + " / " + EnergyFormat.fe(bufferSize()))
                        .withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);

        // Input below use means it will keep stopping and starting; say what speed the supply holds.
        boolean short_ = input < feUse();
        CreateLang.translate("solar_punk.tooltip.fe_input")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(EnergyFormat.feRate(input))
                        .withStyle(short_ ? ChatFormatting.RED : ChatFormatting.GREEN))
                .forGoggles(tooltip, 1);
        if (short_)
            CreateLang.translate("solar_punk.tooltip.electric_motor_sustains", input / Config.motorFePerRpm)
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip, 1);
        return true;
    }
}
