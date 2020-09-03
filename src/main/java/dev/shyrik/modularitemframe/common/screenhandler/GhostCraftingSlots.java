package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.util.FixedInventoryWrapper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class GhostCraftingSlots extends GhostSlot {

    private final Inventory wrappedInventory;

    public GhostCraftingSlots(BaseScreenHandler handler, FixedItemInv inv, int slot, int xPosition, int yPosition) {
        super(handler, inv, slot, xPosition, yPosition);
        wrappedInventory = new FixedInventoryWrapper(inv);
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        parent.onContentChanged(wrappedInventory);
    }
}
