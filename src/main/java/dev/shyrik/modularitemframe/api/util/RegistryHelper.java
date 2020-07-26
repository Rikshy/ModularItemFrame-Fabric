package dev.shyrik.modularitemframe.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistryHelper {

    public static Identifier getItemId(Item item) {
        return Registry.ITEM.getId(item);
    }

    public static Identifier getItemId(ItemStack stack) {
        return getItemId(stack.getItem());
    }

    public static Identifier getBlockId(Block block) {
        return Registry.BLOCK.getId(block);
    }
}
