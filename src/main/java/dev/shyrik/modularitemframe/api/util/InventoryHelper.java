package dev.shyrik.modularitemframe.api.util;

import dev.shyrik.modularitemframe.api.mixin.IngredientGetMatchingStacks;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.collection.DefaultedList;

public class InventoryHelper {
    public static int getFittingSlot(Inventory inventory, ItemStack stack) {
        int slot = findAvailableSlotForItem(inventory, stack);
        return slot < 0 ? getFirstUnOccupiedSlot(inventory) : slot;
    }

    public static int findAvailableSlotForItem(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); ++i)
            if (inventory.getStack(i).getCount() < inventory.getStack(i).getMaxCount() && ItemStack.areItemsEqual(inventory.getStack(i), stack))
                return i;
        return -1;
    }

    public static int getFirstUnOccupiedSlot(Inventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) if (inventory.getStack(i).isEmpty()) return i;
        return -1;
    }

    public static int getLastUnOccupiedSlot(Inventory inventory) {
        for (int i = inventory.size() - 1; i >= 0; i--) if (inventory.getStack(i).isEmpty()) return i;
        return -1;
    }

    public static int getFirstOccupiedSlot(Inventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) if (!inventory.getStack(i).isEmpty()) return i;
        return -1;
    }

    public static Inventory copyItemHandler(Inventory itemHandler) {
        Inventory copy = new SimpleInventory(itemHandler.size());

        for (int i = 0; i < itemHandler.size(); i++) {
            ItemStack stack = itemHandler.getStack(i);
            if (!stack.isEmpty()) {
                stack = stack.copy();
            }
            copy.setStack(i, stack);
        }
        return copy;
    }

    public static boolean canCraft(Inventory inventory, DefaultedList<IngredientGetMatchingStacks> ingredients) {
        Inventory copy = copyItemHandler(inventory);
        for (IngredientGetMatchingStacks ingredient : ingredients) {
            if (ingredient.getMatchingStacks().length > 0) {
                int slot = findSlotOfIngredient(copy, ingredient);
                if (slot >= 0)
                    copy.removeStack(slot, 1);
                else
                    return false;
            }
        }
        return true;
    }

    public static int countPossibleCrafts(Inventory inventory, Recipe recipe) {
        Inventory copy = copyItemHandler(inventory);
        int count = 0, slot = 0;
        while (slot >= 0) {
            for (IngredientGetMatchingStacks ingredient : ItemHelper.getIngredients(recipe)) {
                if (ingredient.getMatchingStacks().length > 0) {
                    slot = findSlotOfIngredient(copy, ingredient);
                    if (slot >= 0)
                        copy.removeStack(slot, 1);
                }
            }
            count++;
        }
        return count - 1;
    }

    public static int findSlotOfIngredient(Inventory inventory, IngredientGetMatchingStacks ingredient) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && ItemHelper.areItemsEqualIgnoreDurability(ingredient.getMatchingStacks(), stack)) {
                return slot;
            }
        }
        return -1;
    }

    public static void removeFromInventory(Inventory inventory, ItemStack[] toRemove) {
        for (int invSlot = 0; invSlot < inventory.size(); invSlot++) {
            ItemStack stack = inventory.getStack(invSlot);
            if (!stack.isEmpty() && ItemHelper.areItemsEqualIgnoreDurability(toRemove, stack)) {
                //if (stack.getItem().isDamageable())
                //    inventory.setStack(invSlot, stack.getItem().getContainerItem(stack));
                //else
                    inventory.removeStack(invSlot, 1);
                break;
            }
        }
    }

    public static ItemStack giveStack(Inventory inventory, ItemStack stack) {
        int slot = getFittingSlot(inventory, stack);
        if (slot < 0) return stack;

        ItemStack remain = insertItem(inventory, slot, stack);
        if (!remain.isEmpty()) return giveStack(inventory, remain);
        return ItemStack.EMPTY;
    }

    public static ItemStack insertItem(Inventory inventory, int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.size())
            return stack;

        ItemStack copy = stack.copy();

        ItemStack invStack = inventory.getStack(slot);
        int oldInvStackSize = invStack.getCount();
        if (invStack.isEmpty()) {
            if (stack.getCount() > inventory.getMaxCountPerStack()) {
                ItemStack insertCopy = stack.copy();
                insertCopy.setCount(inventory.getMaxCountPerStack());
                inventory.setStack(slot, insertCopy);
                invStack = insertCopy;
            } else {
                inventory.setStack(slot, stack);
                invStack = stack;
            }
        } else if (oldInvStackSize + stack.getCount() > invStack.getMaxCount()) {
            invStack.setCount(invStack.getMaxCount());
        } else {
            invStack.increment(stack.getCount());
        }

        copy.decrement(invStack.getCount() - oldInvStackSize);

        return copy;
    }

    public static void giveAllPossibleStacks(Inventory target, Inventory source, ItemStack stack, ItemStack prioSourceStack) {
        ItemStack remain = giveStack(target, prioSourceStack.copy());
        prioSourceStack.setCount(remain.getCount());
        if (!remain.isEmpty())
            return;

        for (int i = 0; i < source.size(); i++) {
            ItemStack sourceStack = source.getStack(i);
            if (ItemHelper.simpleAreStacksEqual(stack, sourceStack)) {
                remain = giveStack(target, sourceStack.copy());
                sourceStack.setCount(remain.getCount());
                if (!remain.isEmpty()) break;
            }
        }
    }

    public static CompoundTag toTag(CompoundTag tag, Inventory inventory, boolean setIfEmpty) {
        ListTag listTag = new ListTag();

        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte)i);
                itemStack.toTag(compoundTag);
                listTag.add(compoundTag);
            }
        }

        if (!listTag.isEmpty() || setIfEmpty) {
            tag.put("Items", listTag);
        }

        return tag;
    }

    public static void fromTag(CompoundTag tag, Inventory inventory) {
        ListTag listTag = tag.getList("Items", 10);

        for(int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            if (j >= 0 && j < inventory.size()) {
                inventory.setStack(j, ItemStack.fromTag(compoundTag));
            }
        }

    }
}
