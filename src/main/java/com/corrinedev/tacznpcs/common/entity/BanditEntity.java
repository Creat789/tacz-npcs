package com.corrinedev.tacznpcs.common.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.marblednull.mcore.datagen.loot.ModBlockLootTables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import java.util.List;
import java.util.Objects;

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
            System.out.println(stacks);
            stacks.forEach((stack) -> {
                inventory.addItem(stack);
            });
        }

    }

    @Override
    public boolean allowInventory(Player player) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof BanditEntity) {
            return false;
        }
        //System.out.println(this.getServer());

        return super.hurt(pSource, pAmount);
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
