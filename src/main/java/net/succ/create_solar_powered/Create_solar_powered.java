package net.succ.create_solar_powered;

import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.succ.create_solar_powered.block.ModBlocks;
import net.succ.create_solar_powered.block.entity.ModBlockEntities;
import net.succ.create_solar_powered.datagen.DataGenerators;
import net.succ.create_solar_powered.item.ModCreativeModeTabs;
import net.succ.create_solar_powered.item.ModItems;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

@Mod(Create_solar_powered.MODID)
public class Create_solar_powered {
    public static final String MODID = "create_solar_powered";

    private static final int MAX_PIPE_SEARCH = 64;

    public Create_solar_powered(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        modEventBus.addListener(DataGenerators::gatherData);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(Create_solar_powered::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // Solar heater placed directly below a boiler (no heat pipe needed)
        BoilerHeater.REGISTRY.register(ModBlocks.SOLAR_HEATER.get(), (level, pos, state) -> {
            if (!level.canSeeSky(pos.above())) return BoilerHeater.NO_HEAT;
            return solarHeat(level);
        });

        // Heat pipe: BFS through the pipe network (and boiler blocks) to find connected
        // solar heaters. Only the pipe with the minimum BlockPos key among all active
        // heater pipes in the network provides heat, equal to numSolarHeaters * heatPerHeater.
        // This prevents a single solar heater from being counted once per pipe under the boiler.
        BoilerHeater.REGISTRY.register(ModBlocks.HEAT_PIPE.get(), (level, pos, state) -> {
            Set<BlockPos> visited = new HashSet<>();
            Deque<BlockPos> queue = new ArrayDeque<>();
            queue.add(pos);
            visited.add(pos);

            int solarHeaterCount = 0;
            long leaderKey = pos.asLong(); // minimum position key among active heater pipes

            while (!queue.isEmpty()) {
                if (visited.size() > MAX_PIPE_SEARCH) break;
                BlockPos current = queue.poll();
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (visited.contains(neighbor)) continue;
                    visited.add(neighbor);
                    BlockState neighborState = level.getBlockState(neighbor);
                    Block neighborBlock = neighborState.getBlock();
                    if (neighborBlock == ModBlocks.SOLAR_HEATER.get()) {
                        // Skip heaters touching a tank — they report their own heat directly
                        boolean touchingTank = false;
                        for (Direction d : Direction.values()) {
                            if (FluidTankBlock.isTank(level.getBlockState(neighbor.relative(d)))) {
                                touchingTank = true;
                                break;
                            }
                        }
                        if (!touchingTank && level.canSeeSky(neighbor.above())) solarHeaterCount++;
                        queue.add(neighbor); // traverse through heater chains to find more
                    } else if (neighborBlock == ModBlocks.HEAT_PIPE.get()) {
                        // If this pipe is also directly below a fluid tank it is an active heater
                        if (FluidTankBlock.isTank(level.getBlockState(neighbor.above())))
                            leaderKey = Math.min(leaderKey, neighbor.asLong());
                        queue.add(neighbor);
                    } else if (FluidTankBlock.isTank(neighborState)) {
                        queue.add(neighbor);
                    }
                }
            }

            if (solarHeaterCount == 0) return BoilerHeater.NO_HEAT;
            // Non-leader pipes yield nothing so the total heat equals numHeaters × heatPerHeater
            if (pos.asLong() != leaderKey) return BoilerHeater.NO_HEAT;

            float heat = solarHeat(level);
            if (heat < 0) return BoilerHeater.NO_HEAT;
            return solarHeaterCount * heat;
        });
    }

    private static float solarHeat(Level level) {
        long time = level.getDayTime() % 24000;
        if (time >= 12000 || level.isThundering()) return BoilerHeater.NO_HEAT;
        if (time < 2000 || time >= 10000) // morning / evening
            return level.isRaining() ? BoilerHeater.PASSIVE_HEAT : 1;
        // noon
        return level.isRaining() ? 1 : 2;
    }
}
