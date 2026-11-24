package org.mob.dysoncubeproject.item;

import net.minecraft.resources.ResourceKey;
import org.mob.dysoncubeproject.DCPAttachments;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.world.item.Item;

public class DysonComponentItem extends Item {

    public DysonComponentItem(ResourceKey<Item> id, int solarSail, int beam, TitaniumTab tab) {
        super(new Properties().setId(id)
                .component(DCPAttachments.SOLAR_SAIL, solarSail)
                .component(DCPAttachments.BEAM, beam));
        tab.getTabList().add(this);
    }
}
