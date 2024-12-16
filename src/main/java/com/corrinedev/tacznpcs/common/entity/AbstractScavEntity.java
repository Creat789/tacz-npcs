package com.corrinedev.tacznpcs.common.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.*;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class BanditEntity extends PathfinderMob implements GeoEntity, SmartBrainOwner<BanditEntity>, RangedAttackMob, IGunOperator {
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    public static final EntityType<BanditEntity> BANDIT;
    protected BanditEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        initialGunOperateData();

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
    static {
        BANDIT = EntityType.Builder.of(BanditEntity::new, MobCategory.MONSTER).sized(0.65f, 1.95f).build("bandit");
    }
    public static AttributeSupplier.Builder createLivingAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<>(this, "controller", 10, event ->
            {
                return event.setAndContinue(
                        // If moving, play the walking animation
                        event.isMoving() ? RawAnimation.begin().thenLoop("walk"):
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

    @Override
    public List<? extends ExtendedSensor<? extends BanditEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>(),
                new NearbyLivingEntitySensor<BanditEntity>()
                        .setPredicate((target, entity) ->
                                target instanceof Player ||
                                        target instanceof IronGolem ||
                                        target instanceof Wolf ||
                                        (target instanceof Turtle turtle && turtle.isBaby() && !turtle.isInWater())));
    }

    @Override
    public BrainActivityGroup<? extends BanditEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                new LookAtTarget<>().runFor((entity) -> entity.getRandom().nextIntBetweenInclusive(40, 300)),
                new MoveToWalkTarget<>()});
    }

    @Override
    public BrainActivityGroup<? extends BanditEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[]{
                new FirstApplicableBehaviour<>(new ExtendedBehaviour[]{new TargetOrRetaliate<>(),
                        new SetPlayerLookTarget<>(), new SetRandomLookTarget<>()}),
                new OneRandomBehaviour<>(new ExtendedBehaviour[]{(new SetRandomWalkTarget<>()).speedModifier(1.0F),
                        new Idle<>().runFor((entity) -> entity.getRandom().nextInt(30, 60))
                })});
    }

    @Override
    public BrainActivityGroup<? extends BanditEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new Behavior[]{
                new InvalidateAttackTarget<>(),
                new SetWalkTargetToAttackTarget<>(),
                new FirstApplicableBehaviour(new ExtendedBehaviour[]{(
                        new AnimatableRangedAttack(5).attackRadius(32)).whenStarting((entity) -> {this.setAggressive(true); System.out.println("STARTING RANGED ATTACK"); this.performRangedAttack((LivingEntity) entity, 5);}).whenStopping((entity) -> {this.setAggressive(false);})
                })});
    }
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        this.tickBrain(this);
    }

    @Override
    public void tick() {
        onTickServerSide();
        super.tick();
    }

    protected void doSpawnBulletEntity(Level world, LivingEntity shooter, ItemStack gunItem, float pitch, float yaw, float speed, float inaccuracy, ResourceLocation ammoId, ResourceLocation gunId, GunData gunData, BulletData bulletData) {
        EntityKineticBullet bullet = new EntityKineticBullet(world, shooter, gunItem, ammoId, gunId, true, gunData, bulletData);
        bullet.shootFromRotation(bullet, pitch, yaw, 0.0F, speed, inaccuracy);
        world.addFreshEntity(bullet);
    }
    @Override
    public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
        lookAt(pTarget, 10, 10);
        if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            System.out.println("Check 1");
            ItemStack gunItem = this.getMainHandItem();
            ResourceLocation gunId = gun.getGunId(gunItem);
            IGun iGun = IGun.getIGunOrNull(gunItem);
            LivingEntity shooter = this;
            if (iGun != null) {
                System.out.println("CHECK 2");
                Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                if (!gunIndexOptional.isEmpty()) {
                    System.out.println("CHECK 3");
                    CommonGunIndex gunIndex = (CommonGunIndex)gunIndexOptional.get();
                    BulletData bulletData = gunIndex.getBulletData();
                    GunData gunData = gunIndex.getGunData();
                    ResourceLocation ammoId = gunData.getAmmoId();
                    FireMode fireMode = iGun.getFireMode(gunItem);
                    AttachmentCacheProperty cacheProperty = this.getCacheProperty();
                    if (cacheProperty != null) {
                        System.out.println("CHECK 4");
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
                                        System.out.println("SPAWNED BULLET");
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
    }

    
    public void bolt() {
        this.tacz$bolt.bolt();
    }

    
    public void reload() {
        this.tacz$reload.reload();
    }

    public void melee() {
        this.tacz$melee.melee();
    }

    
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.tacz$shoot.shoot(pitch, yaw);
    }

    
    public boolean needCheckAmmo() {
        return this.tacz$ammoCheck.needCheckAmmo();
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
