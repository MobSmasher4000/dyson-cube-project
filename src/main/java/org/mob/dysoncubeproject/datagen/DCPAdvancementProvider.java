package org.mob.dysoncubeproject.datagen;

import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import org.mob.dysoncubeproject.DCPContent;
import org.mob.dysoncubeproject.DysonCubeProject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DCPAdvancementProvider extends AdvancementProvider {

    public DCPAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new DCPAdvancementGenerator()));
    }

    public static class DCPAdvancementGenerator implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer) {
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(DCPContent.Blocks.EM_RAILEJECTOR_CONTROLLER,
                            advancementName("root.title"),
                            advancementName("root.description"),
                            Identifier.parse("minecraft:textures/block/iron_block.png"),
                            AdvancementType.TASK, false, true, false)
                    .addCriterion("railejector", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                    .save(consumer, getNameId("main/root"));

            var amounts = new int[]{5, 15, 25, 50, 75, 100};
            var parent = root;
            for (int i = 0; i < amounts.length; i++) {
                parent = getAdvancement(parent, amounts[i] < 50 ? DCPContent.Items.SOLAR_SAIL.get() : DCPContent.Items.SOLAR_SAIL_PACKAGE.get(),
                        "em_railejector_controller/sphere_percentage_" + amounts[i], amounts[i] < 50 ? AdvancementType.TASK : AdvancementType.CHALLENGE, true, true, false)
                        .save(consumer, getNameId("main/em_railejector_controller/" + amounts[i]));
            }
        }

        private Advancement.Builder getAdvancement(AdvancementHolder parent, ItemLike display, String name, AdvancementType frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return Advancement.Builder.advancement().parent(parent)
                    .display(display,
                            advancementName(name + ".title"),
                            advancementName(name + ".description"),
                            null, frame, showToast, announceToChat, hidden)
                    .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
        }

        public Component advancementName(String name) {
            return Component.translatable("advancement." + DysonCubeProject.MOD_ID + "." + name);
        }

        private String getNameId(String id) {
            return DysonCubeProject.MOD_ID + ":" + id;
        }
    }
}