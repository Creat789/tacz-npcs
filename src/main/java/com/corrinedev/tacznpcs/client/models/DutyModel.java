package com.corrinedev.tacznpcs.client.models;

import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import net.minecraft.resources.ResourceLocation;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class DutyModel extends ScavModel<DutyEntity> {
    public DutyModel() {
        super(new ResourceLocation(MODID, "geo/scav.geo.json"), new ResourceLocation(MODID, "textures/entity/scavpistol.png"));
    }
}
