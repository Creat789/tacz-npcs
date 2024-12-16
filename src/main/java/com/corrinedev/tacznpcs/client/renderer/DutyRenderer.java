package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DutyRenderer extends ScavRenderer<DutyEntity> {
    public DutyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DutyModel());
    }
}
