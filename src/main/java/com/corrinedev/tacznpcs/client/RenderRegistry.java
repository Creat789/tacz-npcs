package com.corrinedev.tacznpcs.client;

import com.corrinedev.tacznpcs.client.renderer.BanditRenderer;
import com.corrinedev.tacznpcs.client.renderer.DutyRenderer;
import com.corrinedev.tacznpcs.client.renderer.ScavRenderer;
import com.corrinedev.tacznpcs.common.registry.EntityTypeRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.corrinedev.tacznpcs.NPCS.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {
    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityTypeRegistry.BANDIT.get(), BanditRenderer::new);
        EntityRenderers.register(EntityTypeRegistry.DUTY.get(), DutyRenderer::new);
    }
}
