package dev.shyrik.modularitemframe.common.module.t3;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import dev.shyrik.modularitemframe.common.network.packet.SpawnParticlesPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class ItemTeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_itemtele");
    public static final Identifier BG_IN = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemtelein");
    public static final Identifier BG_OUT = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemteleout");
    public static final Identifier BG_NONE = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_itemtelenone");

    private static final String NBT_LINK = "item_linked_pos";
    private static final String NBT_LINK_X = "linked_posX";
    private static final String NBT_LINK_Y = "linked_posY";
    private static final String NBT_LINK_Z = "linked_posZ";
    private static final String NBT_DIR = "direction";

    private BlockPos linkedLoc = null;
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
        return ImmutableList.of(BG_NONE, BG_IN, BG_OUT);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.itemtele");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        if(direction == EnumMode.NONE) {
            renderer.renderEnder(frame, matrixStack, buffer, 0.70f, 0.08f, 0.30f);
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        if (linkedLoc != null) {
            if (!frame.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().teleportRange + (frame.getRangeUpCount() * 10))) {
                linkedLoc = null;
                direction = EnumMode.NONE;
            }
        }
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
                BlockEntity targetBlockEntity = world.getBlockEntity(tmp);
                int countRange = frame.getRangeUpCount();
                if (!(targetBlockEntity instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetBlockEntity).getModule() instanceof ItemTeleportModule)))
                    player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), false);
                else if (!frame.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().teleportRange + (countRange * 10))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().teleportRange + (countRange * 10)), false);
                } else {
                    linkedLoc = tmp;
                    direction = EnumMode.DISPENSE;

                    ItemTeleportModule targetModule = (ItemTeleportModule) ((ModularFrameEntity) targetBlockEntity).getModule();
                    targetModule.linkedLoc = frame.getPos();
                    targetModule.direction = EnumMode.VACUUM;

                    player.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);

                    targetBlockEntity.markDirty();
                    markDirty();
                }
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (direction != EnumMode.VACUUM) return ActionResult.FAIL;
        if (!hasValidConnection(world)) return ActionResult.FAIL;

        ItemStack held = player.getStackInHand(hand);

        if (!held.isEmpty()) {
            ItemHelper.ejectStack(world, linkedLoc, world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), held);
            NetworkHandler.sendAround(world, linkedLoc, 32,
                    new PlaySoundPacket(linkedLoc, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS));
            held.setCount(0);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        if (!world.isClient && hasValidConnection(world)) {
            ItemTeleportModule targetModule = (ItemTeleportModule) ((ModularFrameEntity) Objects.requireNonNull(world.getBlockEntity(linkedLoc))).getModule();
            targetModule.linkedLoc = null;
            targetModule.direction = EnumMode.NONE;
            targetModule.markDirty();
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

            ItemHelper.ejectStack(world, linkedLoc, world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), entityStack);
            NetworkHandler.sendAround(world, linkedLoc, 32,
                    new PlaySoundPacket(linkedLoc, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS));
            entity.remove();
            NetworkHandler.sendAround(
                    world,
                    frame.getPos(),
                    32,
                    new SpawnParticlesPacket(ParticleTypes.POOF, entity.getBlockPos(), 8));
            break;
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        if (linkedLoc != null) {
            tag.putInt(NBT_LINK_X, linkedLoc.getX());
            tag.putInt(NBT_LINK_Y, linkedLoc.getY());
            tag.putInt(NBT_LINK_Z, linkedLoc.getZ());
        }
        tag.putInt(NBT_DIR, direction.index);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_LINK_X))
            linkedLoc = new BlockPos(tag.getInt(NBT_LINK_X), tag.getInt(NBT_LINK_Y), tag.getInt(NBT_LINK_Z));
        else linkedLoc = null;
        if (tag.contains(NBT_DIR)) direction = EnumMode.values()[tag.getInt(NBT_DIR)];
    }

    private boolean hasValidConnection(World world) {
        if (linkedLoc == null) return false;
        BlockEntity blockEntity = world.getBlockEntity(linkedLoc);
        return blockEntity instanceof ModularFrameEntity
                && ((ModularFrameEntity) blockEntity).getModule() instanceof ItemTeleportModule
                && ((ItemTeleportModule) ((ModularFrameEntity) blockEntity).getModule()).direction != direction;
    }

    public enum EnumMode {
        VACUUM(0, "modularitemframe.mode.in"),
        DISPENSE(1, "modularitemframe.mode.out"),
        NONE(2, "modularitemframe.mode.no");

        private final int index;
        private final String name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = nameIn;
        }

        @Environment(EnvType.CLIENT)
        public String getName() {
            return I18n.translate(this.name);
        }
    }

}
