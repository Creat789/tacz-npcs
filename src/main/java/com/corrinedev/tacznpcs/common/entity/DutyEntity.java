package com.corrinedev.tacznpcs.common.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

import java.util.List;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class DutyEntity extends AbstractScavEntity {
    public static final EntityType<DutyEntity> DUTY;

    static {
        DUTY = EntityType.Builder.of(DutyEntity::new, MobCategory.MISC).sized(0.65f, 1.95f).build("duty");
    }

    protected DutyEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        if(this.getServer() != null) {
            ObjectArrayList<ItemStack> stacks = this.getServer().getLootData().getLootTable(new ResourceLocation(MODID, "duty")).getRandomItems(new LootParams.Builder(this.getServer().overworld()).create(LootContextParamSet.builder().build()));
            System.out.println(stacks);
            stacks.forEach((stack) -> {
                inventory.addItem(stack);
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
        return super.hurt(pSource, pAmount);
    }

    @Override
    public List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors() {
        return ObjectArrayList.of(
                //new NearbyPlayersSensor<>(),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<AbstractScavEntity>()
                        .setPredicate((target, entity) ->
                                target instanceof BanditEntity ||
                                        (target instanceof Monster) ||
                                        target.getType().getCategory() == MobCategory.MONSTER));
    }
}
