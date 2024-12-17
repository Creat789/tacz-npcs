package com.corrinedev.tacznpcs.common.entity;

import net.minecraft.world.item.Item;

public class PatchItem extends Item {
    public Rank rank;
    public PatchItem(Rank rank) {
        super(new Properties());
        this.rank = rank;
    }
}
