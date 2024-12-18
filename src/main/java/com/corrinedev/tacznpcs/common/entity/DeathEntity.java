package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.common.entity.inventory.ScavInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DeathEntity extends Mob implements GeoEntity, InventoryCarrier, HasCustomInventoryScreen, MenuProvider {
    private final AnimatableInstanceCache cache =  GeckoLibUtil.createInstanceCache(this);
    public SimpleContainer inventory;
    public AbstractScavEntity attachedEntity;
    public static final EntityType<DeathEntity> DEATH;
    static {
        DEATH = EntityType.Builder.of(, MobCategory.MONSTER).sized(0.65f, 1.95f).build("bandit");
    }
    public DeathEntity(EntityType<? extends DeathEntity> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        inventory = new SimpleContainer(27);
    }

    private DeathEntity(Level p_21684_, AbstractScavEntity entity) {
        this(DEATH, p_21684_);
        this.attachedEntity = entity;

    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 64.0D).add(Attributes.MOVEMENT_SPEED, 0.35F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void openCustomInventoryScreen(@NotNull Player pPlayer) {
        createMenu(999, pPlayer.getInventory(), pPlayer);
        pPlayer.openMenu(this);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return ScavInventory.generate(pContainerId, pPlayerInventory, inventory, this);
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        this.writeInventoryToTag(pCompound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        resetSlots();
        this.readInventoryFromTag(pCompound);

    }
    public void resetSlots() {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            this.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "controller", 5, event ->
            {
                return event.setAndContinue(
                                  RawAnimation.begin().thenLoop("idle"));
            }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    @Override
    public void tick() {
        if(this.inventory.isEmpty()) {
            this.die(this.damageSources().genericKill());
        }
        super.tick();
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        openCustomInventoryScreen(pPlayer);
        return super.interact(pPlayer, pHand);
    }
}
