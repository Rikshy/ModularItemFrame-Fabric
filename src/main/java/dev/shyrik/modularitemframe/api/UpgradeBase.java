package dev.shyrik.modularitemframe.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class UpgradeBase {
    UpgradeItem item;

    public UpgradeItem getItem() {
        return item;
    }

    /**
     * @return max times this upgrade can be present in a frame.
     */
    public abstract int getMaxCount();

    /**
     * @return unique ID the upgrade gets registered with.
     */
    public abstract Identifier getId();

    /**
     * @return the name of the upgrade :O
     */
    @Environment(EnvType.CLIENT)
    public abstract Text getName();

    /**
     * Called when upgrade is added to a frame.
     */
    public void onInsert(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
    }

    /**
     * Called when upgrade is removed with the {@link dev.shyrik.modularitemframe.common.item.ScrewdriverItem}
     * or destroyed.
     */
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
    }

    /**
     * Tag serialization in case there are some data to be saved!
     */
    public CompoundTag toTag() {
        return new CompoundTag();
    }

    /**
     * Tag deserialization in case there are some data to be saved!
     */
    public void fromTag(CompoundTag tag) {
    }
}
