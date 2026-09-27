package org.mob.dysoncubeproject.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.block.ModBlocks;
import org.mob.dysoncubeproject.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends ForgeAdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new DCPAdvancementGenerator()));
    }

    public static class DCPAdvancementGenerator implements ForgeAdvancementProvider.AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer, ExistingFileHelper existingFileHelper) {
            Advancement root = Advancement.Builder.advancement()
                    .display(ModBlocks.EM_RAILEJECTOR_CONTROLLER.getKey().get(),
                            advancementName("root.title"),
                            advancementName("root.description"),
                            ResourceLocation.parse("minecraft:textures/block/iron_block.png"),
                            FrameType.TASK, false, true, false)
                    .addCriterion("railejector", InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}))
                    .save(consumer, getNameId("main/root"));
            
            int[] amounts = new int[]{5, 15, 25, 50, 75, 100};
            Advancement parent = root;
            
            for (int i = 0; i < amounts.length; i++) {
                parent = getAdvancement(parent, amounts[i] < 50 ? ModItems.SOLAR_SAIL.get() : ModItems.SOLAR_SAIL_PACKAGE.get(),
                        "em_railejector_controller/sphere_percentage_" + amounts[i], amounts[i] < 50 ? FrameType.TASK : FrameType.CHALLENGE, true, true, false)
                        .save(consumer, getNameId("main/em_railejector_controller/" + amounts[i]));
            }
        }

        private Advancement.Builder getAdvancement(Advancement parent, ItemLike display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden) {
            return Advancement.Builder.advancement().parent(parent)
                    .display(display,
                            advancementName(name + ".title"),
                            advancementName(name + ".description"),
                            null, frame, showToast, announceToChat, hidden)
                    .addCriterion("impossible", new ImpossibleTrigger.TriggerInstance());
        }

        public Component advancementName(String name) {
            return Component.translatable("advancement." + DysonCubeProject.MOD_ID + "." + name);
        }

        private String getNameId(String id) {
            return DysonCubeProject.MOD_ID + ":" + id;
        }
    }
}