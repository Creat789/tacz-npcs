package com.corrinedev.tacznpcs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NPCS.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOWNAMETAGS = BUILDER.comment("NPCs show nametags?").define("nametags", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();
}
