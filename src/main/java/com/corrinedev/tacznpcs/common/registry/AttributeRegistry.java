package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttributeRegistry {
    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.BANDIT.get(), BanditEntity.createLivingAttributes().build());
        event.put(EntityTypeRegistry.DUTY.get(), DutyEntity.createLivingAttributes().build());
    }
}
