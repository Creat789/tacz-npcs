package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class BanditModel extends GeoModel<BanditEntity> {
    private static final ResourceLocation model = new ResourceLocation(MODID, "geo/scav.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(MODID, "textures/entity/terroristnoarmor.png");
    private static final ResourceLocation animationrifle = new ResourceLocation(MODID, "animations/scav.animation.json");
    private static final ResourceLocation animationpistol = new ResourceLocation(MODID, "animations/scavpistol.animation.json");

    @Override
    public ResourceLocation getModelResource(BanditEntity object) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(BanditEntity object) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(BanditEntity object) {
        if(object.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(TimelessAPI.getCommonGunIndex(gun.getGunId(object.getMainHandItem())).isPresent()) {
                if(TimelessAPI.getCommonGunIndex(gun.getGunId(object.getMainHandItem())).get().getType().equalsIgnoreCase("pistol")) {
                    return animationpistol;
                }
            }
            return animationrifle;
        }
        return animationpistol;
    }
}
