package net.succ.solar_punk.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.succ.solar_punk.SolarPunk;
import net.succ.solar_punk.datagen.AllSolarpunkAdvancements;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class SolarpunkAdvancement {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, "textures/block/salt_block.png");

    private static final String LANG_PREFIX = "advancement." + SolarPunk.MODID + ".";

    private final Advancement.Builder mcBuilder = Advancement.Builder.advancement();
    private SolarpunkAdvancement parent;
    private final Builder builder = new Builder();
    AdvancementHolder datagenResult;
    private final String id;
    private String title;
    private String description;

    public SolarpunkAdvancement(String id, UnaryOperator<Builder> configurator) {
        this.id = id;
        configurator.apply(this.builder);
        AllSolarpunkAdvancements.ENTRIES.add(this);
    }

    private String titleKey() { return LANG_PREFIX + id; }
    private String descriptionKey() { return titleKey() + ".desc"; }

    public void save(Consumer<AdvancementHolder> consumer, HolderLookup.Provider registries) {
        if (parent != null)
            mcBuilder.parent(parent.datagenResult);

        mcBuilder.display(
                builder.icon,
                Component.translatable(titleKey()),
                Component.translatable(descriptionKey()),
                id.equals("root") ? BACKGROUND : null,
                builder.type.advancementType,
                builder.type.toast,
                builder.type.announce,
                builder.type.hide
        );

        datagenResult = mcBuilder.save(consumer,
                ResourceLocation.fromNamespaceAndPath(SolarPunk.MODID, id).toString());
    }

    public void provideLang(BiConsumer<String, String> langOut) {
        langOut.accept(titleKey(), title);
        langOut.accept(descriptionKey(), description);
    }

    public static SolarpunkAdvancement create(String id, UnaryOperator<Builder> configurator) {
        return new SolarpunkAdvancement(id, configurator);
    }

    public enum TaskType {
        SILENT(AdvancementType.TASK,  false, false, false),
        NORMAL(AdvancementType.TASK,  true,  false, false),
        NOISY( AdvancementType.TASK,  true,  true,  false),
        EXPERT(AdvancementType.GOAL,  true,  true,  false),
        SECRET(AdvancementType.GOAL,  true,  true,  true);

        final AdvancementType advancementType;
        final boolean toast, announce, hide;

        TaskType(AdvancementType type, boolean toast, boolean announce, boolean hide) {
            this.advancementType = type;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    public class Builder {
        private TaskType type = TaskType.NORMAL;
        private int keyIndex = 0;
        private ItemStack icon = ItemStack.EMPTY;

        Builder() {}

        public Builder special(TaskType type) {
            this.type = type;
            return this;
        }

        public Builder after(SolarpunkAdvancement other) {
            SolarpunkAdvancement.this.parent = other;
            return this;
        }

        public Builder icon(ItemLike item) {
            this.icon = new ItemStack(item);
            return this;
        }

        public Builder title(String title) {
            SolarpunkAdvancement.this.title = title;
            return this;
        }

        public Builder description(String desc) {
            SolarpunkAdvancement.this.description = desc;
            return this;
        }

        public Builder awardedForFree() {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate[]{}));
            return this;
        }

        public Builder whenIconCollected() {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    InventoryChangeTrigger.TriggerInstance.hasItems(icon.getItem()));
            return this;
        }

        public Builder whenItemCollected(ItemLike item) {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    InventoryChangeTrigger.TriggerInstance.hasItems(item));
            return this;
        }

        public Builder withCustomTrigger(DeferredHolder<CriterionTrigger<?>, SolarpunkTrigger> holder) {
            mcBuilder.addCriterion(String.valueOf(keyIndex++),
                    holder.get().createCriterion(new SolarpunkTrigger.Instance()));
            return this;
        }
    }
}
