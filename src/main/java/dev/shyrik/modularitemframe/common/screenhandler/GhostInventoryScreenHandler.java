package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public abstract class GhostInventoryScreenHandler extends BaseScreenHandler {

    protected static final int SLOTS_PER_ROW = 9;
    private static final int INV_ROWS = 3;

    private static final int PLAYER_SLOTS = 36;

    protected GhostInventoryScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventory(Inventory playerInventory) {
        FixedItemInv playerInv = new FixedInventoryVanillaWrapper(playerInventory);

        for (int row = 0; row < INV_ROWS; ++row) {
            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                addSlot(new FixedSlot(this, playerInv, col + row * SLOTS_PER_ROW + SLOTS_PER_ROW, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < SLOTS_PER_ROW; ++col) {
            addSlot(new FixedSlot(this, playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack onSlotClick(int slotId, int dragType_or_button, SlotActionType clickType, PlayerEntity player) {
        Slot slot = slotId < 0 ? null : getSlot(slotId);
//        if (player.world.isClient)
//            return ItemStack.EMPTY;

        if (slot instanceof GhostSlot) {
            //return SlotHelper.ghostSlotClick(slot, dragType_or_button, clickType, player);
            if (clickType == SlotActionType.PICKUP || clickType == SlotActionType.SWAP) {
                ItemStack dropping = player.inventory.getCursorStack();

                if (dropping.getCount() > 0) {
                    slot.setStack(dropping);
                } else if (slot.getStack().getCount() > 0) {
                    slot.setStack(ItemStack.EMPTY);
                }

                slot.markDirty();
            }

            return ItemStack.EMPTY;
        }

        return super.onSlotClick(slotId, dragType_or_button, clickType, player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        if (player.world.isClient)
            return ItemStack.EMPTY;

        //return SlotHelper.transferStackInSlot(slots, player, slotIndex);
        Slot slot = getSlot(slotIndex);
        if (slot == null || !slot.hasStack() || slot instanceof GhostSlot) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        if (!insertItem(stack, slots.size() - PLAYER_SLOTS, slots.size(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == stackCopy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return stackCopy;
    }
}
