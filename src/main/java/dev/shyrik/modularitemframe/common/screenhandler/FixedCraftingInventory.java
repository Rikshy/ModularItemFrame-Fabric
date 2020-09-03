package dev.shyrik.modularitemframe.common.screenhandler;

import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;

import java.util.Iterator;

public class FixedCraftingInventory extends DirectFixedItemInv implements RecipeInputProvider {

    public FixedCraftingInventory(int slotCount) {
        super(slotCount);
    }

    @Override
    public void provideRecipeInputs(RecipeFinder finder) {
        for (ItemStack itemStack : getStoredStacks()) {
            finder.addNormalItem(itemStack);
        }
    }
}
