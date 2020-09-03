package dev.shyrik.modularitemframe.common.screenhandler.filter;

import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import dev.shyrik.modularitemframe.common.screenhandler.FixedSlot;
import dev.shyrik.modularitemframe.common.screenhandler.GhostSlot;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;

public class FilterUpgradeScreenHandler extends GhostInventoryScreenHandler {

    private static final int SLOTS_PER_ROW = 9;

    private final DirectFixedItemInv inv;
    private final ItemStack filterStack;

    public FilterUpgradeScreenHandler(int containerId, Inventory playerInventory, ItemStack filter) {
        super(ScreenHandlerType.GENERIC_9X1, containerId);

        filterStack = filter;
        inv = ItemFilterUpgradeItem.readInvTag(filterStack.getOrCreateTag());

        for (int col = 0; col < SLOTS_PER_ROW; ++col) {
            addSlot(new GhostSlot(this, inv, col, 8 + col * 18, 18));
        }

        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotChanged(FixedSlot slot) {
        if (slot instanceof GhostSlot) {
            ItemFilterUpgradeItem.writeInvTag(filterStack.getOrCreateTag(), inv);
            inv.markDirty();
        }
    }
}
