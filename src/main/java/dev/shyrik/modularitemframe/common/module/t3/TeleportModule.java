package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public class TeleportModule extends ModuleBase {

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t3_tele");

    private static final String NBT_LINK = "linked_pos";
    private static final String NBT_LINKX = "linked_posX";
    private static final String NBT_LINKY = "linked_posY";
    private static final String NBT_LINKZ = "linked_posZ";

    private BlockPos linkedLoc = null;

    @Override
    public Identifier getId() {
        return LOC;
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
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks,  IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        BlockPos pos = blockEntity.getPos();
        FrameEnderRenderer.render(matrixStack, buffer, pos, renderer.getDispatcher().renderInfo.getProjectedView(), info -> {
            float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            switch (blockEntity.blockFacing()) {
                case DOWN:
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.08f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.08f, z + 0.14f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.08f, z + 0.14f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.08f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
                case UP:
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.92f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.92f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.16f, y + 0.92f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.16f, y + 0.92f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
                case NORTH:
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.85f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.85f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.14f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.14f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
                case SOUTH:
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.85f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.85f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.85f, y + 0.14f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.14f, y + 0.14f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
                case WEST:
                    info.buffer.pos(info.matrix,x + 0.08f, y + 0.85f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.08f, y + 0.85f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.08f, y + 0.16f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.08f, y + 0.16f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
                case EAST:
                    info.buffer.pos(info.matrix,x + 0.92f, y + 0.85f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.92f, y + 0.85f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.92f, y + 0.16f, z + 0.16f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    info.buffer.pos(info.matrix,x + 0.92f, y + 0.16f, z + 0.85f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
                    break;
            }
            return true;
        });
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        if (linkedLoc != null) {
            if (!blockEntity.getPos().isWithinDistance(linkedLoc, ConfigValues.BaseTeleportRange + (blockEntity.getRangeUpCount() * 10))) {
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

                if (player.isBeingRidden()) { // kp ob es das so gibt wie das hier gemeint ist beim neuen player
                    player.removePassengers();
                }

                player.stopRiding();

                if (player.teleport(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, true)) {
                    NetworkHandler.sendAround(new TeleportEffectPacket(player.getPos()), world, player.getPos(), 32);
                    NetworkHandler.sendAround(new TeleportEffectPacket(target), world, target, 32);
                }
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity playerIn, ItemStack driver) {
        CompoundTag nbt = driver.getTag();
        if (playerIn.isSneaking()) {
            if (nbt == null) nbt = new CompoundTag();
            nbt.putLong(NBT_LINK, blockEntity.getPos().asLong());
            driver.setTag(nbt);
            playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.loc_saved"));
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                if (blockEntity.getPos().isWithinDistance(tmp, 1)) return;
                BlockEntity targetTile = blockEntity.getWorld().getBlockEntity(tmp);
                int countRange = blockEntity.getRangeUpCount();
                if (!(targetTile instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetTile).module instanceof TeleportModule)))
                    playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.invalid_target"));
                else if (!blockEntity.getPos().withinDistance(tmp, ConfigValues.BaseTeleportRange + (countRange * 10))) {
                    playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.too_far", ConfigValues.BaseTeleportRange + (countRange * 10)));
                } else {
                    linkedLoc = tmp;
                    ((TeleportModule) ((ModularFrameEntity) targetTile).module).linkedLoc = blockEntity.getPos();
                    playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.link_established"));
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);
                }
            }
        }
    }

    private boolean isTargetLocationValid( World worldIn) {
        if (blockEntity.blockFacing().getAxis().isHorizontal() || blockEntity.blockFacing() == Direction.UP)
            return worldIn.isAir(linkedLoc.offset(Direction.DOWN));
        else return worldIn.isAir(linkedLoc.offset(Direction.UP));
    }

    private boolean hasValidConnection( World world, PlayerEntity player) {
        if (linkedLoc == null) {
            if (player != null) player.sendMessage(new TranslationTextComponent("modularitemframe.message.no_target"));
            return false;
        }
        BlockEntity targetTile = world.getBlockEntity(linkedLoc);
        if (!(targetTile instanceof ModularFrameEntity) || !(((ModularFrameEntity) targetTile).module instanceof TeleportModule)) {
            if (player != null)
                player.sendMessage(new TranslationTextComponent("modularitemframe.message.invalid_target"));
            return false;
        }
        if (!isTargetLocationValid(world)) {
            if (player != null)
                player.sendMessage(new TranslationTextComponent("modularitemframe.message.location_blocked"));
            return false;
        }
        return true;
    }

    @Override
    public void onRemove( World worldIn, BlockPos pos,  Direction facing,  PlayerEntity playerIn) {
        if (hasValidConnection(worldIn, null)) {
            ((TeleportModule) ((ModularFrameEntity) Objects.requireNonNull(worldIn.getBlockEntity(linkedLoc))).module).linkedLoc = null;
        }
        super.onRemove(worldIn, pos, facing, playerIn);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = super.toTag();
        if (linkedLoc != null) {
            compound.putInt(NBT_LINKX, linkedLoc.getX());
            compound.putInt(NBT_LINKY, linkedLoc.getY());
            compound.putInt(NBT_LINKZ, linkedLoc.getZ());
        }
        return compound;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.hasUniqueId(NBT_LINKX)) // was ist das für eine Prüfung gewesen? ob eine ID drin ist die unique ist oder ob genau diese id drin ist oder was anderes?
            linkedLoc = new BlockPos(nbt.getInt(NBT_LINKX), nbt.getInt(NBT_LINKY), nbt.getInt(NBT_LINKZ));
    }
}
