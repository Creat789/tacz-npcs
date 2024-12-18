package com.corrinedev.tacznpcs.client.renderer;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import software.bernie.geckolib.util.RenderUtils;

public class ScavRenderer<T extends AbstractScavEntity> extends GeoEntityRenderer<T> {
    public EntityRendererProvider.Context context;
    public ItemArmorGeoLayer<T> LAYER;
    public ScavRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
        this.context = renderManager;
        LAYER = new ItemArmorGeoLayer<T>(this) {
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
                // Return the appropriate armor item based on the bone name
                return switch (bone.getName()) {
                    case "left_boot", "right_boot" -> this.bootsStack;
                    case "left_armor_leg", "right_armor_leg" -> this.leggingsStack;
                    case "chestplate", "right_sleeve", "left_sleeve" -> this.chestplateStack;
                    case "helmet" -> this.helmetStack;
                    default -> null;
                };
            }
            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
                return switch (bone.getName()) {
                    case "left_boot" -> EquipmentSlot.FEET;
                    case "left_armor_leg", "right_armor_leg" -> EquipmentSlot.LEGS;
                    case "right_sleeve" -> !animatable.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                    case "left_sleeve" -> animatable.isLeftHanded() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                    case "chestplate" -> EquipmentSlot.CHEST;
                    case "helmet" -> EquipmentSlot.HEAD;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }
            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable, HumanoidModel<?> baseModel) {
                // Map the bone to the corresponding models part
                return switch (bone.getName()) {
                    case "left_boot", "left_armor_leg" -> baseModel.leftLeg;
                    case "right_boot", "right_armor_leg" -> baseModel.rightLeg;
                    case "right_sleeve" -> baseModel.rightArm;
                    case "left_sleeve" -> baseModel.leftArm;
                    case "chestplate" -> baseModel.body;
                    case "helmet" -> baseModel.head;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }
        };

        addRenderLayer(LAYER);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

    }
    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (bone.getName().equals("third_person_right_hand")) {
            
            poseStack.pushPose();

            RenderUtils.translateMatrixToBone(poseStack, bone);
            poseStack.translate(0.4, 0.8, -0.1);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.scale(1.15f, 1.15f, 1.15f);
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
