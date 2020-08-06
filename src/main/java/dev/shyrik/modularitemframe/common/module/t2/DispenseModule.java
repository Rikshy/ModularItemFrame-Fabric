package dev.shyrik.modularitemframe.common.module.t2;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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

public class DispenseModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_dispense");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_dispense");
    private static final String NBT_RANGE = "range";

    private int range = 1;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.dispense");
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        int countRange = blockEntity.getRangeUpCount() + 1;
        if (!world.isClient && countRange > 1) {
            if (player.isSneaking()) range--;
            else range++;
            if (range < 1) range = countRange;
            if (range > countRange) range = 1;
            player.sendMessage(new TranslatableText("modularitemframe.message.range_change", range + 1), false);
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (!held.isEmpty()) {
                ItemHelper.ejectStack(world, pos, facing.getOpposite(), held.copy());
                held.setCount(0);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();
        range = Math.min(range, blockEntity.getRangeUpCount() + 1);
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (!world.isClient) {
            if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

            Direction facing = blockEntity.blockFacing();
            BlockEntity targetTile = world.getBlockEntity(pos.offset(facing.getOpposite(), range));
            if (targetTile != null) {
                if (targetTile instanceof Inventory) { //TODO? sided inventory?
                    Inventory inv = (Inventory)targetTile;
                    for (int slot = 0; slot < inv.size(); slot++) {
                        if (!inv.getStack(slot).isEmpty()) {
                            ItemHelper.ejectStack(world, pos, facing, inv.getStack(slot));
                            inv.setStack(slot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_RANGE, range);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_RANGE)) range = tag.getInt(NBT_RANGE);
    }
}
