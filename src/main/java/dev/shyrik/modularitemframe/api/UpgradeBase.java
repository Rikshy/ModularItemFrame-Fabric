package dev.shyrik.modularitemframe.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class UpgradeBase {
    UpgradeItem item;

    public UpgradeItem getItem() {
        return item;
    }

    public abstract int getMaxCount();
    public abstract Identifier getId();

    public void onInsert(World world, BlockPos pos, Direction facing, ItemStack upStack) {
    }

    public void onRemove(World world, BlockPos pos, Direction facing, ItemStack upStack) {
    }

    public CompoundTag toTag() {
        return new CompoundTag();
    }
    public void fromTag(CompoundTag tag) {
    }
}
