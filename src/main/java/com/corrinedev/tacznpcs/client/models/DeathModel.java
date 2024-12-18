package com.corrinedev.tacznpcs.client.models;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.corrinedev.tacznpcs.common.entity.DeathEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class DeathModel extends GeoModel<DeathEntity> {
    private static final ResourceLocation animationrifle = new ResourceLocation(MODID, "animations/scav.animation.json");
    private static final ResourceLocation animationpistol = new ResourceLocation(MODID, "animations/scavpistol.animation.json");
    public DeathModel() {
    }
    @Override
    public ResourceLocation getModelResource(DeathEntity object) {
        return new ResourceLocation(MODID, "geo/scav.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DeathEntity object) {
        if(object.attachedEntity instanceof DutyEntity) {
            return new ResourceLocation(MODID, "textures/entity/duty.png");
        }
        return new ResourceLocation(MODID, "textures/entity/bandit.png");
    }

    @Override
    public void setCustomAnimations(DeathEntity animatable, long instanceId, AnimationState<DeathEntity> animationState) {
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * 0.017453292F);
            head.setRotY(entityData.netHeadYaw() * 0.017453292F);
        }
    }

    @Override
    public ResourceLocation getAnimationResource(DeathEntity object) {
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
