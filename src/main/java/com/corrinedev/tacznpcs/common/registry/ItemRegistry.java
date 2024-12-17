package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import com.corrinedev.tacznpcs.common.entity.Rank;
import com.corrinedev.tacznpcs.common.entity.PatchItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<SpawnEggItem> DUTYSPAWN = ITEMS.register("duty_spawn_egg", () -> new SpawnEggItem(DutyEntity.DUTY, 2, 1, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> BANDITSPAWN = ITEMS.register("bandit_spawn_egg", () -> new SpawnEggItem(BanditEntity.BANDIT, 2, 1, new Item.Properties()));
    public static final RegistryObject<PatchItem> ROOKIE = ITEMS.register("rookie", () -> new PatchItem(Rank.ROOKIE));
    public static final RegistryObject<PatchItem> EXPERIENCED = ITEMS.register("experienced", () -> new PatchItem(Rank.EXPERIENCED));
    public static final RegistryObject<PatchItem> VETERAN = ITEMS.register("veteran", () -> new PatchItem(Rank.VETERAN));
    public static final RegistryObject<PatchItem> EXPERT = ITEMS.register("expert", () -> new PatchItem(Rank.EXPERT));
}
