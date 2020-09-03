package dev.shyrik.modularitemframe.common.module.t3;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
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

import java.util.List;

public class ItemTeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_itemtele");
    public static final Identifier BG_IN = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemtelein");
    public static final Identifier BG_OUT = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemteleout");
    public static final Identifier BG_NONE = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemtelenone");
    private static final Text NAME = new TranslatableText("modularitemframe.module.itemtele");

    private final List<Identifier> frontTex = ImmutableList.of(
            BG_NONE, BG_IN, BG_OUT
    );

    private static final String NBT_LINK = "item_linked_pos";
    private static final String NBT_DIM = "item_linked_dim";
    private static final String NBT_DIR = "direction";

    private BlockPos linkedLoc = null;
    private Identifier linkedDim = null;
    private EnumMode direction = EnumMode.NONE;

    @Override
    public Identifier  getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        switch (direction) {
            case VACUUM:
                return BG_IN;
            case DISPENSE:
                return BG_OUT;
            case NONE:
                return BG_NONE;
        }
        return BG_NONE;
    }

    @Override
    public List<Identifier> getVariantFronts() {
        return frontTex;
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
    public void appendTooltips(List<Text> tooltips) {
        tooltips.add(new TranslatableText("modularitemframe.tooltip.mode").append(direction.getName()));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        if(direction != EnumMode.NONE) {
            renderer.renderEnder(frame, matrixStack, buffer, 0.625f, 0.063f, 0.375f);
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
                BlockEntity targetBlockEntity = targetWorld.getBlockEntity(tmp);

                if (!(targetBlockEntity instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetBlockEntity).getModule() instanceof ItemTeleportModule))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.teleport.invalid_target"), false);
                    return;
                }

                ModularFrameEntity targetFrame =  (ModularFrameEntity) targetBlockEntity;

                if (!isInRange(targetFrame, dim.compareTo(world.getRegistryKey().getValue()) != 0)){
                    player.sendMessage(new TranslatableText("modularitemframe.message.teleport.too_far"), false);
                    return;
                }

                breakLink(world);
                linkedLoc = tmp;
                linkedDim = dim;
                direction = EnumMode.DISPENSE;

                ItemTeleportModule targetModule = (ItemTeleportModule) targetFrame.getModule();
                targetModule.breakLink(world);
                targetModule.linkedLoc = frame.getPos();
                targetModule.linkedDim = world.getRegistryKey().getValue();
                targetModule.direction = EnumMode.VACUUM;

                player.sendMessage(new TranslatableText("modularitemframe.message.teleport.link_established"), false);
                nbt.remove(NBT_LINK);
                nbt.remove(NBT_DIM);
                driver.setTag(nbt);

                targetModule.markDirty();
                markDirty();
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (world.isClient) return ActionResult.FAIL;
        if (direction != EnumMode.VACUUM) return ActionResult.FAIL;
        if (!hasValidConnection(world)) return ActionResult.FAIL;

        ItemStack held = player.getStackInHand(hand);

        if (!held.isEmpty()) {
            World targetWorld = getDimWorld(world);
            ItemHelper.ejectStack(targetWorld, linkedLoc, targetWorld.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), held.copy());
            world.playSound(player, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
            targetWorld.playSound(null, linkedLoc, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
            targetWorld.playSound(null, linkedLoc, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
            held.setCount(0);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        if (!world.isClient) {
            breakLink(world);
        }
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient || frame.isPowered() || !canTick(world,60, 10)) return;
        if (direction != EnumMode.VACUUM || !hasValidConnection(world)) return;

        List<ItemEntity> entities = world.getEntitiesByClass(ItemEntity.class, getScanBox(), itemEntity -> true);
        for (ItemEntity entity : entities) {
            ItemStack entityStack = entity.getStack();
            if (!frame.getItemFilter().matches(entityStack)) continue;
            if (!entity.isAlive() || entityStack.isEmpty()) continue;

            World targetWorld = getDimWorld(world);
            ItemHelper.ejectStack(targetWorld, linkedLoc, targetWorld.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), entityStack);
            world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
            targetWorld.playSound(null, linkedLoc, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1F, 1F);
            targetWorld.playSound(null, linkedLoc, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
            entity.remove();
            ((ServerWorld) world).spawnParticles(ParticleTypes.POOF, entity.getX() - 0.1, entity.getY(), entity.getZ() - 0.1, 4, 0.2, 0.2, 0.2, 0.07);
            break;
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
        tag.putInt(NBT_DIR, direction.index);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        linkedLoc = tag.contains(NBT_LINK) ? BlockPos.fromLong(tag.getLong(NBT_LINK)) : null;
        linkedDim = tag.contains(NBT_DIM) ? Identifier.tryParse(tag.getString(NBT_DIM)) : null;
        if (tag.contains(NBT_DIR)) direction = EnumMode.values()[tag.getInt(NBT_DIR)];
    }

    private void breakLink(World world) {
        if (linkedLoc != null) {
            BlockEntity be = world.getBlockEntity(linkedLoc);
            if (be instanceof ModularFrameEntity && ((ModularFrameEntity) be).getModule() instanceof ItemTeleportModule) {
                ItemTeleportModule targetModule = (ItemTeleportModule) ((ModularFrameEntity) be).getModule();
                targetModule.linkedLoc = null;
                targetModule.linkedDim = null;
                targetModule.direction = EnumMode.NONE;
                targetModule.markDirty();
            }

            linkedLoc = null;
            linkedDim = null;
            direction = EnumMode.NONE;
            markDirty();
        }
    }

    private boolean hasValidConnection(World world) {
        if (linkedLoc == null) return false;
        World targetWorld = getDimWorld(world);
        BlockEntity blockEntity = targetWorld.getBlockEntity(linkedLoc);
        return blockEntity instanceof ModularFrameEntity
                && ((ModularFrameEntity) blockEntity).getModule() instanceof ItemTeleportModule
                && ((ItemTeleportModule) ((ModularFrameEntity) blockEntity).getModule()).direction != direction;
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

    public enum EnumMode {
        VACUUM(0, "modularitemframe.mode.in"),
        DISPENSE(1, "modularitemframe.mode.out"),
        NONE(2, "modularitemframe.mode.no");

        private final int index;
        private final Text name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = new TranslatableText(nameIn);
        }

        @Environment(EnvType.CLIENT)
        public Text getName() {
            return this.name;
        }
    }

}
