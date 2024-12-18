package com.corrinedev.tacznpcs.common;

import com.corrinedev.tacznpcs.Config;
import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Events {
    @SubscribeEvent
    public static void onHitByScav(EntityHurtByGunEvent.Pre event) {
        if(event.getAttacker() instanceof AbstractScavEntity && event.getHurtEntity() instanceof Player) {
            event.setBaseAmount((float) (event.getBaseAmount() * Config.NPCDAMAGE.get()));
        }
    }
}
