package com.corrinedev.tacznpcs.common.entity.behavior;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.BowItem;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableRangedAttack;
import net.tslat.smartbrainlib.util.BrainUtils;

public class TaczShootAttack<E extends AbstractScavEntity & RangedAttackMob> extends AnimatableRangedAttack<E> {
    public TaczShootAttack(int delayTicks) {
        super(delayTicks);
        this.attackRadius = 32;
        this.attackIntervalSupplier = (entity) -> entity.level().getDifficulty() == Difficulty.HARD ? delayTicks : delayTicks * 2;
    }
    public TaczShootAttack(int delayTicks, int attackRadius) {
        super(delayTicks);
        this.attackRadius = attackRadius;
        this.attackIntervalSupplier = (entity) -> entity.level().getDifficulty() == Difficulty.HARD ? delayTicks : delayTicks * 2;
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);
        return BrainUtils.canSee(entity, this.target);
    }
    @Override
    protected void doDelayedAction(E entity) {
        if (this.target != null) {
            BehaviorUtils.lookAtEntity(entity, this.target);
            if (BrainUtils.canSee(entity, this.target)) {
                entity.firing = true;
                entity.shoot(()-> entity.getViewXRot(1f), ()-> entity.getViewYRot(1f));
                entity.rangedCooldown = entity.getStateRangedCooldown();
                BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, (Integer)this.attackIntervalSupplier.apply(entity));
            }
        }
    }
}
