package dev.shyrik.modularitemframe.api.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class SlotHelper {

    public static ItemStack ghostSlotClick(Slot slot, int mouseButton, SlotActionType clickTypeIn, PlayerEntity player) {
        ItemStack stack = ItemStack.EMPTY;
        ItemStack stackSlot = slot.getStack();
        ItemStack stackHeld = player.inventory.getMainHandStack();

        if (mouseButton == 0 || mouseButton == 1) {
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty()) {
                    fillGhostSlot(slot, stackHeld, mouseButton);
                }
            } else if (stackHeld.isEmpty()) {
                adjustGhostSlot(slot, mouseButton, clickTypeIn);
            } else if (slot.canInsert(stackHeld)) {
                if (ItemHelper.simpleAreItemsEqual(stackSlot, stackHeld)) {
                    adjustGhostSlot(slot, mouseButton, clickTypeIn);
                } else {
                    fillGhostSlot(slot, stackHeld, mouseButton);
                }
            }
        } else if (mouseButton == 5) {
            if (!slot.hasStack()) {
                fillGhostSlot(slot, stackHeld, mouseButton);
            }
        }
        return stack;
    }

    private static void adjustGhostSlot(Slot slot, int mouseButton, SlotActionType clickTypeIn) {
        ItemStack stackSlot = slot.getStack();
        int stackSize;
        if (clickTypeIn == SlotActionType.QUICK_MOVE) {
            stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() * 2;
        } else {
            stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
        }

        if (stackSize > slot.getMaxStackAmount()) {
            stackSize = slot.getMaxStackAmount();
        }

        stackSlot.setCount(stackSize);

        slot.setStack(stackSlot);
    }

    private static void fillGhostSlot(Slot slot, ItemStack stackHeld, int mouseButton) {
        if (stackHeld.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
            return;
        }

        int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
        if (stackSize > slot.getMaxStackAmount()) {
            stackSize = slot.getMaxStackAmount();
        }
        ItemStack phantomStack = stackHeld.copy();
        phantomStack.setCount(stackSize);

        slot.setStack(phantomStack);
    }

    public static ItemStack transferStackInSlot(List<Slot> inventorySlots, PlayerEntity player, int slotIndex) {
        Slot slot = inventorySlots.get(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        boolean fromCraftingSlot = slot instanceof CraftingResultSlot;

        int numSlots = inventorySlots.size();
        ItemStack stackInSlot = slot.getStack();
        ItemStack originalStack = stackInSlot.copy();

        if (!shiftItemStack(inventorySlots, stackInSlot, slotIndex, numSlots, fromCraftingSlot)) {
            return ItemStack.EMPTY;
        }

        slot.onSlotChange(stackInSlot, originalStack);
        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stackInSlot.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stackInSlot);
        return originalStack;
    }

    private static boolean shiftItemStack(List<Slot> inventorySlots, ItemStack stackInSlot, int slotIndex, int numSlots, boolean fromCraftingSlot) {
        if (isInPlayerInventory(slotIndex)) {
            if (shiftToMachineInventory(inventorySlots, stackInSlot, numSlots)) {
                return true;
            }

            if (isInPlayerHotbar(slotIndex)) {
                return shiftToPlayerInventoryNoHotbar(inventorySlots, stackInSlot);
            } else {
                return shiftToHotbar(inventorySlots, stackInSlot);
            }
        } else {
            if (fromCraftingSlot) {
                if (shiftToMachineInventory(inventorySlots, stackInSlot, numSlots)) {
                    return true;
                }
            }
            return shiftToPlayerInventory(inventorySlots, stackInSlot);
        }
    }
    private static final int PLAYER_INVENTORY_SIZE = 9 * 4;
    private static final int PLAYER_HOTBAR_SIZE = 9;

    private static boolean isInPlayerInventory(int slotIndex) {
        return slotIndex < PLAYER_INVENTORY_SIZE;
    }

    private static boolean isInPlayerHotbar(int slotIndex) {
        return isSlotInRange(slotIndex, PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE, PLAYER_INVENTORY_SIZE);
    }

    private static boolean shiftToPlayerInventory(List<Slot> inventorySlots, ItemStack stackInSlot) {
        int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;

        // try to merge with existing stacks, hotbar first
        boolean shifted = shiftItemStackToRangeMerge(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
        shifted |= shiftItemStackToRangeMerge(inventorySlots, stackInSlot, 0, playerHotbarStart);

        // shift to open slots, hotbar first
        shifted |= shiftItemStackToRangeOpenSlots(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
        shifted |= shiftItemStackToRangeOpenSlots(inventorySlots, stackInSlot, 0, playerHotbarStart);
        return shifted;
    }

    private static boolean shiftToPlayerInventoryNoHotbar(List<Slot> inventorySlots, ItemStack stackInSlot) {
        int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;
        return shiftItemStackToRange(inventorySlots, stackInSlot, 0, playerHotbarStart);
    }
    public static boolean isSlotInRange(int slotIndex, int start, int count) {
        return slotIndex >= start && slotIndex < start + count;
    }

    private static boolean shiftToHotbar(List<Slot> inventorySlots, ItemStack stackInSlot) {
        int playerHotbarStart = PLAYER_INVENTORY_SIZE - PLAYER_HOTBAR_SIZE;
        return shiftItemStackToRange(inventorySlots, stackInSlot, playerHotbarStart, PLAYER_HOTBAR_SIZE);
    }

    private static boolean shiftToMachineInventory(List<Slot> inventorySlots, ItemStack stackToShift, int numSlots) {
        boolean success = false;
        if (stackToShift.isStackable()) {
            success = shiftToMachineInventory(inventorySlots, stackToShift, numSlots, true);
        }
        if (!stackToShift.isEmpty()) {
            success |= shiftToMachineInventory(inventorySlots, stackToShift, numSlots, false);
        }
        return success;
    }

    // if mergeOnly = true, don't shift into empty slots.
    private static boolean shiftToMachineInventory(List<Slot> inventorySlots, ItemStack stackToShift, int numSlots, boolean mergeOnly) {
        for (int machineIndex = PLAYER_INVENTORY_SIZE; machineIndex < numSlots; machineIndex++) {
            Slot slot = inventorySlots.get(machineIndex);
            if (mergeOnly && slot.getStack().isEmpty()) {
                continue;
            }
            if (slot instanceof GhostSlot) {
                continue;
            }
            if (!slot.canInsert(stackToShift)) {
                continue;
            }
            if (shiftItemStackToRange(inventorySlots, stackToShift, machineIndex, 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shiftItemStackToRange(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
        boolean changed = shiftItemStackToRangeMerge(inventorySlots, stackToShift, start, count);
        changed |= shiftItemStackToRangeOpenSlots(inventorySlots, stackToShift, start, count);
        return changed;
    }

    private static boolean shiftItemStackToRangeOpenSlots(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
        if (stackToShift.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) {
            Slot slot = inventorySlots.get(slotIndex);
            ItemStack stackInSlot = slot.getStack();
            if (stackInSlot.isEmpty()) {
                int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
                stackInSlot = stackToShift.copy();
                stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                stackToShift.decrement(stackInSlot.getCount());
                slot.setStack(stackInSlot);
                slot.onSlotChanged();
                changed = true;
            }
        }
        return changed;
    }


    private static boolean shiftItemStackToRangeMerge(List<Slot> inventorySlots, ItemStack stackToShift, int start, int count) {
        if (!stackToShift.isStackable() || stackToShift.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (int slotIndex = start; !stackToShift.isEmpty() && slotIndex < start + count; slotIndex++) {
            Slot slot = inventorySlots.get(slotIndex);
            ItemStack stackInSlot = slot.getStack();
            if (!stackInSlot.isEmpty() && ItemHelper.simpleAreItemsEqual(stackInSlot, stackToShift)) {
                int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
                if (resultingStackSize <= max) {
                    stackToShift.setCount(0);
                    stackInSlot.setCount(resultingStackSize);
                    slot.onSlotChanged();
                    changed = true;
                } else if (stackInSlot.getCount() < max) {
                    stackToShift.decrement(max - stackInSlot.getCount());
                    stackInSlot.setCount(max);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        }
        return changed;
    }
}
