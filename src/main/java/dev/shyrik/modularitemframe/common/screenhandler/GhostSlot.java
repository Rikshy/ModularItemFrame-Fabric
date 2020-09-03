package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class GhostSlot  extends FixedSlot {
    public GhostSlot(BaseScreenHandler handler, FixedItemInv inventoryIn, int slot, int xPosition, int yPosition) {
        super(handler, inventoryIn, slot, xPosition, yPosition);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        return stack;
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
        ItemStack copy = stack.copy();
        if (!copy.isEmpty())
            copy.setCount(1);
        super.setStack(copy);
    }
}
