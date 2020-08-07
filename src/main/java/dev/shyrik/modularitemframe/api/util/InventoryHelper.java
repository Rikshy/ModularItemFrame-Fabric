package dev.shyrik.modularitemframe.api.util;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;

public class InventoryHelper {

    public static ItemStack givePlayer(PlayerEntity player, ItemStack stack) {
        return new FixedInventoryVanillaWrapper(player.inventory).getInsertable().insert(stack);
    }

    public static FixedItemInv copyItemHandler(FixedItemInv itemHandler) {
        FixedItemInv copy = new DirectFixedItemInv(itemHandler.getSlotCount());

        for (int i = 0; i < itemHandler.getSlotCount(); i++) {
            ItemStack stack = itemHandler.getInvStack(i);
            if (!stack.isEmpty()) {
                stack = stack.copy();
            }
            copy.setInvStack(i, stack, Simulation.ACTION);
        }
        return copy;
    }

    public static boolean canCraft(FixedItemInv inventory, CraftingRecipe recipe) {
        FixedItemInv copy = copyItemHandler(inventory);
        for (Ingredient ingredient : recipe.getPreviewInputs()) {
            int slot = findSlotOfIngredient(copy, ingredient);
            if (slot >= 0)
                copy.getInvStack(slot).decrement(1);
            else
                return false;
        }
        return true;
    }

    public static int countPossibleCrafts(FixedItemInv inventory, CraftingRecipe recipe) {
        FixedItemInv copy = copyItemHandler(inventory);
        int count = 0, slot = 0;
        while (slot >= 0) {
            for (Ingredient ingredient : recipe.getPreviewInputs()) {
                slot = findSlotOfIngredient(copy, ingredient);
                if (slot >= 0)
                    copy.getInvStack(slot).decrement(1);
            }
            count++;
        }
        return count - 1;
    }

    public static int findSlotOfIngredient(FixedItemInv inventory, Ingredient ingredient) {
        for (int slot = 0; slot < inventory.getSlotCount(); slot++) {
            ItemStack stack = inventory.getInvStack(slot);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public static void removeIngredients(FixedItemInv inventory, CraftingRecipe recipe) {
        for (Ingredient ingredient : recipe.getPreviewInputs()) {
            for (int invSlot = 0; invSlot < inventory.getSlotCount(); invSlot++) {
                ItemStack stack = inventory.getInvStack(invSlot);
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    //if (stack.getItem().isDamageable())
                    //    inventory.setStack(invSlot, stack.getItem().getContainerItem(stack));
                    //else
                    inventory.setInvStack(invSlot, stack.split(stack.getCount() - 1), Simulation.ACTION);
                    break;
                }
            }
        }
    }

    public static void giveAllPossibleStacks(ItemInsertable target, Inventory source, ItemStack stack, ItemStack prioritySourceStack) {
        ItemStack remain = target.insert(prioritySourceStack.copy());
        prioritySourceStack.setCount(remain.getCount());
        if (!remain.isEmpty())
            return;

        for (int i = 0; i < source.size(); i++) {
            ItemStack sourceStack = source.getStack(i);
            if (ItemHelper.simpleAreStacksEqual(stack, sourceStack)) {
                remain = target.insert(sourceStack.copy());
                sourceStack.setCount(remain.getCount());
                if (!remain.isEmpty()) break;
            }
        }
    }
}
