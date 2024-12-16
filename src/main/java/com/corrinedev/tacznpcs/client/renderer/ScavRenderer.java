package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BanditRenderer extends GeoEntityRenderer<BanditEntity> {
    public EntityRendererProvider.Context context;
    public BanditRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BanditModel());
        this.context = renderManager;
    }

    @Override
    public void render(BanditEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
       //poseStack.pushPose();
       //ItemStack stackToRender = entity.getMainHandItem();
       //poseStack.translate(0,1,0);
       //Minecraft minecraft = Minecraft.getInstance();
       //BakedModel model = minecraft.getItemRenderer().getModel(stackToRender, minecraft.player.level(), minecraft.player, minecraft.player.getId() + ItemDisplayContext.GROUND.ordinal());
       //RenderSystem.applyModelViewMatrix();

        //context.getItemRenderer().render(stackToRender, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);
        // poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

    }

    @Override
    public void renderRecursively(PoseStack poseStack, BanditEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (bone.getName().equals("third_person_right_hand")) {
            poseStack.pushPose();
            RenderUtils.translateMatrixToBone(poseStack, bone);
            poseStack.translate(0.4, 0.8, -0.1);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.scale(1.35f, 1.35f, 1.35f);
            ItemStack itemstack = animatable.getMainHandItem();
            if(!itemstack.isEmpty()){
                Minecraft minecraft = Minecraft.getInstance();
                BakedModel model = minecraft.getItemRenderer().getModel(itemstack, minecraft.player.level(), minecraft.player, minecraft.player.getId() + ItemDisplayContext.THIRD_PERSON_RIGHT_HAND.ordinal());
                context.getItemRenderer().render(itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, bufferSource, packedLight, packedOverlay, model);
            }
            poseStack.popPose();
        }
    }
}
