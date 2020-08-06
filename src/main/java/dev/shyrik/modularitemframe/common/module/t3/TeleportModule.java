package dev.shyrik.modularitemframe.common.module.t3;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayer;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.client.helper.EnderRenderHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.TeleportPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.Objects;

public class TeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_tele");

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
        return new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_item");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.tele");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        ImmutableList<EnderRenderHelper.EndRenderFace> faces =
                ImmutableList.of(
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.08f, 0.14f, Direction.UP),
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.92f, 0.14f, Direction.DOWN),
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.92f, 0.14f, Direction.NORTH),
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.08f, 0.14f, Direction.SOUTH),
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.08f, 0.14f, Direction.EAST),
                        new EnderRenderHelper.EndRenderFace(0.85f, 0.92f, 0.14f, Direction.WEST)
                );

        EnderRenderHelper.render(blockEntity, matrixStack, buffer, renderer.getDispatcher().camera.getPos(), faces);
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        if (linkedLoc != null) {
            if (!blockEntity.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().teleportRange + (blockEntity.getRangeUpCount() * 10))) {
                linkedLoc = null;
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (player instanceof FakePlayer) return ActionResult.FAIL;

        if (!world.isClient) {
            if (hasValidConnection(world, player)) {
                BlockPos target;
                if (blockEntity.blockFacing().getAxis().isHorizontal() || blockEntity.blockFacing() == Direction.UP)
                    target = linkedLoc.offset(Direction.DOWN);
                else target = linkedLoc;

                if (player.hasPassengers()) {
                    player.removeAllPassengers();
                }

                player.stopRiding();

                if (player.teleport(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, true)) {
                    NetworkHandler.sendAround(world, player.getBlockPos(), 32, new TeleportPacket(player.getBlockPos()));
                    NetworkHandler.sendAround(world, target, 32, new TeleportPacket(target));
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        CompoundTag nbt = driver.getTag();
        if (player.isSneaking()) {
            if (nbt == null) nbt = new CompoundTag();
            nbt.putLong(NBT_LINK, blockEntity.getPos().asLong());
            driver.setTag(nbt);
            player.sendMessage(new TranslatableText("modularitemframe.message.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                if (blockEntity.getPos().isWithinDistance(tmp, 1)) return;
                BlockEntity targetTile = blockEntity.getWorld().getBlockEntity(tmp);
                int countRange = blockEntity.getRangeUpCount();
                if (!(targetTile instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetTile).module instanceof TeleportModule)))
                    player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), true);
                else if (!blockEntity.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().teleportRange + (countRange * 10))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().teleportRange + (countRange * 10)), true);
                } else {
                    linkedLoc = tmp;
                    ((TeleportModule) ((ModularFrameEntity) targetTile).module).linkedLoc = blockEntity.getPos();
                    player.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);
                }
            }
        }
    }

    private boolean isTargetLocationValid(World world) {
        if (blockEntity.blockFacing().getAxis().isHorizontal() || blockEntity.blockFacing() == Direction.UP)
            return world.isAir(linkedLoc.offset(Direction.DOWN));
        else return world.isAir(linkedLoc.offset(Direction.UP));
    }

    private boolean hasValidConnection(World world, PlayerEntity player) {
        if (linkedLoc == null) {
            if (player != null) player.sendMessage(new TranslatableText("modularitemframe.message.no_target"), false);
            return false;
        }
        BlockEntity targetTile = world.getBlockEntity(linkedLoc);
        if (!(targetTile instanceof ModularFrameEntity) || !(((ModularFrameEntity) targetTile).module instanceof TeleportModule)) {
            if (player != null)
                player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), false);
            return false;
        }
        if (!isTargetLocationValid(world)) {
            if (player != null)
                player.sendMessage(new TranslatableText("modularitemframe.message.location_blocked"), false);
            return false;
        }
        return true;
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        if (hasValidConnection(world, null)) {
            ((TeleportModule) ((ModularFrameEntity) Objects.requireNonNull(world.getBlockEntity(linkedLoc))).module).linkedLoc = null;
        }
        super.onRemove(world, pos, facing, player);
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
    }
}
