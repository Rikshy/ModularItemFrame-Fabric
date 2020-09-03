package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.FixedItemInv;

public class GhostCraftingResultSlot extends GhostSlot {
    public GhostCraftingResultSlot(BaseScreenHandler handler, FixedItemInv inventoryIn, int slot, int xPosition, int yPosition) {
        super(handler, inventoryIn, slot, xPosition, yPosition);
    }

    @Override
    public boolean doDrawHoveringEffect() {
        return false;
    }
}
