package org.mob.dysoncubeproject.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mob.dysoncubeproject.block.ModBlocks;

import java.util.List;

public class DysonComponentItem extends Item {

    private final int solarSail;
    private final int beam;

    public DysonComponentItem(int solarSail, int beam) {
        super(new Properties());
        this.solarSail = solarSail;
        this.beam = beam;
    }

    public int getSolarSail() {
        return this.solarSail;
    }

    public int getBeam() {
        return this.beam;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (getBeam()>0){
        pTooltipComponents.add(Component.translatable("tooltip.dysoncubeproject.contains_beams",getBeam())
                .withStyle(style -> style.withColor(ModBlocks.CYAN_COLOR)));
        }
        if (getSolarSail()>0){
            pTooltipComponents.add(Component.translatable("tooltip.dysoncubeproject.contains_solar_sails",getSolarSail())
                    .withStyle(style -> style.withColor(ModBlocks.CYAN_COLOR)));
        }
    }
}