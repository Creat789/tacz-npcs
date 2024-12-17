package com.corrinedev.tacznpcs.client;

import com.corrinedev.tacznpcs.common.registry.ItemRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.corrinedev.tacznpcs.NPCS.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NPCSClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {


        event.enqueueWork(() -> {
            //REGISTER ITEM OVERRIDES
                ItemProperties.registerGeneric(
                    new ResourceLocation(MODID, "type"), (stack, level, living, id) -> {
                        return switch (stack.getOrCreateTag().getString("type")) {
                            case "bandit" -> 1;
                            case "duty" -> 2;
                            default -> 0;
                        };
                    });
            });
}
}
