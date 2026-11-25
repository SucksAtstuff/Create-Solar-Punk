package net.succ.solar_punk.worldgen.structure;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.SolarPunk;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds the solarpunk houses to the vanilla village house pools when a server starts: one build
 * per village type (plains, desert, savanna, snowy, taiga), in structure/village/<type>/.
 * Vanilla has no API for adding to a pool, so the pool's lists are reached by reflection; any
 * failure is logged once and the houses are simply left out.
 */
public class VillageHouses {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** One village type: its house pool, which house goes in and how it's processed. */
    private record Entry(String village, String house, ResourceKey<StructureProcessorList> processors) {}

    private static final List<Entry> ENTRIES = List.of(
            new Entry("plains", "plains", processorKey("minecraft", "mossify_10_percent")),
            new Entry("desert", "desert", processorKey("minecraft", "empty")),
            new Entry("savanna", "savanna", processorKey("minecraft", "empty")),
            new Entry("snowy", "snowy", processorKey("minecraft", "empty")),
            new Entry("taiga", "taiga", processorKey("minecraft", "mossify_10_percent")));

    public static ResourceKey<StructureProcessorList> processorKey(String namespace, String path) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        int weight = Config.villageSolarHouseWeight;
        if (weight <= 0) return;
        var access = event.getServer().registryAccess();
        Registry<StructureTemplatePool> pools = access.registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorLists = access.registryOrThrow(Registries.PROCESSOR_LIST);
        try {
            for (Entry entry : ENTRIES) {
                StructureTemplatePool pool = pools.get(ResourceLocation.withDefaultNamespace("village/" + entry.village + "/houses"));
                if (pool == null) continue;
                Holder<StructureProcessorList> processors = processorLists.getHolderOrThrow(entry.processors);
                StructurePoolElement element = StructurePoolElement
                        .single(SolarPunk.MODID + ":village/" + entry.house + "/solar_house", processors)
                        .apply(StructureTemplatePool.Projection.RIGID);
                addToPool(pool, element, weight);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Couldn't add the solarpunk houses to villages", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addToPool(StructureTemplatePool pool, StructurePoolElement element, int weight)
            throws ReflectiveOperationException {
        // templates: one copy per point of weight, which is what generation picks from
        Field templatesField = StructureTemplatePool.class.getDeclaredField("templates");
        templatesField.setAccessible(true);
        ObjectArrayList<StructurePoolElement> templates = (ObjectArrayList<StructurePoolElement>) templatesField.get(pool);
        for (int i = 0; i < weight; i++) templates.add(element);

        // rawTemplates: the weighted list the pool is saved with (immutable, so replaced)
        Field rawField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
        rawField.setAccessible(true);
        List<Pair<StructurePoolElement, Integer>> raw =
                new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) rawField.get(pool));
        raw.add(Pair.of(element, weight));
        rawField.set(pool, raw);
    }
}
