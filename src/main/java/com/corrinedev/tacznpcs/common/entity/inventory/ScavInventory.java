package com.corrinedev.tacznpcs.common.entity.inventory;

import com.corrinedev.tacznpcs.common.entity.DeathEntity;
import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class ScavInventory extends ChestMenu {
    public AbstractScavEntity attachedEntity;
    public DeathEntity attachedDeath;
    public ScavInventory(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows, AbstractScavEntity entity) {
        super(pType, pContainerId, pPlayerInventory, pContainer, pRows);
        this.attachedEntity = entity;
        this.attachedDeath = null;
    }
    public ScavInventory(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows, DeathEntity entity) {
        super(pType, pContainerId, pPlayerInventory, pContainer, pRows);
        this.attachedEntity = null;
        this.attachedDeath = entity;
    }
    public static ScavInventory generate(int pContainerId, Inventory pPlayerInventory, Container pContainer, AbstractScavEntity entity) {
        return new ScavInventory(MenuType.GENERIC_9x3, pContainerId, pPlayerInventory, pContainer, 3, entity);
    }

    public static ScavInventory generate(int pContainerId, @NotNull Inventory pPlayerInventory, SimpleContainer inventory, DeathEntity abstractDeathEntity) {
        return new ScavInventory(MenuType.GENERIC_9x3, pContainerId, pPlayerInventory, inventory, 3, abstractDeathEntity);
    }


    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if(this.attachedEntity != null) {
            this.attachedEntity.resetSlots();
        } else {
            this.attachedDeath.resetSlots();
        }
    }
}
