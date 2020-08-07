package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class FrameCrafting extends CraftingInventory {

    private final int length;
    private final ScreenHandler eventHandler;
    private final FixedItemInv parent;

    public FrameCrafting(ScreenHandler eventHandler, FixedItemInv parent, int width, int height) {
        super(eventHandler, width, height);
        int k = width * height;

        assert (k == parent.getSlotCount());

        this.parent = parent;
        this.length = k;
        this.eventHandler = eventHandler;
    }

    @Override
    public int size() {
        return this.length;
    }

    public ItemStack getStack(int index) {
        return index >= size() ? ItemStack.EMPTY : parent.getInvStack(index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        parent.setInvStack(index, stack, Simulation.ACTION);
        markDirty();
    }

    @Override
    public void markDirty() {
        eventHandler.onContentChanged(this);
    }

    @Override
    public void clear() {
        // inventory can't clear the tile container
    }
}
