package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.mixin.BlockResistanceAccessor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlastResistUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_resist");

    private float oldAttached, oldMe;

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public void onInsert(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        Block attached = world.getBlockState(pos.offset(facing.getOpposite())).getBlock();
        Block me = world.getBlockState(pos).getBlock();

        oldMe = me.getBlastResistance();
        oldAttached = attached.getBlastResistance();

        ((BlockResistanceAccessor)me).setResistance(99999);
        ((BlockResistanceAccessor)attached).setResistance(99999);
    }

    public void onRemove(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        Block attached = world.getBlockState(pos.offset(facing.getOpposite())).getBlock();
        Block me = world.getBlockState(pos).getBlock();

        ((BlockResistanceAccessor)me).setResistance(oldMe);
        ((BlockResistanceAccessor)attached).setResistance(oldAttached);
    }
}
