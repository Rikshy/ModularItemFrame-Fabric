package dev.shyrik.modularitemframe.mixin;

import dev.shyrik.modularitemframe.api.mixin.SimpleInventoryAccessor;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleInventory.class)
public abstract class SimpleInventoryMixin implements SimpleInventoryAccessor {

    @Accessor("stacks")
    public abstract DefaultedList<ItemStack> mifGetStacks();
}
