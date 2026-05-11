package net.succ.create_solar_powered.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.succ.create_solar_powered.Create_solar_powered;
import net.succ.create_solar_powered.fluid.ModFluids;
import net.succ.create_solar_powered.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Create_solar_powered.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.SALT.get());
        basicItem(ModFluids.MOLTEN_SALT_BUCKET.get());

    }
    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Create_solar_powered.MODID,
                        "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder blockItemFromBlock(DeferredBlock<Block> block) {
        return withExistingParent(block.getId().getPath(), ResourceLocation.fromNamespaceAndPath(
                Create_solar_powered.MODID, "block/" + block.getId().getPath()));
    }
}
