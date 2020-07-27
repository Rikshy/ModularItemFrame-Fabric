package dev.shyrik.modularitemframe.api.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class GhostSlot  extends Slot {
    public GhostSlot(Inventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerIn) {
        return false;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxStackAmount(ItemStack stack) {
        return 1;
    }

    @Override
    public int getMaxStackAmount() {
        return 1;
    }
}
