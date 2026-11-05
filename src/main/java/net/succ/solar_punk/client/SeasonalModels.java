package net.succ.solar_punk.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.util.SeasonalEvent;

import java.util.List;
import java.util.Map;

/**
 * Holiday looks for blocks. Each swap's models live in models/block/events/<event>/<block>.json
 * (and <block>_lit.json for the LIT state), with their textures in the matching textures
 * folder. They're always baked, and while the event is on they replace the block's normal
 * models when models load, so the look changes on the next game start or resource reload
 * after the date does.
 */
public final class SeasonalModels {

    /** A block whose models are swapped during an event. Lit blocks also get a _lit model. */
    private record Swap(SeasonalEvent event, String block, boolean lit) {
        ResourceLocation model(boolean litState) {
            return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID,
                    "block/events/" + event.id() + "/" + block + (litState ? "_lit" : ""));
        }
    }

    private static final List<Swap> SWAPS = List.of(
            new Swap(SeasonalEvent.CHRISTMAS, "garden_light", true)
    );

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (Swap swap : SWAPS) {
            event.register(ModelResourceLocation.standalone(swap.model(false)));
            if (swap.lit()) event.register(ModelResourceLocation.standalone(swap.model(true)));
        }
    }

    public static void apply(Map<ModelResourceLocation, BakedModel> models) {
        for (Swap swap : SWAPS) {
            if (!swap.event().isActive()) continue;
            BakedModel off = models.get(ModelResourceLocation.standalone(swap.model(false)));
            BakedModel lit = swap.lit() ? models.get(ModelResourceLocation.standalone(swap.model(true))) : off;
            if (off == null || lit == null) continue;
            models.replaceAll((key, model) -> {
                if (!key.id().getNamespace().equals(SolarPunk.MODID) || !key.id().getPath().equals(swap.block()))
                    return model;
                // The item shows the lit model, like the normal Garden Light's item.
                boolean litState = key.variant().equals(ModelResourceLocation.INVENTORY_VARIANT)
                        || key.variant().contains("lit=true");
                return litState ? lit : off;
            });
        }
    }

    private SeasonalModels() {}
}
