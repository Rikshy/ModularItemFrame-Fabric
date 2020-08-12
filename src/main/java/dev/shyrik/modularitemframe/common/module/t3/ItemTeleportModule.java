package dev.shyrik.modularitemframe.common.module.t3;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.client.helper.EnderRenderHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class ItemTeleportModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_itemtele");
    public static final Identifier BG_IN = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemtelein");
    public static final Identifier BG_OUT = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemteleout");
    public static final Identifier BG_NONE = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemtelenone");

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
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    public List<Identifier> getVariantFronts() {
        return ImmutableList.of(BG_NONE, BG_IN, BG_OUT);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        if(direction == EnumMode.NONE) {
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
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.itemtele");
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        if (linkedLoc != null) {
            if (!blockEntity.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().teleportRange + (blockEntity.getRangeUpCount() * 10))) {
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
            nbt.putLong(NBT_LINK, blockEntity.getPos().asLong());
            driver.setTag(nbt);
            player.sendMessage(new TranslatableText("modularitemframe.message.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                BlockEntity targetBlockEntity = world.getBlockEntity(tmp);
                int countRange = blockEntity.getRangeUpCount();
                if (!(targetBlockEntity instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetBlockEntity).getModule() instanceof ItemTeleportModule)))
                    player.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), false);
                else if (!blockEntity.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().teleportRange + (countRange * 10))) {
                    player.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().teleportRange + (countRange * 10)), false);
                } else {
                    linkedLoc = tmp;
                    direction = EnumMode.DISPENSE;

                    ItemTeleportModule targetModule = (ItemTeleportModule) ((ModularFrameEntity) targetBlockEntity).getModule();
                    targetModule.linkedLoc = blockEntity.getPos();
                    targetModule.direction = EnumMode.VACUUM;

                    player.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);

                    targetBlockEntity.markDirty();
                    blockEntity.markDirty();
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
            held.setCount(0);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        if (hasValidConnection(world)) {
            ItemTeleportModule targetModule = (ItemTeleportModule) ((ModularFrameEntity) Objects.requireNonNull(world.getBlockEntity(linkedLoc))).getModule();
            targetModule.linkedLoc = null;
            targetModule.direction = EnumMode.NONE;
        }
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient) return;
        if (direction != EnumMode.VACUUM) return;
        if (!hasValidConnection(world)) return;
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

        List<ItemEntity> entities = world.getEntitiesByClass(ItemEntity.class, getVacuumBox(pos), itemEntity -> true);
        for (ItemEntity entity : entities) {
            ItemStack entityStack = entity.getStack();
            if (!entity.isAlive() || entityStack.isEmpty()) continue;

            ItemHelper.ejectStack(world, linkedLoc, world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), entityStack);
            entity.remove();
            NetworkHandler.sendAround(
                    world,
                    blockEntity.getPos(),
                    32,
                    new SpawnParticlesPacket(ParticleTypes.EXPLOSION, entity.getBlockPos(), 1));
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
            tag.putInt(NBT_DIR, direction.index);
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_LINK_X))
            linkedLoc = new BlockPos(tag.getInt(NBT_LINK_X), tag.getInt(NBT_LINK_Y), tag.getInt(NBT_LINK_Z));
        if (tag.contains(NBT_DIR)) direction = EnumMode.values()[tag.getInt(NBT_DIR)];
    }

    private boolean hasValidConnection(World world) {
        if (linkedLoc == null) return false;
        BlockEntity blockEntity = world.getBlockEntity(linkedLoc);
        if (!(blockEntity instanceof ModularFrameEntity)
                || !(((ModularFrameEntity) blockEntity).getModule() instanceof ItemTeleportModule)
                || ((ItemTeleportModule) ((ModularFrameEntity) blockEntity).getModule()).direction != EnumMode.DISPENSE)
            return false;
        return true;
    }

    private Box getVacuumBox(BlockPos pos) {
        int range = ModularItemFrame.getConfig().vacuumRange + blockEntity.getRangeUpCount();
        switch (blockEntity.blockFacing()) {
            case DOWN:
                return new Box(pos.add(-5, 0, -5), pos.add(5, -5, 5));
            case UP:
                return new Box(pos.add(-5, 0, -5), pos.add(5, 5, 5));
            case NORTH:
                return new Box(pos.add(-5, -5, 0), pos.add(5, 5, -5));
            case SOUTH:
                return new Box(pos.add(-5, -5, 0), pos.add(5, 5, 5));
            case WEST:
                return new Box(pos.add(0, -5, -5), pos.add(5, 5, 5));
            case EAST:
                return new Box(pos.add(0, -5, -5), pos.add(-5, 5, 5));
        }
        return new Box(pos, pos.add(1, 1, 1));
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

        public int getIndex() {
            return this.index;
        }

        @Environment(EnvType.CLIENT)
        public String getName() {
            return I18n.translate(this.name);
        }
    }

}
