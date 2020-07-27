package dev.shyrik.modularitemframe.common.module.t2;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.dto.PlayerInfo;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class UseModule extends ModuleBase {
    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t2_use");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");

    private static final GameProfile DEFAULT_CLICKER = new GameProfile(UUID.nameUUIDFromBytes("modularitemframe".getBytes()), "[Frame Clicker]");

    private static final String NBT_DISPLAY = "display";
    private static final String NBT_ROTATION = "rotation";
    private static final String NBT_SNEAK = "sneaking";
    private static final String NBT_RIGHT = "rightclick";

    private boolean isSneaking = false;
    private boolean rightClick = false;
    private int rotation = 0;
    private ItemStack displayItem = ItemStack.EMPTY;
    private WeakReference<FakePlayerHelper.UsefulFakePlayer> player;

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer tesr,  MatrixStack matrixStack, float partialTicks,  IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        Direction facing = blockEntity.blockFacing();
        switch (facing) {
            case DOWN:
            case NORTH:
                FrameItemRenderer.renderOnFrame(displayItem, Direction.WEST, rotation, 0.5F, TransformType.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case UP:
            case SOUTH:
                FrameItemRenderer.renderOnFrame(displayItem, Direction.EAST, rotation, 0.5F, TransformType.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case WEST:
                matrixStack.rotate(new Quaternion(0, 90.0F, 0.0F, true));
                matrixStack.translate(-1, 0 ,0);
                FrameItemRenderer.renderOnFrame(displayItem, Direction.WEST, rotation, 0.5F, TransformType.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case EAST:
                matrixStack.rotate(new Quaternion(0, 90.0F, 0.0F, true));
                matrixStack.translate(-1, 0 ,0);
                FrameItemRenderer.renderOnFrame(displayItem, Direction.EAST, rotation, 0.5F, TransformType.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.use");
    }

    @Override
    public void onRemove( World worldIn, BlockPos pos, Direction facing,  PlayerEntity playerIn) {
        if (!worldIn.isClient) ItemHelper.ejectStack(worldIn, pos, facing, displayItem);
    }

    @Override
    public ActionResult onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (!worldIn.isClient) {
            ItemStack held = playerIn.getStackInHand(hand);
            if (held.isEmpty()) {
                playerIn.setStackInHand(hand, displayItem.copy());
                displayItem.setCount(0);
            } else {
                if (displayItem.isEmpty()) {
                    displayItem = held.copy();
                    playerIn.setStackInHand(hand, ItemStack.EMPTY);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw( World world,  BlockPos pos,  PlayerEntity playerIn, ItemStack driver) {
        if (!world.isClient) {
            if (playerIn.isSneaking()) {
                isSneaking = !isSneaking;
            } else {
                rightClick = !rightClick;
            }
            String mode = isSneaking ? I18n.format("modularitemframe.mode.sn") + " + " : "";
            mode += rightClick ? I18n.format("modularitemframe.mode.rc") : I18n.format("modularitemframe.mode.lc");

            playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.mode_change", mode));
            blockEntity.markDirty();
        }
    }

    @Override
    public void tick( World world,  BlockPos pos) {
        if (!world.isClient) {
            if (displayItem.isEmpty()) {
                displayItem = getNextStack();
                rotation = 0;
                blockEntity.markDirty();
            } else {
                if (rotation >= 360) {
                    rotation -= 360;
                    hitIt(world, pos);
                }
                rotation += 15 * (blockEntity.getSpeedUpCount() + 1);
                blockEntity.markDirty();
            }
        }
    }

    private void hitIt(World world, BlockPos pos) {
        if (player == null) player = new WeakReference<>(FakePlayerHelper.getPlayer(world, DEFAULT_CLICKER));

        Direction facing = blockEntity.blockFacing();
        FakePlayerHelper.setupFakePlayerForUse(getPlayer(), pos, facing, displayItem, isSneaking);
        ItemStack result;
        if (rightClick)
            result = FakePlayerHelper.rightClickInDirection(getPlayer(), world, pos.offset(facing), facing, world.getBlockState(pos), 2 + blockEntity.getRangeUpCount());
        else
            result = FakePlayerHelper.leftClickInDirection(getPlayer(), world, pos.offset(facing), facing, world.getBlockState(pos), 2 + blockEntity.getRangeUpCount());
        FakePlayerHelper.cleanupFakePlayerFromUse(player.get(), result, displayItem, this);
    }

    private ItemStack getNextStack() {
        Inventory handler = blockEntity.getAttachedInventory();
        if (handler != null) {
            int slot = InventoryHelper.getFirstOccupiedSlot(handler);
            if (slot >= 0) {
                return handler.removeStack(slot, handler.getStack(slot).getCount());
            }
        }

        return ItemStack.EMPTY;
    }

    FakePlayerHelper.UsefulFakePlayer getPlayer() {
        return player.get();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag cmp = super.toTag();
        cmp.put(NBT_DISPLAY, displayItem.toTag());
        cmp.putBoolean(NBT_SNEAK, isSneaking);
        cmp.putBoolean(NBT_RIGHT, rightClick);
        cmp.putInt(NBT_ROTATION, rotation);
        return cmp;
    }

    @Override
    public void fromTag(CompoundTag cmp) {
        super.fromTag(cmp);
        if (cmp.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(cmp.getCompound(NBT_DISPLAY));
        if (cmp.contains(NBT_SNEAK)) isSneaking = cmp.getBoolean(NBT_SNEAK);
        if (cmp.contains(NBT_RIGHT)) rightClick = cmp.getBoolean(NBT_RIGHT);
        if (cmp.contains(NBT_ROTATION)) rotation = cmp.getInt(NBT_ROTATION);
    }

    //@Override
    public void accept(ItemStack stack) {
        displayItem = stack;
    }
}
