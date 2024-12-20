package com.corrinedev.tacznpcs.common.entity.behavior;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.tacz.guns.api.entity.ShootResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.BowItem;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.util.BrainUtils;

public class TaczShootAttack<E extends AbstractScavEntity & RangedAttackMob> extends AnimatableRangedAttack<E> {
    public TaczShootAttack() {
        super(0);
        this.attackRadius = 32;
        this.attackIntervalSupplier = (entity) -> RandomSource.create().nextInt(0,2);
    }
    public TaczShootAttack(int attackRadius) {
        super(0);
        this.attackRadius = attackRadius;
        this.attackIntervalSupplier = (entity) -> RandomSource.create().nextInt(0,2);;
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);
        return BrainUtils.canSee(entity, this.target);
    }

    @Override
    protected void start(E entity) {
        assert this.target != null;
            BehaviorUtils.lookAtEntity(entity, this.target);
            if(BehaviorUtils.entityIsVisible(entity.getBrain(), this.target)) {
                System.out.println(target.hasLineOfSight(entity));
                if (entity.hasLineOfSight(target)) {
                    ShootResult result = entity.shoot(() -> entity.getViewXRot(1f), () -> entity.getViewYRot(1f));
                    if (result == ShootResult.SUCCESS) {
                        entity.firing = true;
                        entity.collectiveShots++;
                        entity.rangedCooldown = entity.getStateRangedCooldown();
                    } else if (result == ShootResult.NEED_BOLT) {
                        entity.bolt();
                    } else if (result == ShootResult.NO_AMMO) {
                        entity.triggerAnim("layered", "reload_upper");
                        entity.isReloading = true;
                        entity.reload();
                    }
                    BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, (Integer) this.attackIntervalSupplier.apply(entity));
                }
            }
    }

    @Override
    protected void stop(E entity) {
        //super.stop(entity);
    }

    @Override
    protected void doDelayedAction(E entity) {

    }

}
