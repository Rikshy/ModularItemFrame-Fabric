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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StorageModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_storage");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_storage");

    private static final String NBT_LAST = "lastclick";
    private static final String NBT_LASTSTACK = "laststack";
    private static final String NBT_INVENTORY= "inventory";

    private final SimpleInventory inventory = new SimpleInventory(1);

    private long lastClick;
    private ItemStack lastStack = ItemStack.EMPTY;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.storage");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        ItemRenderHelper.renderOnFrame(lastStack, blockEntity.blockFacing(), 0, 0.1F, ModelTransformation.Mode.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            int slot = InventoryHelper.getFirstOccupiedSlot(inventory);
            if (slot >= 0) {
                int amount = player.isSneaking() ? inventory.getStack(slot).getMaxCount() : 1;
                ItemStack extract = inventory.removeStack(slot, amount);
                extract = InventoryHelper.giveStack(player.inventory, extract);
                if (!extract.isEmpty()) ItemHelper.ejectStack(world, pos, blockEntity.blockFacing(), extract);
                blockEntity.markDirty();
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (lastStack.isEmpty() || ItemStack.areItemsEqual(lastStack, held)) {
                long time = world.getTime();

                if (time - lastClick <= 8L && !player.isSneaking() && !lastStack.isEmpty())
                    InventoryHelper.giveAllPossibleStacks(inventory, player.inventory, lastStack, held);
                else if (!held.isEmpty()) {
                    ItemStack heldCopy = held.copy();
                    heldCopy.setCount(1);
                    if (InventoryHelper.giveStack(inventory, heldCopy).isEmpty()) {
                        held.decrement(1);

                        lastStack = heldCopy;
                        lastClick = time;
                    }
                }
                blockEntity.markDirty();
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onFrameUpgradesChanged() {
        int newCapacity = (int)Math.pow(2, blockEntity.getCapacityUpCount());
        SimpleInventory tmp = new SimpleInventory(newCapacity);
        for (int slot = 0; slot < inventory.size(); slot++) {
            if (slot < tmp.size())
                tmp.setStack(slot, inventory.getStack(slot));
            else
                ItemHelper.ejectStack(blockEntity.getWorld(), blockEntity.getPos(), blockEntity.blockFacing(), inventory.getStack(slot));
        }
        blockEntity.markDirty();
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        super.onRemove(world, pos, facing, player);
        for( int slot = 0; slot < inventory.size(); slot++) {
            ItemHelper.ejectStack(world, pos, blockEntity.blockFacing(), inventory.getStack(slot));
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_INVENTORY, InventoryHelper.toTag(new CompoundTag(), inventory, false));
        tag.putLong(NBT_LAST, lastClick);
        tag.put(NBT_LASTSTACK, lastStack.toTag(new CompoundTag()));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_INVENTORY)) InventoryHelper.fromTag(tag.getCompound(NBT_INVENTORY), inventory);
        if (tag.contains(NBT_LAST)) lastClick = tag.getLong(NBT_LAST);
        if (tag.contains(NBT_LASTSTACK)) lastStack = ItemStack.fromTag(tag.getCompound(NBT_LASTSTACK));
    }
}
