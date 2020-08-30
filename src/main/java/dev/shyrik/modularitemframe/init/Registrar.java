package dev.shyrik.modularitemframe.init;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.ModuleItem;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.UpgradeItem;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import dev.shyrik.modularitemframe.common.item.ScrewdriverItem;
import dev.shyrik.modularitemframe.common.module.t1.IOModule;
import dev.shyrik.modularitemframe.common.module.t1.ItemModule;
import dev.shyrik.modularitemframe.common.module.t1.StorageModule;
import dev.shyrik.modularitemframe.common.module.t1.TankModule;
import dev.shyrik.modularitemframe.common.module.t2.*;
import dev.shyrik.modularitemframe.common.module.t3.*;
import dev.shyrik.modularitemframe.common.upgrade.*;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "SameParameterValue"})
public class Registrar {
    public static final LinkedHashMap<Item, Identifier> ITEMS = new LinkedHashMap<>();
    public static final LinkedHashMap<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    public static final LinkedHashMap<BlockEntityType<?>, Identifier> BLOCK_ENTITIES = new LinkedHashMap<>();

    public static final Block MODULAR_FRAME = create("modular_frame", new ModularFrameBlock(ModularFrameBlock.DEFAULT_SETTINGS));

    @SuppressWarnings("unchecked")
    public static final BlockEntityType<ModularFrameEntity> MODULAR_FRAME_ENTITY = (BlockEntityType<ModularFrameEntity>) create("modularframe", ModularFrameEntity::new, MODULAR_FRAME);

    public static final Item SCREWDRIVER = create("screwdriver",
            new ScrewdriverItem(new Item.Settings().group(ModularItemFrame.GROUP)));
    public static final Item CANVAS = create("canvas", new Item(new Item.Settings().group(ModularItemFrame.GROUP)));
    public static final Item MODULAR_FRAME_ITEM = create("modular_frame",
            new BlockItem(MODULAR_FRAME, new Item.Settings().group(ModularItemFrame.GROUP)));

    public static final ModuleItem IO_MODULE = createMod(IOModule.ID, IOModule.class);
    public static final ModuleItem ITEM_MODULE = createMod(ItemModule.ID, ItemModule.class);
    public static final ModuleItem STORAGE_MODULE = createMod(StorageModule.ID, StorageModule.class);
    public static final ModuleItem TANK_MODULE = createMod(TankModule.ID, TankModule.class);

    public static final ModuleItem BLOCK_BREAKER = createMod(BlockBreakModule.ID, BlockBreakModule.class);
    public static final ModuleItem BLOCK_PLACER = createMod(BlockPlaceModule.ID, BlockPlaceModule.class);
    public static final ModuleItem CRAFTING_MODULE = createMod(CraftingModule.ID, CraftingModule.class);
    public static final ModuleItem TRASH_CAN_MODULE = createMod(TrashCanModule.ID, TrashCanModule.class);
    public static final ModuleItem DISPENSE_MODULE = createMod(DispenseModule.ID, DispenseModule.class);
    public static final ModuleItem SLAY_MODULE = createMod(SlayModule.ID, SlayModule.class);
    public static final ModuleItem VACUUM_MODULE = createMod(VacuumModule.ID, VacuumModule.class);
    public static final ModuleItem FAN_MODULE = createMod(FanModule.ID, FanModule.class);

    public static final ModuleItem AUTO_CRAFTING_MODULE = createMod(AutoCraftingModule.ID, AutoCraftingModule.class);
    public static final ModuleItem FLUID_DISPENSE_MODULE = createMod(FluidDispenserModule.ID, FluidDispenserModule.class);
    public static final ModuleItem ITEM_TP_MODULE = createMod(ItemTeleportModule.ID, ItemTeleportModule.class);
    public static final ModuleItem TP_MODULE = createMod(TeleportModule.ID, TeleportModule.class);
    public static final ModuleItem XP_MODULE = createMod(XPModule.ID, XPModule.class);
    public static final ModuleItem JUKEBOX_MODULE = createMod(JukeboxModule.ID, JukeboxModule.class);


    public static final UpgradeItem SPEED_UPGRADE = createUp(SpeedUpgrade.ID, SpeedUpgrade.class);
    public static final UpgradeItem RANGE_UPGRADE = createUp(RangeUpgrade.ID, RangeUpgrade.class);
    public static final UpgradeItem CAP_UPGRADE = createUp(CapacityUpgrade.ID, CapacityUpgrade.class);
    public static final UpgradeItem INFINITY_UPGRADE = createUp(InfinityUpgrade.ID, InfinityUpgrade.class);
    public static final UpgradeItem SECURITY_UPGRADE = createUp(SecurityUpgrade.ID, SecurityUpgrade.class);
    public static final UpgradeItem BLAST_RESIST_UPGRADE = createUp(BlastResistUpgrade.ID, BlastResistUpgrade.class);
    public static final UpgradeItem ITEM_FILTER_UPGRADE =  create(ItemFilterUpgrade.ID.getPath(),
            new ItemFilterUpgradeItem(new Item.Settings().group(ModularItemFrame.GROUP), ItemFilterUpgrade.class, ItemFilterUpgrade.ID));

    public static void register() {
        ITEMS.keySet().forEach(item -> Registry.register(Registry.ITEM, ITEMS.get(item), item));
        BLOCKS.keySet().forEach(item -> Registry.register(Registry.BLOCK, BLOCKS.get(item), item));
        BLOCK_ENTITIES.keySet().forEach(blockEntity ->
                Registry.register(Registry.BLOCK_ENTITY_TYPE, BLOCK_ENTITIES.get(blockEntity), blockEntity));
    }

    private static <T extends Item> T create(String name, T item){
        ITEMS.put(item, new Identifier(ModularItemFrame.MOD_ID, name));
        return item;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> T createMod(Identifier id, Class<? extends ModuleBase> moduleClass){
        T item = (T)new ModuleItem(new Item.Settings().group(ModularItemFrame.GROUP), moduleClass, id);
        ITEMS.put(item, id);
        return item;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> T createUp(Identifier id, Class<? extends UpgradeBase> upgradeClass){
        T item = (T)new UpgradeItem(new Item.Settings().group(ModularItemFrame.GROUP), upgradeClass, id);
        ITEMS.put(item, id);
        return item;
    }

    private static <T extends Block> T create(String name, T block){
        BLOCKS.put(block, new Identifier(ModularItemFrame.MOD_ID, name));
        return block;
    }

    private static BlockEntityType<?> create(String name, Supplier<? extends BlockEntity> supplier, Block block){
        BlockEntityType<?> type = BlockEntityType.Builder.create(supplier, block).build(null);
        BLOCK_ENTITIES.put(type, new Identifier(ModularItemFrame.MOD_ID, name));
        return type;
    }
}
