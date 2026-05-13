package net.succ.solar_punk.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.fluid.ModFluids;
import net.succ.solar_punk.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SolarPunk.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.SALT.get());
        basicItem(ModItems.BIOMASS.get());
        basicItem(ModItems.BIOMASS_PELLET.get());
        basicItem(ModFluids.MOLTEN_SALT_BUCKET.get());
        basicItem(ModFluids.BIOFUEL_BUCKET.get());

    }
    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID,
                        "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder blockItemFromBlock(DeferredBlock<Block> block) {
        return withExistingParent(block.getId().getPath(), ResourceLocation.fromNamespaceAndPath(
                SolarPunk.MODID, "block/" + block.getId().getPath()));
    }
}
