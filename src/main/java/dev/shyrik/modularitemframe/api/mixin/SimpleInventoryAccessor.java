package dev.shyrik.modularitemframe.api.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface SimpleInventoryAccessor {
    DefaultedList<ItemStack> mifGetStacks();
}
