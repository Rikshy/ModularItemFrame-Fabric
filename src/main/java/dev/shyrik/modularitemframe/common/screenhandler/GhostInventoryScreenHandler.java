package dev.shyrik.modularitemframe.common.screenhandler;

import dev.shyrik.modularitemframe.api.util.GhostSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public abstract class GhostInventoryScreenHandler extends ScreenHandler {
    protected GhostInventoryScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public ItemStack onSlotClick(int slotId, int dragType_or_button, SlotActionType clickType, PlayerEntity player) {
        Slot slot = slotId < 0 ? null : getSlot(slotId);
        if (slot instanceof GhostSlot) {
            //if (clickType == SlotActionType.PICKUP || clickType == SlotActionType.PICKUP_ALL || clickType == SlotActionType.SWAP)
            {
                ItemStack dropping = player.inventory.getCursorStack();

                if (dropping.getCount() > 0) {
                    ItemStack copy = dropping.copy();
                    copy.setCount(1);
                    slot.setStack(copy);
                } else if (slot.getStack().getCount() > 0) {
                    slot.setStack(ItemStack.EMPTY);
                }

                return slot.getStack().copy();
            }

            //return ItemStack.EMPTY;
        }
        return super.onSlotClick(slotId, dragType_or_button, clickType, player);
    }

    @Override
    public final ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        Slot slot = getSlot(slotIndex);
        if (slot == null || !slot.hasStack() || slot instanceof GhostSlot) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex < 9) {
            return ItemStack.EMPTY;
        } else if (slotIndex < 18) {
            startIndex = 18;
            endIndex = 18 + 27 + 9;
        } else {
            startIndex = 9;
            endIndex = 18;
        }

        if (!insertItem(stack, startIndex, endIndex, false)) {
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
