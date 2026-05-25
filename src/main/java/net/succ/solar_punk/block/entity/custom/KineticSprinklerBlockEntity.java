package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.fluid.ModFluids;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class KineticSprinklerBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int SCAN_DEPTH = 5;

    public final FluidTank fluidTank = new FluidTank(Config.sprinklerTank) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            Fluid f = stack.getFluid();
            return f.isSame(Fluids.WATER) || f.isSame(ModFluids.FERTILIZER_SOURCE.get());
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    public KineticSprinklerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private boolean hasFertilizer() {
        return !fluidTank.isEmpty() && fluidTank.getFluid().getFluid().isSame(ModFluids.FERTILIZER_SOURCE.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        if (level.getGameTime() % 20 != 0) return;
        if (fluidTank.isEmpty()) return;

        boolean fertilizer = hasFertilizer();
        fluidTank.drain(Config.sprinklerFluidPerCycle, IFluidHandler.FluidAction.EXECUTE);

        ServerLevel serverLevel = (ServerLevel) level;

        for (int x = -Config.sprinklerRange; x <= Config.sprinklerRange; x++) {
            for (int z = -Config.sprinklerRange; z <= Config.sprinklerRange; z++) {
                for (int dy = 1; dy <= SCAN_DEPTH; dy++) {
                    BlockPos checkPos = worldPosition.offset(x, -dy, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    Block block = checkState.getBlock();

                    if (block instanceof FarmBlock) {
                        if (checkState.getValue(FarmBlock.MOISTURE) < 7)
                            level.setBlock(checkPos, checkState.setValue(FarmBlock.MOISTURE, 7), 2);
                        break;
                    } else if (block instanceof BonemealableBlock bonemealable) {
                        if (fertilizer) {
                            if (bonemealable.isValidBonemealTarget(serverLevel, checkPos, checkState))
                                bonemealable.performBonemeal(serverLevel, serverLevel.random, checkPos, checkState);
                        } else {
                            checkState.randomTick(serverLevel, checkPos, serverLevel.random);
                        }
                    } else if (!checkState.isAir()) {
                        break;
                    }
                }

                if (serverLevel.random.nextFloat() < 0.7f) {
                    double px = worldPosition.getX() + 0.5 + x + (serverLevel.random.nextDouble() - 0.5) * 0.8;
                    double py = worldPosition.getY() + 0.1;
                    double pz = worldPosition.getZ() + 0.5 + z + (serverLevel.random.nextDouble() - 0.5) * 0.8;
                    serverLevel.sendParticles(
                        fertilizer ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.FALLING_WATER,
                        px, py, pz, 1, 0, 0, 0, 0
                    );
                }
            }
        }

        setChanged();
        sendData();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("solar_punk.tooltip.sprinkler_header").forGoggles(tooltip);

        boolean active = !fluidTank.isEmpty();
        CreateLang.translate("solar_punk.tooltip.sprinkler_status")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(active ? "Active" : "Stopped")
                        .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        boolean fertilizer = hasFertilizer();
        CreateLang.translate(fertilizer ? "solar_punk.tooltip.fertilizer" : "solar_punk.tooltip.water")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(fluidTank.getFluidAmount())
                        .text(" / " + Config.sprinklerTank + " mB")
                        .style(fertilizer ? ChatFormatting.GREEN : ChatFormatting.AQUA)
                        .component())
                .forGoggles(tooltip, 1);

        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        FluidStack fluid = fluidTank.getFluid();
        if (!fluid.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            fluidTag.putInt("Amount", fluid.getAmount());
            tag.put("FluidTank", fluidTag);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("FluidTank")) {
            CompoundTag fluidTag = tag.getCompound("FluidTank");
            ResourceLocation fluidId = ResourceLocation.parse(fluidTag.getString("Fluid"));
            Fluid f = BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
            if (f != Fluids.EMPTY)
                fluidTank.setFluid(new FluidStack(f, fluidTag.getInt("Amount")));
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}
