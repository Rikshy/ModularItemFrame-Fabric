package dev.shyrik.modularitemframe.common.module.t2;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t1.CraftingModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CraftingPlusModule extends CraftingModule {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_craft_plus");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_craft_plus");
    private static final String NBT_MODE = "cpmode";

    public EnumMode mode = EnumMode.PLAYER;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.crafting_plus");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD_LOC;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            if (player.isSneaking()) {
                int modeIdx = mode.getIndex() + 1;
                if (modeIdx == EnumMode.values().length) modeIdx = 0;
                mode = EnumMode.values()[modeIdx];
                player.sendMessage(new TranslatableText(mode.getName()), false);
                blockEntity.markDirty();
            } else {
                player.openHandledScreen(getScreenHandler(blockEntity.getCachedState(), world, pos));
                blockEntity.markDirty();
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        return null;
    }

    @Override
    protected Inventory getWorkingInventories(Inventory playerInventory) {
        Inventory neighborInventory = (Inventory)blockEntity.getAttachedInventory();

        if (neighborInventory != null) {
            //if (mode == EnumMode.NO_PLAYER) return neighborInventory;
            //else return new CombinedInvWrapper(neighborInventory, playerInventory);
        }
        return playerInventory;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_MODE, mode.getIndex());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_MODE)) mode = EnumMode.values()[tag.getInt(NBT_MODE)];
    }

    public enum EnumMode {
        PLAYER(0, "modularitemframe.message.crafting_plus.player"),
        NO_PLAYER(1, "modularitemframe.message.crafting_plus.no_player");

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
