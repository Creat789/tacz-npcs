package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.Config;
import com.tacz.guns.item.ModernKineticGunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class DutyEntity extends AbstractScavEntity {
    public static final EntityType<DutyEntity> DUTY;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;
    private boolean angry = false;
    static {
        DUTY = EntityType.Builder.of(DutyEntity::new, MobCategory.MISC).sized(0.65f, 1.95f).build("duty");
    }

    protected DutyEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        if(this.getServer() != null) {
            ObjectArrayList<ItemStack> stacks = this.getServer().getLootData().getLootTable(new ResourceLocation(MODID, "duty")).getRandomItems(new LootParams.Builder(this.getServer().overworld()).create(LootContextParamSet.builder().build()));
            stacks.forEach((stack) -> {
                inventory.addItem(stack);
                if(stack.getItem() instanceof ModernKineticGunItem) {
                    if(ModList.get().isLoaded("taczdurability")) {
                        stack.getOrCreateTag().putInt("Durability", RandomSource.create().nextInt(Config.DURABILITYFROM.get(), Config.DURABILITYTO.get()));
                    }
                }
            });
        }
        for(int i = 0; i < this.inventory.getContainerSize() - 1; i++) {
            if(inventory.getItem(i).getItem() instanceof PatchItem r) {
                this.setCustomName(Component.literal(r.rank.toString() + " " + this.getName().getString()));
                inventory.getItem(i).getOrCreateTag().putString("type","duty");
                inventory.getItem(i).setHoverName(Component.literal( r.rank.toString() + " Duty Patch"));
                switch (r.rank) {
                    case ROOKIE -> {

                    }
                    case EXPERIENCED -> {

                    }
                    case EXPERT -> {

                    }
                    case VETERAN -> {

                    }
                }
            }
        }
    }

    @Override
    public boolean allowInventory(Player player) {
        return lastHurtByPlayer != player;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof DutyEntity) {
            return false;
        }
        this.angry = true;

        return super.hurt(pSource, pAmount);
    }
    @Deprecated
    public void alertOthers() {
        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0D, d0);
        this.level().getEntitiesOfClass(DutyEntity.class, aabb, EntitySelector.NO_SPECTATORS).stream().filter((p_34463_) -> {
            return p_34463_ != this;
        }).filter((p_289465_) -> {
            return p_289465_.getTarget() == null;
        }).forEach((p_289464_) -> {
            p_289464_.setTarget(this.getTarget());
        });
    }
    @Override
    public BrainActivityGroup<? extends AbstractScavEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                new Panic<>().panicFor((e, d) -> RandomSource.create().nextInt(20, 30)).setRadius(3).stopIf((e)->this.getTarget() != null && !BehaviorUtils.canSee(this, this.getTarget())).whenStarting((e)-> {panic = true; paniccooldown = RandomSource.create().nextInt(10, 20);}).runFor((e)-> 20),
                new TargetOrRetaliate<DutyEntity>().isAllyIf((e, l) -> l instanceof DutyEntity).attackablePredicate(l -> l != null && this.hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999),
                (new AvoidEntity<>()).noCloserThan(16).speedModifier(1.0f).avoiding((entity) -> entity instanceof Player).startCondition((e) -> this.tacz$data.reloadStateType.isReloading()).whenStarting((e)-> this.isAvoiding = true).whenStopping((e) -> this.isAvoiding = false),
                (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextIntBetweenInclusive(40, 300)).stopIf((e)-> this.getTarget() == null),
                new MoveToWalkTarget<>()});
    }
    @Override
    public List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<AbstractScavEntity>().setPredicate((p, e) -> e.attackers.contains(p)),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<AbstractScavEntity>()
                        .setPredicate((target, entity) ->
                                target instanceof BanditEntity ||
                                        (target instanceof Monster) ||
                                        target.getType().getCategory() == MobCategory.MONSTER));
    }

    @Override
    public void tick() {
        if(this.getTarget() !=null && this.angry) {
            angry = false;
        }
        super.tick();
    }

    public void setTarget(@Nullable LivingEntity pLivingEntity) {
        if (this.getTarget() == null && pLivingEntity != null) {
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }

        if (pLivingEntity instanceof Player) {
            this.setLastHurtByPlayer((Player)pLivingEntity);
        }

        super.setTarget(pLivingEntity);
    }
}
