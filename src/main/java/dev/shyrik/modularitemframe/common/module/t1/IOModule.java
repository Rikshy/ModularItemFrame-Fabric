package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t1_io");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_io");

    private static final String NBT_LAST = "lastclick";
    private static final String NBT_LASTSTACK = "laststack";
    private static final String NBT_DISPLAY = "display";

    private long lastClick;
    private ItemStack displayItem = ItemStack.EMPTY;
    private ItemStack lastStack = ItemStack.EMPTY;

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer tesr, @Nonnull MatrixStack matrixStack, float partialTicks, @Nonnull IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        FrameItemRenderer.renderOnFrame(displayItem, blockEntity.blockFacing(), 0F, 0.1F, TransformType.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.io");
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, PlayerEntity playerIn) {
        if (!worldIn.isClient) {
            Inventory handler = (Inventory) blockEntity.getAttachedInventory();
            if (handler != null) {
                Direction blockFacing = blockEntity.blockFacing();
                Inventory player = playerIn.inventory;

                int slot = InventoryHelper.getFirstOccupiedSlot(handler);
                if (slot >= 0) {
                    int amount = playerIn.isSneaking() ? handler.getStack(slot).getMaxCount() : 1;
                    ItemStack extract = handler.removeStack(slot, amount);
                    extract = InventoryHelper.giveStack(player, extract);
                    if (!extract.isEmpty()) ItemHelper.ejectStack(worldIn, pos, blockFacing, extract);
                    blockEntity.getAttachedTile().markDirty();
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (!worldIn.isClient) {
            Inventory handler = (Inventory)blockEntity.getAttachedInventory();
            if (handler != null) {
                Inventory player = playerIn.inventory;
                ItemStack held = playerIn.getStackInHand(hand);
                long time = worldIn.getTime();

                if (time - lastClick <= 8L && !playerIn.isSneaking() && !lastStack.isEmpty())
                    InventoryHelper.giveAllPossibleStacks(handler, player, lastStack, held);
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
        CompoundTag cmp = new CompoundTag();
        cmp.putLong(NBT_LAST, lastClick);
        cmp.put(NBT_LASTSTACK, lastStack.toTag(new CompoundTag()));
        cmp.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        return cmp;
    }

    @Override
    public void fromTag(CompoundTag cmp) {
        if (cmp.contains(NBT_LAST)) lastClick = cmp.getLong(NBT_LAST);
        if (cmp.contains(NBT_LASTSTACK)) lastStack = ItemStack.fromTag(cmp.getCompound(NBT_LASTSTACK));
        if (cmp.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(cmp.getCompound(NBT_DISPLAY));
    }
}
