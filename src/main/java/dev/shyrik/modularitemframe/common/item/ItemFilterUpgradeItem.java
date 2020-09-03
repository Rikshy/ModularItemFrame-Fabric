package dev.shyrik.modularitemframe.common.item;

import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.UpgradeItem;
import dev.shyrik.modularitemframe.common.screenhandler.filter.FilterUpgradeScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ItemFilterUpgradeItem extends UpgradeItem {

    private static final String NBT_FILTER = "item_filter";
    private static final String NBT_MODE = "filter_mode";

    public ItemFilterUpgradeItem(Settings prop, Class<? extends UpgradeBase> upgradeClass, Identifier upgradeId) {
        super(prop, upgradeClass, upgradeId);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        EnumMode mode = readModeTag(stack.getOrCreateTag());
        tooltip.add(new TranslatableText("modularitemframe.tooltip.mode").append(mode.getName()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!user.isSneaking()) return TypedActionResult.pass(stack);

        if (!world.isClient) {
            user.openHandledScreen(getScreenHandler(stack));
        }

        return TypedActionResult.success(stack);
    }

    public NamedScreenHandlerFactory getScreenHandler(ItemStack filter) {
        return new SimpleNamedScreenHandlerFactory((id, playerInventory, player) ->
                new FilterUpgradeScreenHandler(
                        id,
                        player.inventory,
                        filter),
                new TranslatableText("gui.modularitemframe.filter.name")
        );
    }

    public static TagReadResult readTags(CompoundTag tag) {
        TagReadResult result = new TagReadResult();
        result.inv = readInvTag(tag);
        result.mode = readModeTag(tag);
        return result;
    }

    public static DirectFixedItemInv readInvTag(CompoundTag tag) {
        DirectFixedItemInv inv = new DirectFixedItemInv(9);
        inv.fromTag(tag.getCompound(NBT_FILTER));
        return inv;
    }

    public static EnumMode readModeTag(CompoundTag tag) {
        EnumMode mode = EnumMode.WHITELIST;
        if (tag.contains(NBT_MODE)) {
            mode = EnumMode.values()[tag.getInt(NBT_MODE)];
        }
        return mode;
    }

    public static void writeTags(CompoundTag tag, DirectFixedItemInv inv, EnumMode mode) {
        writeInvTag(tag, inv);
        tag.putInt(NBT_MODE, mode.getIndex());
    }

    public static void writeInvTag(CompoundTag tag, DirectFixedItemInv inv) {
        tag.put(NBT_FILTER, inv.toTag());
    }

    public static class TagReadResult {
        public DirectFixedItemInv inv;
        public EnumMode mode = EnumMode.WHITELIST;
    }

    public enum EnumMode {
        WHITELIST(0, "modularitemframe.mode.whitelist"),
        BLACKLIST(1, "modularitemframe.mode.blacklist");

        private final int index;
        private final Text name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = new TranslatableText(nameIn);
        }

        public int getIndex() {
            return this.index;
        }

        @Environment(EnvType.CLIENT)
        public Text getName() {
            return this.name;
        }
    }
}
