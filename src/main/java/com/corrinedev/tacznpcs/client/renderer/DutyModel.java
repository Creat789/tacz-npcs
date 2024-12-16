package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import net.minecraft.resources.ResourceLocation;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class BanditModel extends ScavModel<BanditEntity> {
    public BanditModel() {
        super(new ResourceLocation(MODID, "geo/scav.geo.json"), new ResourceLocation(MODID, "textures/entity/terroristnoarmor.png"));
    }
}
