package dev.shyrik.modularitemframe.common.screenhandler.filter;

import dev.shyrik.modularitemframe.api.util.GhostSlot;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class FilterUpgradeScreenHandler extends GhostInventoryScreenHandler {

    private static final int SLOTS_PER_ROW = 9;
    private static final int INV_ROWS = 3;

    private final SimpleInventory inv = new SimpleInventory(9);
    private final ItemStack filterStack;

    public FilterUpgradeScreenHandler(int containerId, Inventory playerInventory, ItemStack filter) {
        super(ScreenHandlerType.GENERIC_9X1, containerId);

        filterStack = filter;
        ItemFilterUpgradeItem.readTags(filterStack, inv);

        for (int col = 0; col < SLOTS_PER_ROW; ++col) {
            addSlot(new GhostSlot(inv, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < INV_ROWS; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                addSlot(new Slot(playerInventory, col + row * SLOTS_PER_ROW + SLOTS_PER_ROW, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < SLOTS_PER_ROW; ++col) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void sendContentUpdates() {
        ItemFilterUpgradeItem.writeTags(filterStack, inv);

        super.sendContentUpdates();
    }
}
