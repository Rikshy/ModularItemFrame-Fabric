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
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        return false;
    }

    @Override
    public void setStack(ItemStack stack) {
        ItemStack stack2 = stack.copy();
        if (!stack2.isEmpty())
            stack2.setCount(1);
        super.setStack(stack2);
    }
}
