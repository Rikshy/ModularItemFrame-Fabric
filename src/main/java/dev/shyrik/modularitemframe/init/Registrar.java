package dev.shyrik.modularitemframe.init;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class Registrar {
    public static final LinkedHashMap<Item, Identifier> ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    public static final LinkedHashMap<BlockEntityType<?>, Identifier> BLOCK_ENTIIES = new LinkedHashMap<>();

    public static final Block MODULARFRAME = create("modular_frame", new ModularFrameBlock(ModularFrameBlock.DEFAULT_SETTINGS));

    @SuppressWarnings("unchecked")
    public static final BlockEntityType<ModularFrameEntity> MODULARFRAME_ENTITY = (BlockEntityType<ModularFrameEntity>) create("modularframe", ModularFrameEntity::new, MODULARFRAME);

    public static void register() {
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));
        BLOCKS.keySet().forEach(item -> Registry.register(Registry.BLOCK, BLOCKS.get(item), item));
        BLOCK_ENTIIES.keySet().forEach(blockEntity -> Registry.register(Registry.BLOCK_ENTITY_TYPE, BLOCK_ENTIIES.get(blockEntity), blockEntity));
    }

    private static <T extends Item> T create(String name, T item){
        ITEMS.put(item, new Identifier(ModularItemFrame.MOD_ID, name));
        return item;
    }

    private static <T extends Block> T create(String name, T block){
        BLOCKS.put(block, new Identifier(ModularItemFrame.MOD_ID, name));
        return block;
    }

    private static BlockEntityType<?> create(String name, Supplier<? extends BlockEntity> supplier, Block block){
        BlockEntityType<?> type = BlockEntityType.Builder.create(supplier, block).build(null);
        BLOCK_ENTIIES.put(type, new Identifier(ModularItemFrame.MOD_ID, name));
        return type;
    }
}
