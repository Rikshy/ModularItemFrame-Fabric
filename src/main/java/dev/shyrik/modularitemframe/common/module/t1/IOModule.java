package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.client.helper.ItemRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class IOModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_io");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_io");

    private static final String NBT_LAST = "lastclick";
    private static final String NBT_LASTSTACK = "laststack";
    private static final String NBT_DISPLAY = "display";

    private long lastClick;
    private ItemStack displayItem = ItemStack.EMPTY;
    private ItemStack lastStack = ItemStack.EMPTY;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        ItemRenderHelper.renderOnFrame(displayItem, blockEntity.blockFacing(), 0F, 0.1F, ModelTransformation.Mode.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.io");
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            Inventory handler = (Inventory) blockEntity.getAttachedInventory();
            if (handler != null) {
                Direction blockFacing = blockEntity.blockFacing();

                int slot = InventoryHelper.getFirstOccupiedSlot(handler);
                if (slot >= 0) {
                    int amount = player.isSneaking() ? handler.getStack(slot).getMaxCount() : 1;
                    ItemStack extract = handler.removeStack(slot, amount);
                    extract = InventoryHelper.giveStack(player.inventory, extract);
                    if (!extract.isEmpty()) ItemHelper.ejectStack(world, pos, blockFacing, extract);
                    blockEntity.getAttachedTile().markDirty();
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            Inventory handler = (Inventory)blockEntity.getAttachedInventory();
            if (handler != null) {
                ItemStack held = player.getStackInHand(hand);
                long time = world.getTime();

                if (time - lastClick <= 8L && !player.isSneaking() && !lastStack.isEmpty())
                    InventoryHelper.giveAllPossibleStacks(handler, player.inventory, lastStack, held);
                else if (!held.isEmpty()) {
                    ItemStack heldCopy = held.copy();
                    heldCopy.setCount(1);
                    if (InventoryHelper.giveStack(handler, heldCopy).isEmpty()){
                        held.decrement(1);

                        lastStack = heldCopy;
                        lastClick = time;
                    }
                }
                blockEntity.markDirty();
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if(!world.isClient) {
            Inventory handler = blockEntity.getAttachedInventory();
            if (handler != null) {
                int slot = InventoryHelper.getFirstOccupiedSlot(handler);
                if (slot >= 0) {
                    ItemStack slotStack = handler.getStack(slot);
                    if (!ItemStack.areItemsEqual(slotStack, displayItem)) {
                        ItemStack copy = slotStack.copy();
                        copy.setCount(1);
                        displayItem = copy;
                        blockEntity.markDirty();
                    }
                } else {
                    displayItem = ItemStack.EMPTY;
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public  CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_LAST, lastClick);
        tag.put(NBT_LASTSTACK, lastStack.toTag(new CompoundTag()));
        tag.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(NBT_LAST)) lastClick = tag.getLong(NBT_LAST);
        if (tag.contains(NBT_LASTSTACK)) lastStack = ItemStack.fromTag(tag.getCompound(NBT_LASTSTACK));
        if (tag.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(tag.getCompound(NBT_DISPLAY));
    }
}
