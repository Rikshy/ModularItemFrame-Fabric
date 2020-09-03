package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.SingleItemSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class FixedSlot extends Slot {
    private static final Inventory DUMMY_INVENTORY = new SimpleInventory(0);

    protected final BaseScreenHandler parent;
    private final SingleItemSlot backingSlot;

    public FixedSlot(BaseScreenHandler handler, FixedItemInv inv, int slot, int x, int y) {
        super(DUMMY_INVENTORY, slot, x, y);
        backingSlot = inv.getSlot(slot);
        parent = handler;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return backingSlot.isValid(stack);
    }

    @Override
    public ItemStack getStack() {
        return backingSlot.get();
    }

    @Override
    public void setStack(ItemStack stack) {
        backingSlot.set(stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        parent.onSlotChanged(this);
    }

    @Override
    public int getMaxItemCount() {
        return backingSlot.getMaxAmount(ItemStack.EMPTY);
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return Math.min(getMaxItemCount(), stack.getMaxCount());
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return backingSlot.couldExtractAnything();
    }

    @Override
    public ItemStack takeStack(int amount) {
        return backingSlot.extract(amount);
    }

    @Override
    public boolean hasStack() {
        return !backingSlot.get().isEmpty();
    }

    @Override
    public boolean doDrawHoveringEffect() {
        return true;
    }
}
