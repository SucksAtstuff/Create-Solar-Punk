package net.succ.solar_punk.fluid;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.block.ModBlocks;
import net.succ.solar_punk.item.ModItems;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, SolarPunk.MODID);

    public static final Supplier<FlowingFluid> MOLTEN_SALT_SOURCE = FLUIDS.register("molten_salt",
            () -> new BaseFlowingFluid.Source(ModFluids.MOLTEN_SALT_PROPERTIES));
    public static final Supplier<FlowingFluid> MOLTEN_SALT_FLOWING = FLUIDS.register("molten_salt_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.MOLTEN_SALT_PROPERTIES));

    public static final DeferredBlock<LiquidBlock> MOLTEN_SALT_BLOCK = ModBlocks.BLOCKS.register("molten_salt",
            () -> new LiquidBlock(MOLTEN_SALT_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER)
                    .mapColor(MapColor.COLOR_CYAN)));

    public static final DeferredItem<BucketItem> MOLTEN_SALT_BUCKET = ModItems.ITEMS.registerItem("molten_salt_bucket",
            properties -> new BucketItem(MOLTEN_SALT_SOURCE.get(), properties.stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final BaseFlowingFluid.Properties MOLTEN_SALT_PROPERTIES = new BaseFlowingFluid.Properties(
            ModFluidTypes.MOLTEN_SALT_TYPE, MOLTEN_SALT_SOURCE, MOLTEN_SALT_FLOWING)
            .tickRate(30)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(1)
            .block(MOLTEN_SALT_BLOCK)
            .bucket(MOLTEN_SALT_BUCKET);

    public static final Supplier<FlowingFluid> BIOFUEL_SOURCE = FLUIDS.register("biofuel",
            () -> new BaseFlowingFluid.Source(ModFluids.BIOFUEL_PROPERTIES));
    public static final Supplier<FlowingFluid> BIOFUEL_FLOWING = FLUIDS.register("biofuel_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.BIOFUEL_PROPERTIES));

    public static final DeferredBlock<LiquidBlock> BIOFUEL_BLOCK = ModBlocks.BLOCKS.register("biofuel",
            () -> new LiquidBlock(BIOFUEL_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER)
                    .mapColor(MapColor.COLOR_GREEN)));

    public static final DeferredItem<BucketItem> BIOFUEL_BUCKET = ModItems.ITEMS.registerItem("biofuel_bucket",
            properties -> new BucketItem(BIOFUEL_SOURCE.get(), properties.stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final BaseFlowingFluid.Properties BIOFUEL_PROPERTIES = new BaseFlowingFluid.Properties(
            ModFluidTypes.BIOFUEL_TYPE, BIOFUEL_SOURCE, BIOFUEL_FLOWING)
            .tickRate(5)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .block(BIOFUEL_BLOCK)
            .bucket(BIOFUEL_BUCKET);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}