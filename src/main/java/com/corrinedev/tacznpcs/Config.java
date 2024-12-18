package com.corrinedev.tacznpcs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NPCS.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue NPCDAMAGE = BUILDER.comment("Damage multiplier for tacz npcs").defineInRange("base multiplier", 0.50, 0, Double.MAX_VALUE);
    //TACZ DURABILITY
    public static final ForgeConfigSpec.IntValue DURABILITYFROM = BUILDER.comment("The lowest durability value for guns to spawn with from TACZ Durability").defineInRange("from", 200, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue DURABILITYTO = BUILDER.comment("The highest durability value for guns to spawn with from TACZ Durability").defineInRange("to", 800, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();
}
