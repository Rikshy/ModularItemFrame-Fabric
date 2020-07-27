package dev.shyrik.modularitemframe.common.screenhandler;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class FrameCrafting extends CraftingInventory {

    private final int length;
    private final ScreenHandler eventHandler;
    private final Inventory parent;
    private boolean doNotCallUpdates;

    public FrameCrafting(ScreenHandler eventHandler, Inventory parent, int width, int height) {
        super(eventHandler, width, height);
        int k = width * height;

        assert (k == parent.size());

        this.parent = parent;
        this.length = k;
        this.eventHandler = eventHandler;
        this.doNotCallUpdates = false;
    }

    @Override
    public int size() {
        return this.length;
    }

    public ItemStack getStack(int index) {
        return index >= size() ? ItemStack.EMPTY : parent.getStack(index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        parent.setStack(index, stack);
        onCraftMatrixChanged();
    }

    @Override
    public void markDirty() {
        onCraftMatrixChanged();
    }

    @Override
    public void clear() {
        // inventory can't clear the tile container
    }

    /**
     * If set to true no eventhandler.onCraftMatrixChanged calls will be made.
     * This is used to prevent recipe check when changing the item slots when something is crafted
     * (since each slot with an item is reduced by 1, it changes -> callback)
     */
    public void setDoNotCallUpdates(boolean doNotCallUpdates) {
        this.doNotCallUpdates = doNotCallUpdates;
    }

    public void onCraftMatrixChanged() {
        if (!doNotCallUpdates) {
            this.eventHandler.onContentChanged(this);
        }
    }
}