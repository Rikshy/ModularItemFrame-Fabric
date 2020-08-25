package dev.shyrik.modularitemframe.common.upgrade;

import alexiil.mc.lib.attributes.item.filter.AggregateItemFilter;
import alexiil.mc.lib.attributes.item.filter.ConstantItemFilter;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.mixin.SimpleInventoryAccessor;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import dev.shyrik.modularitemframe.mixin.SimpleInventoryMixin;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemFilterUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_filter");
    private static final String NBT_FILTER = "item_filter";

    private final SimpleInventory inv = new SimpleInventory(9);
    private ItemFilter filter = null;

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
        filter = AggregateItemFilter.anyOf(
                ((SimpleInventoryAccessor)inv).mifGetStacks()
                        .stream().filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new));
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, ItemStack upStack) {
        ItemFilterUpgradeItem.writeTags(upStack, inv);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_FILTER, inv.getTags());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_FILTER)) {
            inv.readTags(tag.getList(NBT_FILTER, 10));
        }
    }

    public ItemFilter getFilter() {
        return filter;
    }
}
