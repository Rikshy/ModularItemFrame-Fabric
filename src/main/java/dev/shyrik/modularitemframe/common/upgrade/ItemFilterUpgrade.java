package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemFilterUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_filter");

    private final SimpleInventory inv = new SimpleInventory(9);

    @Override
    public int getMaxCount() {
        return 99;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void onInsert(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        ItemFilterUpgradeItem.readTags(upStack, inv);
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        ItemFilterUpgradeItem.writeTags(upStack, inv);
    }
}
