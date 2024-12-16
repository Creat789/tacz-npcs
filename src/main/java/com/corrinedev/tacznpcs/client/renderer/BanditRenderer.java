package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.client.models.BanditModel;
import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BanditRenderer extends ScavRenderer<BanditEntity> {
    public BanditRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BanditModel());
    }
}
