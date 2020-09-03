package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.util.fake.FakePlayer;
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
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class TeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_tele");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_tele");
    private static final Text NAME = new TranslatableText("modularitemframe.module.tele");

    private static final String NBT_LINK = "linked_pos";
    private static final String NBT_DIM = "linked_dim";

    private BlockPos linkedLoc = null;
    private Identifier linkedDim = null;

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
    public Text getName() {
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
            World targetWorld = getDimWorld(world);
            if (!isInRange((ModularFrameEntity) targetWorld.getBlockEntity(linkedLoc), linkedDim.compareTo(world.getRegistryKey().getValue()) != 0)) {
                breakLink(world);
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (player instanceof FakePlayer) return ActionResult.FAIL;

        if (!world.isClient && linkedLoc != null) {
            BlockPos target = getTargetLocation(world);
            if (target == null) {
                player.sendMessage(new TranslatableText("modularitemframe.message.teleport.location_blocked"), false);
                return ActionResult.FAIL;
            }

            if (player.hasPassengers()) {
                player.removeAllPassengers();
            }

            if (player.hasVehicle()) {
                player.stopRiding();
            }

            World targetWorld = getDimWorld(world);
            double offset = targetWorld.getBlockState(linkedLoc).get(ModularFrameBlock.FACING) == Direction.UP ? 0.15 : 0;
            if (!world.getRegistryKey().getValue().equals(targetWorld.getRegistryKey().getValue())){
                player.moveToWorld((ServerWorld) targetWorld);
            }
            player.teleport(target.getX() + 0.5D, target.getY() + offset, target.getZ() + 0.5D);
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
            player.sendMessage(new TranslatableText("modularitemframe.message.teleport.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                Identifier dim = new Identifier(nbt.getString(NBT_DIM));
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                World targetWorld = getDimWorld(world, dim);
                BlockEntity targetTile = targetWorld.getBlockEntity(tmp);

                if (!(targetTile instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetTile).getModule() instanceof TeleportModule))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.teleport.invalid_target"), false);
                    return;
                }
                ModularFrameEntity targetFrame = (ModularFrameEntity) targetTile;

                if (!isInRange(targetFrame, dim.compareTo(world.getRegistryKey().getValue()) != 0)) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.teleport.too_far"), false);
                    return;
                }

                breakLink(world);
                linkedLoc = tmp;
                linkedDim = dim;

                TeleportModule targetModule = (TeleportModule) targetFrame.getModule();
                targetModule.breakLink(world);
                targetModule.linkedLoc = frame.getPos();
                targetModule.linkedDim = world.getRegistryKey().getValue();

                player.sendMessage(new TranslatableText("modularitemframe.message.teleport.link_established"), false);
                nbt.remove(NBT_LINK);
                nbt.remove(NBT_DIM);
                driver.setTag(nbt);

                targetTile.markDirty();
                markDirty();
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
        if (linkedDim != null) {
            tag.putString(NBT_DIM, linkedDim.toString());
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        linkedLoc = tag.contains(NBT_LINK) ? BlockPos.fromLong(tag.getLong(NBT_LINK)) : null;
        linkedDim = tag.contains(NBT_DIM) ? Identifier.tryParse(tag.getString(NBT_DIM)) : null;
    }

    private BlockPos getTargetLocation(World world) {
        World targetWorld = getDimWorld(world);
        if (targetWorld.getBlockState(linkedLoc).get(ModularFrameBlock.FACING) == Direction.DOWN) {
            BlockPos pos2 = linkedLoc.offset(Direction.DOWN);
            if (!targetWorld.getBlockState(pos2).getMaterial().blocksMovement())
                return linkedLoc;
        } else {
            BlockPos pos2 = linkedLoc.offset(Direction.DOWN);
            if (!targetWorld.getBlockState(pos2).getMaterial().blocksMovement())
                return pos2;
            pos2 = linkedLoc.offset(Direction.UP);
            if (!targetWorld.getBlockState(pos2).getMaterial().blocksMovement())
                return linkedLoc;
        }

        return null;
    }

    private void breakLink(World world) {
        if (linkedLoc != null) {
            World targetWorld = getDimWorld(world);
            BlockEntity be = targetWorld.getBlockEntity(linkedLoc);
            if (be instanceof ModularFrameEntity && ((ModularFrameEntity) be).getModule() instanceof TeleportModule) {
                TeleportModule targetModule = (TeleportModule) ((ModularFrameEntity) be).getModule();
                targetModule.linkedLoc = null;
                targetModule.linkedDim = null;
                targetModule.markDirty();
            }

            linkedLoc = null;
            linkedDim = null;
            markDirty();
        }
    }

    private World getDimWorld(World sourceWorld) {
        return getDimWorld(sourceWorld, linkedDim);
    }

    private World getDimWorld(World sourceWorld, Identifier targetId) {
        World targetWorld = sourceWorld;
        if (targetId != null && targetId.compareTo(sourceWorld.getRegistryKey().getValue()) != 0) {
            targetWorld = sourceWorld.getServer().getWorld(RegistryKey.of(Registry.DIMENSION, targetId));
        }
        return targetWorld;
    }

    private boolean isInRange(ModularFrameEntity targetFrame, boolean isCrossDim) {
        if (targetFrame.hasInfinity() && frame.hasInfinity()) {
            return true;
        } else if (!isCrossDim) {
            int sourceRange = ModularItemFrame.getConfig().teleportRange + (frame.getRangeUpCount() * 10);
            int targetRange = ModularItemFrame.getConfig().teleportRange + (targetFrame.getRangeUpCount() * 10);
            return (frame.hasInfinity() || frame.getPos().isWithinDistance(targetFrame.getPos(), sourceRange)) &&
                    (targetFrame.hasInfinity() || targetFrame.getPos().isWithinDistance(frame.getPos(), targetRange));
        }

        return false;
    }
}
