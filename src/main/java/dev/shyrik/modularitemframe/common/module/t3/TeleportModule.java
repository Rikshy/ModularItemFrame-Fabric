package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayer;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_tele");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_tele");
    private static final Text NAME = new TranslatableText("modularitemframe.module.tele");

    private static final String NBT_LINK = "linked_pos";
    private static final String NBT_LINK_X = "linked_posX";
    private static final String NBT_LINK_Y = "linked_posY";
    private static final String NBT_LINK_Z = "linked_posZ";

    private BlockPos linkedLoc = null;

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
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getModuleName() {
        return NAME;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        if (linkedLoc != null) {
            renderer.renderEnder(frame, matrixStack, buffer, 0.85f, 0.08f, 0.14f);
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        if (linkedLoc != null) {
            if (!frame.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().teleportRange + (frame.getRangeUpCount() * 10))) {
                linkedLoc = null;
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (player instanceof FakePlayer) return ActionResult.FAIL;

        if (!world.isClient) {
            if (hasValidConnection(world, player)) {
                BlockPos target = getTargetLocation(world);
                if (target == null) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.location_blocked"), false);
                    return ActionResult.FAIL;
                }

                if (player.hasPassengers()) {
                    player.removeAllPassengers();
                }

                if (player.hasVehicle()) {
                    player.stopRiding();
                }

                double offset = world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING) == Direction.UP ? 0.15 : 0;

                player.requestTeleport(target.getX() + 0.5D, target.getY() + offset, target.getZ() + 0.5D);
                player.fallDistance = 0.0F;
                world.playSound(null, target, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1F, 1F);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        CompoundTag nbt = driver.getTag();
        if (player.isSneaking()) {
            if (nbt == null) nbt = new CompoundTag();
            nbt.putLong(NBT_LINK, frame.getPos().asLong());
            driver.setTag(nbt);
            player.sendMessage(new TranslatableText("modularitemframe.message.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                if (frame.getPos().isWithinDistance(tmp, 1)) return;
                BlockEntity targetTile = world.getBlockEntity(tmp);
                int countRange = frame.getRangeUpCount();
                if (!(targetTile instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetTile).getModule() instanceof TeleportModule)))
                    player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), true);
                else if (!frame.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().teleportRange + (countRange * 10))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().teleportRange + (countRange * 10)), true);
                } else {
                    linkedLoc = tmp;
                    ((TeleportModule) ((ModularFrameEntity) targetTile).getModule()).linkedLoc = frame.getPos();
                    targetTile.markDirty();
                    player.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);
                }
            }
        }
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        if (linkedLoc != null) {
            BlockEntity targetTile = world.getBlockEntity(linkedLoc);
            if (targetTile instanceof ModularFrameEntity && ((ModularFrameEntity) targetTile).getModule() instanceof TeleportModule) {
                TeleportModule farMod = ((TeleportModule)((ModularFrameEntity) targetTile).getModule());
                farMod.linkedLoc = null;
                farMod.markDirty();
            }
        }
        super.onRemove(world, pos, facing, player, moduleStack);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        if (linkedLoc != null) {
            tag.putInt(NBT_LINK_X, linkedLoc.getX());
            tag.putInt(NBT_LINK_Y, linkedLoc.getY());
            tag.putInt(NBT_LINK_Z, linkedLoc.getZ());
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_LINK_X))
            linkedLoc = new BlockPos(tag.getInt(NBT_LINK_X), tag.getInt(NBT_LINK_Y), tag.getInt(NBT_LINK_Z));
        else linkedLoc = null;
    }

    private BlockPos getTargetLocation(World world) {

        if (world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING) == Direction.DOWN) {
            BlockPos pos2 = linkedLoc.offset(Direction.DOWN);
            if (!world.getBlockState(pos2).getMaterial().blocksMovement())
                return linkedLoc;
        } else {
            BlockPos pos2 = linkedLoc.offset(Direction.DOWN);
            if (!world.getBlockState(pos2).getMaterial().blocksMovement())
                return pos2;
            pos2 = linkedLoc.offset(Direction.UP);
            if (!world.getBlockState(pos2).getMaterial().blocksMovement())
                return linkedLoc;
        }

        return null;
    }

    private boolean hasValidConnection(World world, PlayerEntity player) {
        if (linkedLoc == null) {
            if (player != null) player.sendMessage(new TranslatableText("modularitemframe.message.no_target"), false);
            return false;
        }
        BlockEntity targetTile = world.getBlockEntity(linkedLoc);
        if (!(targetTile instanceof ModularFrameEntity) || !(((ModularFrameEntity) targetTile).getModule() instanceof TeleportModule)) {
            if (player != null)
                player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), false);
            return false;
        }
        return true;
    }
}
