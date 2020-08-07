package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.mojang.authlib.GameProfile;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayer;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayerHelper;
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
import net.minecraft.text.TranslatableText;
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
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_use");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");

    private static final GameProfile DEFAULT_CLICKER = new GameProfile(UUID.nameUUIDFromBytes("modularitemframe".getBytes()), "[Frame Clicker]");

    private static final String NBT_DISPLAY = "display";
    private static final String NBT_ROTATION = "rotation";
    private static final String NBT_SNEAK = "sneaking";
    private static final String NBT_RIGHT = "right_click";

    private boolean isSneaking = false;
    private boolean rightClick = false;
    private int rotation = 0;
    private ItemStack displayItem = ItemStack.EMPTY;
    private WeakReference<FakePlayer> player;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Identifier frontTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        Direction facing = blockEntity.blockFacing();
        switch (facing) {
            case DOWN:
            case NORTH:
                ItemRenderHelper.renderOnFrame(displayItem, Direction.WEST, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case UP:
            case SOUTH:
                ItemRenderHelper.renderOnFrame(displayItem, Direction.EAST, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case WEST:
                matrixStack.multiply(new Quaternion(0, 90.0F, 0.0F, true));
                matrixStack.translate(-1, 0 ,0);
                ItemRenderHelper.renderOnFrame(displayItem, Direction.WEST, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
            case EAST:
                matrixStack.multiply(new Quaternion(0, 90.0F, 0.0F, true));
                matrixStack.translate(-1, 0 ,0);
                ItemRenderHelper.renderOnFrame(displayItem, Direction.EAST, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, combinedLight, combinedOverlay);
                break;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.use");
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing,  PlayerEntity player) {
        if (!world.isClient) ItemHelper.ejectStack(world, pos, facing, displayItem);
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isEmpty()) {
                player.setStackInHand(hand, displayItem.copy());
                displayItem.setCount(0);
            } else {
                if (displayItem.isEmpty()) {
                    displayItem = held.copy();
                    player.setStackInHand(hand, ItemStack.EMPTY);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            if (player.isSneaking()) {
                isSneaking = !isSneaking;
            } else {
                rightClick = !rightClick;
            }
            String mode = isSneaking ? I18n.translate("modularitemframe.mode.sn") + " + " : "";
            mode += rightClick ? I18n.translate("modularitemframe.mode.rc") : I18n.translate("modularitemframe.mode.lc");

            player.sendMessage(new TranslatableText("modularitemframe.message.mode_change", mode), false);
            blockEntity.markDirty();
        }
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (!world.isClient) {
            if (displayItem.isEmpty()) {
                displayItem = getNextStack();
                rotation = 0;
            } else {
                if (rotation >= 360) {
                    rotation -= 360;
                    hitIt(world, pos);
                }
                rotation += 15 * (blockEntity.getSpeedUpCount() + 1);
            }
            blockEntity.markDirty();
        }
    }

    private void hitIt(World world, BlockPos pos) {
        if (player == null) player = new WeakReference<>(FakePlayerHelper.getPlayer(world, DEFAULT_CLICKER));

//        Direction facing = blockEntity.blockFacing();
//        FakePlayerHelper.setupFakePlayerForUse(getPlayer(), pos, facing, displayItem, isSneaking);
//        ItemStack result;
//        if (rightClick)
//            result = FakePlayerHelper.rightClickInDirection(getPlayer(), world, pos.offset(facing), facing, world.getBlockState(pos), 2 + blockEntity.getRangeUpCount());
//        else
//            result = FakePlayerHelper.leftClickInDirection(getPlayer(), world, pos.offset(facing), facing, world.getBlockState(pos), 2 + blockEntity.getRangeUpCount());
//        FakePlayerHelper.cleanupFakePlayerFromUse(player.get(), result, displayItem, stack -> displayItem = stack);

        // moddev: call blockState.onUse on the given block
    }

    private ItemStack getNextStack() {
        FixedItemInv handler = blockEntity.getAttachedInventory();
        if (handler != null) {
            if (handler.getExtractable().couldExtractAnything()) {
                return handler.getExtractable().extract(1);
            }
        }

        return ItemStack.EMPTY;
    }

    FakePlayer getPlayer() {
        return player.get();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        tag.putBoolean(NBT_SNEAK, isSneaking);
        tag.putBoolean(NBT_RIGHT, rightClick);
        tag.putInt(NBT_ROTATION, rotation);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(tag.getCompound(NBT_DISPLAY));
        if (tag.contains(NBT_SNEAK)) isSneaking = tag.getBoolean(NBT_SNEAK);
        if (tag.contains(NBT_RIGHT)) rightClick = tag.getBoolean(NBT_RIGHT);
        if (tag.contains(NBT_ROTATION)) rotation = tag.getInt(NBT_ROTATION);
    }
}
