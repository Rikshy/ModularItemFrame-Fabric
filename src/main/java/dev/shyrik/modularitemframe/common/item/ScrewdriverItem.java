package dev.shyrik.modularitemframe.common.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext ctx) {
        super.appendTooltip(stack, world, tooltip, ctx);
        tooltip.add(new TranslatableText("Mode: " + readModeFromTag(stack).getName()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ActionResult result = ActionResult.PASS;
        if (!world.isClient && player.isSneaking()) {
            ItemStack driver = player.getStackInHand(hand);
            EnumMode mode = readModeFromTag(driver);
            mode = EnumMode.values()[mode.getIndex() + 1 >= EnumMode.values().length ? 0 : mode.getIndex() + 1];
            writeModeToTag(driver, mode);
            player.sendMessage(new TranslatableText("modularitemframe.message.screw_mode_change", mode.getName()), false);

            result = ActionResult.SUCCESS;
        }
        return new TypedActionResult<>(result, player.getStackInHand(hand));
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
        EnumMode mode = EnumMode.REMOVE_MOD;
        if (nbt == null) writeModeToTag(stack, mode);
        else if (nbt.contains(NBT_MODE)) mode = EnumMode.values()[nbt.getInt(NBT_MODE)];
        return mode;
    }

    public enum EnumMode {
        REMOVE_MOD(0, "modularitemframe.mode.remove_module"),
        REMOVE_UP(1, "modularitemframe.mode.remove_upgrades"),
        INTERACT(2, "modularitemframe.mode.interact");
        //ROTATE(2, "modularitemframe.mode.rot");

        private final int index;
        private final String name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = nameIn;
        }

        public int getIndex() {
            return this.index;
        }

        @Environment(EnvType.CLIENT)
        public String getName() {
            return I18n.translate(this.name);
        }
    }
}
