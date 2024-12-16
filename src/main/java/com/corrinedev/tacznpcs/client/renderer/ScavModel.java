package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class ScavModel<T extends AbstractScavEntity> extends GeoModel<T> {
    private final ResourceLocation model  ;
    private final ResourceLocation texture;
    private static final ResourceLocation animationrifle = new ResourceLocation(MODID, "animations/scav.animation.json");
    private static final ResourceLocation animationpistol = new ResourceLocation(MODID, "animations/scavpistol.animation.json");
    public ScavModel(ResourceLocation inmodel, ResourceLocation intexture) {
        model = inmodel;
        texture = intexture;
    }
    @Override
    public ResourceLocation getModelResource(AbstractScavEntity object) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractScavEntity object) {
        return texture;
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * 0.017453292F);
            head.setRotY(entityData.netHeadYaw() * 0.017453292F);
        }
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractScavEntity object) {
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
