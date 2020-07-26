package dev.shyrik.modularitemframe.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class UpgradeBase {
    UpgradeItem parent;

    public UpgradeItem getParent() {
        return parent;
    }

    public abstract int getMaxCount();
    public abstract Identifier getId();

    public void onInsert(World world, BlockPos pos, Direction facing) {
    }

    public void onRemove(World world, BlockPos pos, Direction facing) {
    }
}
