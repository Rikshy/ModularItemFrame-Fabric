package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.mixin.BlockResistanceAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlastResistUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_resist");
    private static final Text NAME = new TranslatableText("modularitemframe.upgrade.resistance");

    private float oldAttached, oldMe;

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getName() {
        return NAME;
    }

    public void onInsert(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
        Block attached = world.getBlockState(pos.offset(facing.getOpposite())).getBlock();
        Block me = world.getBlockState(pos).getBlock();

        oldMe = me.getBlastResistance();
        oldAttached = attached.getBlastResistance();

        ((BlockResistanceAccessor)me).setModItFrResistance(99999);
        ((BlockResistanceAccessor)attached).setModItFrResistance(99999);
    }

    public void onRemove(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        Block attached = world.getBlockState(pos.offset(facing.getOpposite())).getBlock();
        Block me = world.getBlockState(pos).getBlock();

        ((BlockResistanceAccessor)me).setModItFrResistance(oldMe);
        ((BlockResistanceAccessor)attached).setModItFrResistance(oldAttached);
    }
}
