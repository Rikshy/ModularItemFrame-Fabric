package dev.shyrik.modularitemframe.common.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class ScrewdriverItem extends ToolItem {
    private static final String NBT_MODE = "mode";

    public ScrewdriverItem(Item.Settings props) {
        super(ToolMaterials.IRON, props);
    }

    @Override
    public void appendTooltip(ItemStack stack, World worldIn, List<Text> tooltip, TooltipContext ctx) {
        super.appendTooltip(stack, worldIn, tooltip, ctx);
        tooltip.add(new TranslatableText("Mode: " + readModeFromTag(stack).getName()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ActionResult result = ActionResult.PASS;
        if (!world.isClient && player.isSneaking()) {
            ItemStack driver = player.getStackInHand(hand);
            EnumMode mode = readModeFromTag(driver);
            mode = EnumMode.VALUES[mode.getIndex() + 1 >= EnumMode.values().length ? 0 : mode.getIndex() + 1];
            writeModeToTag(driver, mode);
            player.sendMessage(new TranslatableText("modularitemframe.message.screw_mode_change", mode.getName()), false);

            result = ActionResult.SUCCESS;
        }
        return new TypedActionResult<>(result, player.getStackInHand(hand));
    }

//    @Override
//    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
//        return true;
//    }

    @Override
    public boolean isNetworkSynced() {
        return true;
    }

    public static EnumMode getMode(ItemStack driver) {
        return readModeFromTag(driver);
    }

    private static void writeModeToTag(ItemStack stack, EnumMode mode) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) nbt = new CompoundTag();
        nbt.putInt(NBT_MODE, mode.getIndex());
        stack.setTag(nbt);
    }

    private static EnumMode readModeFromTag(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        EnumMode mode = EnumMode.REMOVE;
        if (nbt == null) writeModeToTag(stack, mode);
        else if (nbt.contains(NBT_MODE)) mode = EnumMode.VALUES[nbt.getInt(NBT_MODE)];
        return mode;
    }

    public enum EnumMode {
        REMOVE(0, "modularitemframe.message.screw_mode_change.rem"),
        INTERACT(1, "modularitemframe.message.screw_mode_change.inter");
        //ROTATE(2, "modularitemframe.message.screw_mode_change.rot");

        public static final EnumMode[] VALUES = new EnumMode[3];

        private final int index;
        private final String name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = nameIn;
        }

        public int getIndex() {
            return this.index;
        }

        public String getName() {
            return I18n.translate(this.name);
        }

        static {
            for (EnumMode enummode : values())
                VALUES[enummode.index] = enummode;
        }
    }
}
