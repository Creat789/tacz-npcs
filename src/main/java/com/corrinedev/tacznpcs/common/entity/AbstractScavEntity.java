package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.common.entity.behavior.TaczShootAttack;
import com.corrinedev.tacznpcs.common.entity.inventory.ScavInventory;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.*;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunFire;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.CycleTaskHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractScavEntity extends PathfinderMob implements GeoEntity, SmartBrainOwner<AbstractScavEntity>, RangedAttackMob, IGunOperator, InventoryCarrier, HasCustomInventoryScreen, MenuProvider {
    private final AnimatableInstanceCache cache =  GeckoLibUtil.createInstanceCache(this);
    public int rangedCooldown = 0;
    public boolean firing = true;
    public int collectiveShots = 0;
    public boolean panic = false;
    public int paniccooldown = 0;
    public boolean isReloading = false;
    public boolean isAvoiding = false;
    public SimpleContainer inventory;
    protected AbstractScavEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        initialGunOperateData();
        inventory = new SimpleContainer(27);
        this.tacz$draw = new LivingEntityDrawGun(this.tacz$shooter, this.tacz$data);
        this.tacz$aim = new LivingEntityAim(this.tacz$shooter, this.tacz$data);
        this.tacz$crawl = new LivingEntityCrawl(this.tacz$shooter, this.tacz$data);
        this.tacz$ammoCheck = new LivingEntityAmmoCheck(this.tacz$shooter);
        this.tacz$fireSelect = new LivingEntityFireSelect(this.tacz$shooter, this.tacz$data);
        this.tacz$melee = new LivingEntityMelee(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$shoot = new LivingEntityShoot(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$bolt = new LivingEntityBolt(this.tacz$data, this.tacz$draw, this.tacz$shoot);
        this.tacz$reload = new LivingEntityReload(this.tacz$shooter, this.tacz$data, this.tacz$draw, this.tacz$shoot);
        this.tacz$speed = new LivingEntitySpeedModifier(this.tacz$shooter, this.tacz$data);
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 64.0D).add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void openCustomInventoryScreen(Player pPlayer) {
        createMenu(999, pPlayer.getInventory(), pPlayer);
        pPlayer.openMenu(this);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return ScavInventory.generate(pContainerId, pPlayerInventory, inventory, this);
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.writeInventoryToTag(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        resetSlots();
        this.readInventoryFromTag(pCompound);

    }
    public void resetSlots() {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            this.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void onEquipItem(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem) {
        boolean flag = pNewItem.isEmpty() && pOldItem.isEmpty();
        if (!flag && !ItemStack.isSameItemSameTags(pOldItem, pNewItem) && !this.firstTick) {
            Equipable equipable = Equipable.get(pNewItem);
            if (equipable != null && !this.isSpectator() && equipable.getEquipmentSlot() == pSlot) {
                if (this.doesEmitEquipEvent(pSlot)) {
                    this.gameEvent(GameEvent.EQUIP);
                }
            }

        }
    }

    public abstract boolean allowInventory(Player player);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "controller", 5, event ->
            {
                return event.setAndContinue(
                        // If sprinting, play the run animation
                        this.isReloading ? RawAnimation.begin().thenPlayAndHold("reload_upper") :
                                event.getAnimatable().isSprinting() ? RawAnimation.begin().thenLoop("run") :
                                // If moving, play the walk animation
                                event.isMoving() ? RawAnimation.begin().thenLoop("walk"):
                                        firing ? RawAnimation.begin().thenLoop("aim_upper") :

                                                // If not moving, play the idle animation
                                  RawAnimation.begin().thenLoop("idle"));
            })
                    // Sets a Sound KeyFrame
                    .setSoundKeyframeHandler(event -> {
                        //Plays the step sound on the walk keyframes in an animation
                        if (event.getKeyframeData().getSound().matches("walk")) {
                            if(level().isClientSide) {
                                playStepSound(this.blockPosition(), this.getFeetBlockState());
                            }
                        }
                    }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    public BrainActivityGroup<? extends AbstractScavEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                new Panic<>().panicIf((e, d) -> d.is(DamageTypes.PLAYER_ATTACK)).setRadius(3).stopIf((e)->!BehaviorUtils.canSee(this, this.getTarget())).whenStarting((e)-> {panic = true; paniccooldown = RandomSource.create().nextInt(160, 200);}).runFor((e)-> 60),

                (new AvoidEntity<>()).noCloserThan(16).speedModifier(1.0f).avoiding((entity) -> entity instanceof Player).startCondition((e) -> this.tacz$data.reloadStateType.isReloading()).whenStarting((e)-> this.isAvoiding = true).whenStopping((e) -> this.isAvoiding = false),
                (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextIntBetweenInclusive(40, 300)).stopIf((e)-> this.getTarget() == null),
                new MoveToWalkTarget<>()});
    }

    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
        for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
            this.spawnAtLocation(inventory.removeItem(i, inventory.getItem(i).getCount()));
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
            this.spawnAtLocation(inventory.removeItem(i, inventory.getItem(i).getCount()));
        }
        for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = this.getItemBySlot(equipmentslot);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                }

                this.spawnAtLocation(itemstack);
                this.setItemSlot(equipmentslot, ItemStack.EMPTY);
            }
        }
    }

    public BrainActivityGroup<? extends AbstractScavEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[]{new FirstApplicableBehaviour(new ExtendedBehaviour[]{
                new TargetOrRetaliate<>(),
                new SetPlayerLookTarget<>(),
                new SetRandomLookTarget<>()}),
                new OneRandomBehaviour(new ExtendedBehaviour[]{(
                        new SetRandomWalkTarget<>()).speedModifier(1.0F),
                        (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60))})});
    }

    public BrainActivityGroup<? extends AbstractScavEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new Behavior[]{
                new InvalidateAttackTarget<>(),
                (new SetWalkTargetToAttackTarget<>()).startCondition((entity) ->( !isUsingGun() || !BehaviorUtils.canSee(this, Objects.requireNonNull(this.getTarget()))) && !panic),
                new FirstApplicableBehaviour<>(new ExtendedBehaviour[]{
                        (new OneRandomBehaviour<>(new ExtendedBehaviour[]{
                                (new TaczShootAttack<>(0, 64).startCondition((x$0) ->  !isAvoiding && isUsingGun() && !isReloading && !panic && collectiveShots < getStateBurst() && this.hasLineOfSight(Objects.requireNonNull(this.getTarget()))).whenStarting((e)-> {firing = true; collectiveShots++;}).stopIf((e) -> this.isReloading || this.panic || !this.hasLineOfSight(this.getTarget()))),
                                (new TaczShootAttack<>(0, 64).startCondition((x$0) -> !isAvoiding && heldGunType() != null && Objects.equals(heldGunType(), GunTabType.MG) && isUsingGun() && !isReloading && !panic && !this.hasLineOfSight(Objects.requireNonNull(this.getTarget()))).whenStarting((e)-> {}).stopIf((e) -> this.isReloading || this.panic || this.hasLineOfSight(this.getTarget())))
                        })),
                        (new AnimatableMeleeAttack<>(0)).whenStarting((entity) -> this.setAggressive(true)).whenStopping((entity) -> this.setAggressive(false))})});
    }
    public boolean isUsingGun() {
        return this.getMainHandItem().getItem() instanceof ModernKineticGunItem;
    }
    @Override
    public abstract List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors();

    public @Nullable GunTabType heldGunType() {
        if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            return switch (TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType()) {
                case "pistol" -> GunTabType.PISTOL;
                case "rifle" -> GunTabType.RIFLE;
                case "sniper" -> GunTabType.SNIPER;
                case "smg" -> GunTabType.SMG;
                case "rpg" -> GunTabType.RPG;
                case "shotgun" -> GunTabType.SHOTGUN;
                case "mg" -> GunTabType.MG;
                default ->
                        throw new IllegalStateException("Unexpected value: " + TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType());
            };
        }
            return null;
    }
    public static @Nullable GunTabType heldGunType(ItemStack gunStack) {
        if(gunStack.getItem() instanceof ModernKineticGunItem gun) {
            switch (TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).get().getType()) {
                case "pistol" : return GunTabType.PISTOL;
                case "rifle" : return GunTabType.RIFLE;
                case "sniper" : return GunTabType.SNIPER;
                case "smg" : return GunTabType.SMG;
                case "rpg" : return GunTabType.RPG;
                case "shotgun" : return GunTabType.SHOTGUN;
                case "mg" : return GunTabType.MG;
            }
        }
        return null;
    }
    public int getStateRangedCooldown() {
        if(heldGunType() != null) {
            return switch (heldGunType()) {
                case RIFLE -> 5;
                case PISTOL -> 3;
                case SNIPER -> 30;
                case SHOTGUN -> 10;
                case SMG, MG -> 1;
                case RPG -> 100;
            };
        }
        return 60;
    }
    int getStateBurst() {
        if(heldGunType() != null) {
            return switch (heldGunType()) {
                case RIFLE -> 3;
                case PISTOL -> 4;
                case SNIPER -> 1;
                case SHOTGUN -> 1;
                case SMG, MG -> 5;
                case RPG -> 1;
            };
        }
        return 1;
    }
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        this.tickBrain(this);
    }

    public void pickUpItem(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        if (this.wantsToPickUp(itemstack)) {
            SimpleContainer simplecontainer = inventory;
            boolean flag = simplecontainer.canAddItem(itemstack);
            if (!flag) {
                return;
            }

            this.onItemPickup(pItemEntity);
            int i = itemstack.getCount();
            ItemStack itemstack1 = simplecontainer.addItem(itemstack);
            this.take(pItemEntity, i - itemstack1.getCount());
            if (itemstack1.isEmpty()) {
                pItemEntity.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
    }

    @Override
    public void tick() {
        onTickServerSide();
        if(inventory.hasAnyMatching((i) -> i.getItem() instanceof ArmorItem)) {
            for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                if (inventory.getItem(i).getItem() instanceof ArmorItem item) {
                    if(this.getItemBySlot(item.getEquipmentSlot()).isEmpty()) {
                        this.setItemSlotAndDropWhenKilled(item.getEquipmentSlot(), inventory.getItem(i));
                    }
                }
            }
        }
            if(inventory.hasAnyOf(Set.of(ModItems.MODERN_KINETIC_GUN.get())) && !(this.getMainHandItem().getItem() instanceof ModernKineticGunItem)) {
                for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                    if (inventory.getItem(i).is(ModItems.MODERN_KINETIC_GUN.get())) {
                        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, inventory.getItem(i));
                    }
                }
            } else if(!(this.getMainHandItem().getItem() instanceof ModernKineticGunItem)) {
                for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                    if (inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
                        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, inventory.getItem(i));
                        //inventory.removeItem(i, 1);
                    }
                }
            }
        if(getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(this.getMainHandItem().getOrCreateTag().getInt("GunCurrentAmmoCount") == 0) {
                this.reload();
            }
        }
        if (getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(gun.getCurrentAmmoCount(getMainHandItem()) > 0) {
                this.isReloading = false;
            }
        }
        if (firing) {
            if ((System.currentTimeMillis() - tacz$data.shootTimestamp) / 100 > getStateRangedCooldown()) {
                collectiveShots = 0;
            }
        }
        if(rangedCooldown != 0) {
            rangedCooldown--;
        }
        if(paniccooldown != 0) {
            paniccooldown--;
            if(paniccooldown == 1) {
                panic = false;
            }
        }
        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.1));
            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if (item.getItem().getItem() instanceof ModernKineticGunItem || item.getItem().is(ItemTags.AXES) || item.getItem().is(ItemTags.SWORDS) || item.getItem().getItem() instanceof ArmorItem || item.getItem().getItem() instanceof AmmoItem) {

                        pickUpItem(item);
                    }
                }
        }
        super.tick();
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if(allowInventory(pPlayer)) {
            openCustomInventoryScreen(pPlayer);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    protected void doSpawnBulletEntity(Level world, LivingEntity shooter, ItemStack gunItem, float pitch, float yaw, float speed, float inaccuracy, ResourceLocation ammoId, ResourceLocation gunId, GunData gunData, BulletData bulletData) {
        EntityKineticBullet bullet = new EntityKineticBullet(world, shooter, gunItem, ammoId, gunId, true, gunData, bulletData);
        bullet.shootFromRotation(bullet, pitch, yaw, 0.0F, speed, inaccuracy);
        world.addFreshEntity(bullet);
    }
    @Override
    public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
        //lookAt(pTarget, 10, 10);
        if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            ItemStack gunItem = this.getMainHandItem();
            ResourceLocation gunId = gun.getGunId(gunItem);
            IGun iGun = IGun.getIGunOrNull(gunItem);
            LivingEntity shooter = this;
            if (iGun != null) {
                Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                if (!gunIndexOptional.isEmpty()) {
                    CommonGunIndex gunIndex = (CommonGunIndex)gunIndexOptional.get();
                    BulletData bulletData = gunIndex.getBulletData();
                    GunData gunData = gunIndex.getGunData();
                    ResourceLocation ammoId = gunData.getAmmoId();
                    FireMode fireMode = iGun.getFireMode(gunItem);
                    AttachmentCacheProperty cacheProperty = this.getCacheProperty();
                    if (cacheProperty != null) {
                        InaccuracyType inaccuracyType = InaccuracyType.getInaccuracyType(shooter);
                        float inaccuracy = Math.max(0.0F, (Float)((Map)cacheProperty.getCache("inaccuracy")).get(inaccuracyType));
                        //(Float)((Map)cacheProperty.getCache("inaccuracy")).get(inaccuracyType)
                        if (inaccuracyType == InaccuracyType.AIM) {
                            inaccuracy = Math.max(0.0F, (Float)((Map)cacheProperty.getCache("aim_inaccuracy")).get(inaccuracyType));
                            //(Float)((Map)cacheProperty.getCache("aim_inaccuracy")).get(inaccuracyType)
                        }

                        Pair<Integer, Boolean> silence = (Pair)cacheProperty.getCache("silence");
                        int soundDistance = (Integer)silence.first();
                        boolean useSilenceSound = (Boolean)silence.right();
                        float speed = (Float)cacheProperty.getCache("ammo_speed");
                        float finalSpeed = Mth.clamp(speed / 20.0F, 0.0F, Float.MAX_VALUE);
                        int bulletAmount = Math.max(bulletData.getBulletAmount(), 1);
                        int cycles = fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1;
                        long period = fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1L;
                        boolean consumeAmmo = IGunOperator.fromLivingEntity(shooter).consumesAmmoOrNot();
                        float finalInaccuracy = inaccuracy;
                        CycleTaskHelper.addCycleTask(() -> {
                            if (shooter.isDeadOrDying()) {
                                return false;
                            }

                                boolean fire = !MinecraftForge.EVENT_BUS.post(new GunFireEvent(shooter, gunItem, LogicalSide.SERVER));
                                if (fire) {
                                    NetworkHandler.sendToTrackingEntity(new ServerMessageGunFire(shooter.getId(), gunItem), shooter);
                                    if (consumeAmmo) {
                                        this.reduceAmmo(gunItem, gun);
                                    }

                                    Level world = shooter.level();

                                    for(int i = 0; i < bulletAmount; ++i) {
                                        collectiveShots++;
                                        this.doSpawnBulletEntity(world, shooter, gunItem, (Float)this.getXRot(), (Float)this.getYRot(), finalSpeed, finalInaccuracy, ammoId, gunId, gunData, bulletData);
                                    }

                                    if (soundDistance > 0) {
                                        String soundId = useSilenceSound ? SoundManager.SILENCE_3P_SOUND : SoundManager.SHOOT_3P_SOUND;
                                        SoundManager.sendSoundToNearby(shooter, soundDistance, gunId, soundId, 0.8F, 0.9F + shooter.getRandom().nextFloat() * 0.125F);
                                    }
                                }

                                return true;
                        }, period, cycles);
                    }
                }
            }
        }
    }



    protected void reduceAmmo(ItemStack currentGunItem, ModernKineticGunItem gunitem) {
        Bolt boltType = (Bolt)TimelessAPI.getCommonGunIndex(gunitem.getGunId(currentGunItem)).map((index) -> index.getGunData().getBolt()).orElse((Bolt) null);
        if (boltType != null) {
            if (boltType == Bolt.MANUAL_ACTION) {
                gunitem.setBulletInBarrel(currentGunItem, false);
            } else if (boltType == Bolt.CLOSED_BOLT) {
                if (gunitem.getCurrentAmmoCount(currentGunItem) > 0) {
                    gunitem.reduceCurrentAmmoCount(currentGunItem);
                } else {
                    gunitem.setBulletInBarrel(currentGunItem, false);
                }
            } else {
                gunitem.reduceCurrentAmmoCount(currentGunItem);
            }

        }
    }
    public void initialGunOperateData() {
        IGunOperator.fromLivingEntity(this).initialData();
    }

    
    private final LivingEntity tacz$shooter = (LivingEntity)this;
    
    private final ShooterDataHolder tacz$data = new ShooterDataHolder();
    
    private final LivingEntityDrawGun tacz$draw;
    
    private final LivingEntityAim tacz$aim;
    
    private final LivingEntityCrawl tacz$crawl;
    
    private final LivingEntityAmmoCheck tacz$ammoCheck;
    
    private final LivingEntityFireSelect tacz$fireSelect;
    
    private final LivingEntityMelee tacz$melee;
    
    private final LivingEntityShoot tacz$shoot;
    
    private final LivingEntityBolt tacz$bolt;
    
    private final LivingEntityReload tacz$reload;
    
    private final LivingEntitySpeedModifier tacz$speed;
    private boolean drawn = false;
    
    public long getSynShootCoolDown() {
        return (Long) ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public long getSynMeleeCoolDown() {
        return (Long)ModSyncedEntityData.MELEE_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    
    public long getSynDrawCoolDown() {
        return (Long)ModSyncedEntityData.DRAW_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    
    public long getSynBoltCoolDown() {
        return (Long)ModSyncedEntityData.BOLT_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    
    public ReloadState getSynReloadState() {
        return (ReloadState)ModSyncedEntityData.RELOAD_STATE_KEY.getValue(this.tacz$shooter);
    }

    
    public float getSynAimingProgress() {
        return (Float)ModSyncedEntityData.AIMING_PROGRESS_KEY.getValue(this.tacz$shooter);
    }

    
    public float getSynSprintTime() {
        return (Float)ModSyncedEntityData.SPRINT_TIME_KEY.getValue(this.tacz$shooter);
    }

    
    public boolean getSynIsAiming() {
        return (Boolean)ModSyncedEntityData.IS_AIMING_KEY.getValue(this.tacz$shooter);
    }

    
    public void initialData() {
        this.tacz$data.initialData();
        AttachmentPropertyManager.postChangeEvent(this.tacz$shooter, this.tacz$shooter.getMainHandItem());
    }

    
    public void draw(Supplier<ItemStack> gunItemSupplier) {
        this.tacz$draw.draw(gunItemSupplier);
        this.drawn = true;
    }

    
    public void bolt() {
        this.tacz$bolt.bolt();
    }

    
    public void reload() {
        this.tacz$reload.reload();
        this.isReloading = true;
        //this.triggerAnim("controller", "reload_upper");
    }

    public void melee() {
        this.tacz$melee.melee();
    }

    
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.tacz$shoot.shoot(pitch, yaw);
    }

    
    public boolean needCheckAmmo() {
        return false;
    }

    
    public boolean consumesAmmoOrNot() {
        return this.tacz$ammoCheck.consumesAmmoOrNot();
    }

    
    public void aim(boolean isAim) {
        this.tacz$aim.aim(isAim);
    }

    public void crawl(boolean isCrawl) {
        this.tacz$crawl.crawl(isCrawl);
    }

    public void updateCacheProperty(AttachmentCacheProperty cacheProperty) {
        this.tacz$data.cacheProperty = cacheProperty;
    }

    @Nullable
    public AttachmentCacheProperty getCacheProperty() {
        return this.tacz$data.cacheProperty;
    }

    
    public void fireSelect() {
        this.tacz$fireSelect.fireSelect();
    }

    
    public void zoom() {
        this.tacz$aim.zoom();
    }
    private void onTickServerSide() {
        if (!this.level().isClientSide()) {
            if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
                if(!drawn) {
                    this.draw(this::getMainHandItem);
                }
                ItemStack gunItem = this.getMainHandItem();
                ResourceLocation gunId = gun.getGunId(gunItem);
                IGun iGun = IGun.getIGunOrNull(gunItem);
                LivingEntity shooter = this;
                if (iGun != null) {
                    Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                    if (!gunIndexOptional.isEmpty()) {
                        CommonGunIndex gunIndex = (CommonGunIndex) gunIndexOptional.get();
                        BulletData bulletData = gunIndex.getBulletData();
                        GunData gunData = gunIndex.getGunData();
                        AttachmentCacheProperty property = new AttachmentCacheProperty();
                        property.eval(this.getMainHandItem(), gunData);
                        updateCacheProperty(property);
                    }
                }

            }
            ReloadState reloadState = this.tacz$reload.tickReloadState();
            this.tacz$aim.tickAimingProgress();
            this.tacz$aim.tickSprint();
            this.tacz$crawl.tickCrawling();
            this.tacz$bolt.tickBolt();
            this.tacz$melee.scheduleTickMelee();
            this.tacz$speed.updateSpeedModifier();
            ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$shoot.getShootCoolDown());
            ModSyncedEntityData.MELEE_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$melee.getMeleeCoolDown());
            ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$draw.getDrawCoolDown());
            ModSyncedEntityData.BOLT_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$data.boltCoolDown);
            ModSyncedEntityData.RELOAD_STATE_KEY.setValue(this.tacz$shooter, reloadState);
            ModSyncedEntityData.AIMING_PROGRESS_KEY.setValue(this.tacz$shooter, this.tacz$data.aimingProgress);
            ModSyncedEntityData.IS_AIMING_KEY.setValue(this.tacz$shooter, this.tacz$data.isAiming);
            ModSyncedEntityData.SPRINT_TIME_KEY.setValue(this.tacz$shooter, this.tacz$data.sprintTimeS);
        }
    }

}
