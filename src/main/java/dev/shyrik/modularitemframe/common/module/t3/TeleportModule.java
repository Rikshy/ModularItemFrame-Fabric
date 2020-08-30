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
    private static final String NBT_DIM = "item_linked_dim";

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
    public void onFrameUpgradesChanged(World world, BlockPos pos, Direction facing) {
        if (linkedLoc != null) {
            if (!frame.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().teleportRange + (frame.getRangeUpCount() * 10))) {
                breakLink(world);
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (player instanceof FakePlayer) return ActionResult.FAIL;

        if (!world.isClient) {
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
        return ActionResult.SUCCESS;
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        CompoundTag nbt = driver.getTag();
        if (player.isSneaking()) {
            if (nbt == null) nbt = new CompoundTag();
            nbt.putLong(NBT_LINK, frame.getPos().asLong());
            nbt.putString(NBT_DIM, world.getRegistryKey().getValue().toString());
            driver.setTag(nbt);
            player.sendMessage(new TranslatableText("modularitemframe.message.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                Identifier dim = new Identifier(nbt.getString(NBT_DIM));
                if (dim.compareTo(world.getRegistryKey().getValue()) != 0) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.teleport.wrong_dim"), false);
                    return;
                }
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                BlockEntity targetTile = world.getBlockEntity(tmp);
                int countRange = frame.getRangeUpCount();
                if (!(targetTile instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetTile).getModule() instanceof TeleportModule)))
                    player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), true);
                else if (!frame.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().teleportRange + (countRange * 10))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().teleportRange + (countRange * 10)), true);
                } else {
                    breakLink(world);
                    linkedLoc = tmp;

                    TeleportModule targetModule = (TeleportModule) ((ModularFrameEntity) targetTile).getModule();
                    targetModule.breakLink(world);
                    targetModule.linkedLoc = frame.getPos();
                    targetTile.markDirty();

                    player.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);

                    markDirty();
                }
            }
        }
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        if (!world.isClient) {
            breakLink(world);
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        if (linkedLoc != null) {
            tag.putLong(NBT_LINK, linkedLoc.asLong());
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        linkedLoc = tag.contains(NBT_LINK) ? BlockPos.fromLong(tag.getLong(NBT_LINK)) : null;
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

    private void breakLink(World world) {
        if (linkedLoc != null) {
            BlockEntity be = world.getBlockEntity(linkedLoc);
            if (be instanceof ModularFrameEntity && ((ModularFrameEntity) be).getModule() instanceof TeleportModule) {
                TeleportModule targetModule = (TeleportModule) ((ModularFrameEntity) be).getModule();
                targetModule.linkedLoc = null;
                targetModule.markDirty();
            }

            linkedLoc = null;
            markDirty();
        }
    }
}
