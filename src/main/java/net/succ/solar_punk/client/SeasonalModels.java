package net.succ.solar_punk.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.util.SeasonalEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Holiday looks for blocks, by the real date when models load, so the look changes on the next
 * game start or resource reload after the date does. Two kinds:
 * <ul>
 *   <li>{@link Swap}: whole models in models/block/events/&lt;event&gt;/&lt;block&gt;.json (and
 *   &lt;block&gt;_lit.json for the LIT state), with their textures in the matching textures
 *   folder. For blocks with one model per LIT state.</li>
 *   <li>{@link TextureSwap}: keeps the block's models and only redraws one texture with
 *   textures/block/events/&lt;event&gt;/&lt;texture&gt;.png. For blocks with many shapes and
 *   rotations, like the Solar Shingles.</li>
 * </ul>
 * Each block takes the first active event in SeasonalEvent's priority order (the lists are
 * sorted by it), so on Dec 31 New Year's dresses the Solar Shingles while Christmas keeps the
 * rest. One event can make several swaps on the same block.
 */
public final class SeasonalModels {

    /** A block whose models are swapped during an event. Lit blocks also get a _lit model. */
    private record Swap(SeasonalEvent event, String block, boolean lit) {
        ResourceLocation model(boolean litState) {
            return ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID,
                    "block/events/" + event.id() + "/" + block + (litState ? "_lit" : ""));
        }
    }

    /** A block whose models draw a seasonal texture in place of block/&lt;texture&gt;. */
    private record TextureSwap(SeasonalEvent event, String block, String texture) {
        Material from() {
            return new Material(InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/" + texture));
        }

        Material to() {
            return new Material(InventoryMenu.BLOCK_ATLAS,
                    ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "block/events/" + event.id() + "/" + texture));
        }
    }

    private static final List<Swap> SWAPS = Stream.of(
            new Swap(SeasonalEvent.CHRISTMAS, "garden_light", true),
            new Swap(SeasonalEvent.CHRISTMAS, "streetlamp", true)
    ).sorted(Comparator.comparing(Swap::event)).toList();

    private static final List<TextureSwap> TEXTURE_SWAPS = Stream.concat(Stream.of(
            new TextureSwap(SeasonalEvent.NEW_YEARS, "solar_shingles", "solar_shingles"),
            new TextureSwap(SeasonalEvent.CHRISTMAS, "solar_shingles", "solar_shingles"),
            // Heart-shaped Trellis flowers, still tinted the colour of the flower used
            new TextureSwap(SeasonalEvent.VALENTINES, "trellis", "trellis_flowers")
    ), solarPanels(SeasonalEvent.ST_PATRICKS, SeasonalEvent.EASTER, SeasonalEvent.PRIDE))
            .sorted(Comparator.comparing(TextureSwap::event)).toList();

    // Both panels' top textures, lit and unlit: events/<event>/<panel>/solar_panel_top[_lit].png.
    private static Stream<TextureSwap> solarPanels(SeasonalEvent... events) {
        return Stream.of(events).flatMap(event -> Stream.of("andesite_solar_panel", "brass_solar_panel")
                .flatMap(panel -> Stream.of("solar_panel_top", "solar_panel_top_lit")
                        .map(texture -> new TextureSwap(event, panel, panel + "/" + texture))));
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        for (Swap swap : SWAPS) {
            event.register(ModelResourceLocation.standalone(swap.model(false)));
            if (swap.lit()) event.register(ModelResourceLocation.standalone(swap.model(true)));
        }
    }

    public static void apply(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        // Block -> the event dressing it. One event can make several swaps on a block.
        Map<String, SeasonalEvent> claimed = new HashMap<>();

        for (Swap swap : SWAPS) {
            if (!swap.event().isActive() || claimed.containsKey(swap.block())) continue;
            BakedModel off = models.get(ModelResourceLocation.standalone(swap.model(false)));
            BakedModel lit = swap.lit() ? models.get(ModelResourceLocation.standalone(swap.model(true))) : off;
            if (off == null || lit == null) continue;
            claimed.put(swap.block(), swap.event());
            models.replaceAll((key, model) -> {
                // The upper half of a two-block-tall block (the Streetlamp) keeps its own model;
                // the swapped lower model already reaches up through it.
                if (!isBlock(key, swap.block()) || key.variant().contains("half=upper")) return model;
                // The item shows the lit model, like the block's normal item.
                boolean litState = key.variant().equals(ModelResourceLocation.INVENTORY_VARIANT)
                        || key.variant().contains("lit=true");
                return litState ? lit : off;
            });
        }

        for (TextureSwap swap : TEXTURE_SWAPS) {
            if (!swap.event().isActive() || claimed.getOrDefault(swap.block(), swap.event()) != swap.event()) continue;
            TextureAtlasSprite from = event.getTextureGetter().apply(swap.from());
            TextureAtlasSprite to = event.getTextureGetter().apply(swap.to());
            if (from == to) continue; // the seasonal texture is missing
            claimed.put(swap.block(), swap.event());
            models.replaceAll((key, model) -> isBlock(key, swap.block()) ? new Retextured(model, from, to) : model);
        }
    }

    private static boolean isBlock(ModelResourceLocation key, String block) {
        return key.id().getNamespace().equals(SolarPunk.MODID) && key.id().getPath().equals(block);
    }

    /** Draws every quad that uses one sprite with another, at the same spot on the texture. */
    private static class Retextured extends BakedModelWrapper<BakedModel> {
        private final TextureAtlasSprite from, to;
        // Chunk meshing runs on several threads; BakedQuad has identity equality.
        private final Map<BakedQuad, BakedQuad> cache = new ConcurrentHashMap<>();

        Retextured(BakedModel model, TextureAtlasSprite from, TextureAtlasSprite to) {
            super(model);
            this.from = from;
            this.to = to;
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
            return retexture(super.getQuads(state, side, rand));
        }

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                                 @NotNull ModelData data, @Nullable RenderType renderType) {
            return retexture(super.getQuads(state, side, rand, data, renderType));
        }

        @Override
        public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
            TextureAtlasSprite sprite = super.getParticleIcon(data);
            return sprite == from ? to : sprite;
        }

        private List<BakedQuad> retexture(List<BakedQuad> quads) {
            return quads.stream().map(quad -> quad.getSprite() == from ? cache.computeIfAbsent(quad, this::remap) : quad).toList();
        }

        private BakedQuad remap(BakedQuad quad) {
            int[] data = quad.getVertices().clone();
            for (int i = 0; i + IQuadTransformer.STRIDE <= data.length; i += IQuadTransformer.STRIDE) {
                float u = Float.intBitsToFloat(data[i + IQuadTransformer.UV0]);
                float v = Float.intBitsToFloat(data[i + IQuadTransformer.UV0 + 1]);
                u = to.getU0() + (u - from.getU0()) / (from.getU1() - from.getU0()) * (to.getU1() - to.getU0());
                v = to.getV0() + (v - from.getV0()) / (from.getV1() - from.getV0()) * (to.getV1() - to.getV0());
                data[i + IQuadTransformer.UV0] = Float.floatToRawIntBits(u);
                data[i + IQuadTransformer.UV0 + 1] = Float.floatToRawIntBits(v);
            }
            return new BakedQuad(data, quad.getTintIndex(), quad.getDirection(), to, quad.isShade(), quad.hasAmbientOcclusion());
        }
    }

    private SeasonalModels() {}
}
