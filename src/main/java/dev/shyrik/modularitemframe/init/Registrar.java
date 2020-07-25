package dev.shyrik.modularitemframe.init;

import dev.shyrik.modularitemframe.ModularItemFrame;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;

public class Registrar {
    private static final LinkedHashMap<Item, Identifier> ITEMS = new LinkedHashMap<>();
    private static final LinkedHashMap<Block, Identifier> BLOCKS = new LinkedHashMap<>();


    public static void register() {
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));
        BLOCKS.keySet().forEach(item -> Registry.register(Registry.BLOCK, BLOCKS.get(item), item));
    }
}
