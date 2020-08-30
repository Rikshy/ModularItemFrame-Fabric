package dev.shyrik.modularitemframe.common.upgrade;

import alexiil.mc.lib.attributes.item.filter.AggregateItemFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.common.item.ItemFilterUpgradeItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemFilterUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_filter");
    private static final Text NAME = new TranslatableText("modularitemframe.upgrade.filter");

    private SimpleInventory inv;
    private ItemFilter filter = null;
    public ItemFilterUpgradeItem.EnumMode mode;

    @Override
    public int getMaxCount() {
        return 99;
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

    @Override
    public void onInsert(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
        fromTag(upStack.getOrCreateTag());
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
        ItemFilterUpgradeItem.writeTags(upStack.getOrCreateTag(), inv, mode);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        ItemFilterUpgradeItem.writeTags(tag, inv, mode);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        ItemFilterUpgradeItem.TagReadResult result = ItemFilterUpgradeItem.readTags(tag);
        inv = new SimpleInventory(result.stacks.toArray(new ItemStack[0]));
        filter = AggregateItemFilter.anyOf(
                result.stacks
                        .stream().filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new));
        mode = result.mode;

        if (mode == ItemFilterUpgradeItem.EnumMode.BLACKLIST)
            filter = filter.negate();
    }

    public ItemFilter getFilter() {
        return filter;
    }
}
