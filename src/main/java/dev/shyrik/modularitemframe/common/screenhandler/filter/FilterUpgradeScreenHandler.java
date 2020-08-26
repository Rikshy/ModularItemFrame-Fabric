package dev.shyrik.modularitemframe.common.screenhandler.filter;

import dev.shyrik.modularitemframe.api.util.GhostSlot;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;

public class FilterUpgradeScreenHandler extends GhostInventoryScreenHandler {

    private static final int SLOTS_PER_ROW = 9;

    private final SimpleInventory inv;
    private final ItemStack filterStack;

    public FilterUpgradeScreenHandler(int containerId, Inventory playerInventory, ItemStack filter) {
        super(ScreenHandlerType.GENERIC_9X1, containerId, playerInventory);

        filterStack = filter;
        inv = new SimpleInventory(ItemFilterUpgradeItem.readInvTag(filterStack.getOrCreateTag()).toArray(new ItemStack[0]));

        for (int col = 0; col < SLOTS_PER_ROW; ++col) {
            addSlot(new GhostSlot(inv, col, 8 + col * 18, 18));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void sendContentUpdates() {
        ItemFilterUpgradeItem.writeInvTag(filterStack.getOrCreateTag(), inv);

        super.sendContentUpdates();
    }
}
