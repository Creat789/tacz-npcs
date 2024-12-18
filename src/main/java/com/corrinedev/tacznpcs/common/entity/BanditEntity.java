package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.Config;
import com.tacz.guns.item.ModernKineticGunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.fml.ModList;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class BanditEntity extends AbstractScavEntity {
    public static final EntityType<BanditEntity> BANDIT;
    static {
        BANDIT = EntityType.Builder.of(BanditEntity::new, MobCategory.MONSTER).sized(0.65f, 1.95f).build("bandit");
    }

    protected BanditEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        if(this.getServer() != null) {
            ObjectArrayList<ItemStack> stacks = this.getServer().getLootData().getLootTable(new ResourceLocation(MODID, "bandit")).getRandomItems(new LootParams.Builder(this.getServer().overworld()).create(LootContextParamSet.builder().build()));
            stacks.forEach((stack) -> {
                if(stack.getItem() instanceof ModernKineticGunItem) {
                    if(ModList.get().isLoaded("gundurability")) {
                        stack.getOrCreateTag().putInt("Durability", RandomSource.create().nextInt(Config.DURABILITYFROM.get(), Config.DURABILITYTO.get()));
                    }
                }
                if(stack.getMaxDamage() != 0) {
                    stack.setDamageValue(RandomSource.create().nextInt((int)stack.getMaxDamage() / 2, stack.getMaxDamage()));
                }
                inventory.addItem(stack);
            });
        }
        for(int i = 0; i < this.inventory.getContainerSize() - 1; i++) {
            if(inventory.getItem(i).getItem() instanceof PatchItem r) {
                this.setCustomName(Component.literal(r.rank.toString() + " " + this.getName().getString()));
                inventory.getItem(i).getOrCreateTag().putString("type","bandit");
                inventory.getItem(i).setHoverName(Component.literal( r.rank.toString() + " Bandit Patch"));
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
        return this.deadAsContainer;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof BanditEntity) {
            return false;
        }

        return super.hurt(pSource, pAmount);
    }
    @Override
    public BrainActivityGroup<? extends AbstractScavEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                new Panic<>().panicFor((e, d) -> RandomSource.create().nextInt(20, 30)).setRadius(3).stopIf((e)->this.getTarget() != null && !BehaviorUtils.canSee(this, this.getTarget())).whenStarting((e)-> {panic = true; paniccooldown = RandomSource.create().nextInt(10, 20);}).runFor((e)-> 20),
                new TargetOrRetaliate<BanditEntity>().isAllyIf((e, l) -> l instanceof BanditEntity).attackablePredicate(l -> l != null && this.hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999),
                new SetRandomWalkTarget<>().setRadius(16).speedModifier(0.8F).startCondition((e)-> !this.firing),
                (new AvoidEntity<>()).noCloserThan(16).speedModifier(1.0f).avoiding((entity) -> entity instanceof Player).startCondition((e) -> this.tacz$data.reloadStateType.isReloading()).whenStarting((e)-> this.isAvoiding = true).whenStopping((e) -> this.isAvoiding = false),
                (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextIntBetweenInclusive(40, 300)).stopIf((e)-> this.getTarget() == null),
                new MoveToWalkTarget<>()});
    }
    @Override
    public List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>(),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<AbstractScavEntity>()
                        .setPredicate((target, entity) ->
                                target instanceof Player ||
                                        target instanceof IronGolem ||
                                        target instanceof DutyEntity ||
                                        target instanceof Wolf ||
                                        (target instanceof AbstractVillager)));
    }
}
