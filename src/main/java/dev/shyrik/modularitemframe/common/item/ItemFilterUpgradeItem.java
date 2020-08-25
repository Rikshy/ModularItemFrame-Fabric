package dev.shyrik.modularitemframe.common.item;

import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.UpgradeItem;
import dev.shyrik.modularitemframe.common.screenhandler.filter.FilterUpgradeScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemFilterUpgradeItem extends UpgradeItem {

    private static final String NBT_FILTER = "item_filter";

    public ItemFilterUpgradeItem(Settings prop, Class<? extends UpgradeBase> upgradeClass, Identifier upgradeId) {
        super(prop, upgradeClass, upgradeId);
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

    public static void readTags(ItemStack stack, SimpleInventory inv) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(NBT_FILTER)) {
            inv.readTags(tag.getList(NBT_FILTER, 10));
        }
    }

    public static void writeTags(ItemStack stack, SimpleInventory inv) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(NBT_FILTER, inv.getTags());
    }
}
