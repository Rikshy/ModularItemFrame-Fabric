package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.api.util.RegistryHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
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

public class ItemTeleporterModule extends ModuleBase {

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t3_itemtele");
    public static final Identifier BG_IN = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemtelein");
    public static final Identifier BG_OUT = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemteleout");
    public static final Identifier BG_NONE = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_itemtelenone");

    private static final String NBT_LINK = "item_linked_pos";
    private static final String NBT_LINKX = "linked_posX";
    private static final String NBT_LINKY = "linked_posY";
    private static final String NBT_LINKZ = "linked_posZ";
    private static final String NBT_DIR = "direction";

    private BlockPos linkedLoc = null;
    private EnumMode direction = EnumMode.NONE;

    @Override
    public Identifier  getId() {
        return LOC;
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
        return ModularFrameBlock.INNER_HARDEST_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        BlockPos pos = blockEntity.getPos();

        if(direction == EnumMode.NONE) {
//            FrameEnderRenderer.render(matrixStack, buffer, pos, renderer.getDispatcher().renderInfo.getProjectedView(), info -> {
//                float x = pos.getX(), y = pos.getY(), z = pos.getZ();
//                switch (blockEntity.blockFacing()) {
//                    case DOWN:
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.08f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.08f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.08f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.08f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                    case UP:
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.92f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.92f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.92f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.92f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                    case NORTH:
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.7f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.7f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.3f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.3f, z + 0.08f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                    case SOUTH:
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.7f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.7f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.7f, y + 0.3f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.3f, y + 0.3f, z + 0.92f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                    case WEST:
//                        info.buffer.pos(info.matrix, x + 0.08f, y + 0.7f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.08f, y + 0.7f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.08f, y + 0.3f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.08f, y + 0.3f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                    case EAST:
//                        info.buffer.pos(info.matrix, x + 0.92f, y + 0.7f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.92f, y + 0.7f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.92f, y + 0.3f, z + 0.3f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        info.buffer.pos(info.matrix, x + 0.92f, y + 0.3f, z + 0.7f).color(info.color1, info.color2, info.color3, 1.0F).endVertex();
//                        break;
//                }
//                return true;
//            });
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
            if (!blockEntity.getPos().isWithinDistance(linkedLoc, ModularItemFrame.getConfig().BaseTeleportRange + (blockEntity.getRangeUpCount() * 10))) {
                linkedLoc = null;
                direction = EnumMode.NONE;
            }
        }
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity playerIn, ItemStack driver) {
        CompoundTag nbt = driver.getTag();
        if (playerIn.isSneaking()) {
            if (nbt == null) nbt = new CompoundTag();
            nbt.putLong(NBT_LINK, blockEntity.getPos().asLong());
            driver.setTag(nbt);
            playerIn.sendMessage(new TranslatableText("modularitemframe.message.loc_saved"), false);
        } else {
            if (nbt != null && nbt.contains(NBT_LINK)) {
                BlockPos tmp = BlockPos.fromLong(nbt.getLong(NBT_LINK));
                BlockEntity targetBlockEntity = blockEntity.getWorld().getBlockEntity(tmp);
                int countRange = blockEntity.getRangeUpCount();
                if (!(targetBlockEntity instanceof ModularFrameEntity) || !((((ModularFrameEntity) targetBlockEntity).module instanceof ItemTeleporterModule)))
                    playerIn.sendMessage(new TranslatableText("modularitemframe.message.invalid_target"), false);
                else if (!blockEntity.getPos().isWithinDistance(tmp, ModularItemFrame.getConfig().BaseTeleportRange + (countRange * 10))) {
                    playerIn.sendMessage(new TranslatableText("modularitemframe.message.too_far", ModularItemFrame.getConfig().BaseTeleportRange + (countRange * 10)), false);
                } else {
                    linkedLoc = tmp;
                    direction = EnumMode.DISPENSE;
                    reloadModel = true;

                    ItemTeleporterModule targetModule = (ItemTeleporterModule) ((ModularFrameEntity) targetBlockEntity).module;
                    targetModule.linkedLoc = blockEntity.getPos();
                    targetModule.direction = EnumMode.VACUUM;
                    targetModule.reloadModel = true;

                    playerIn.sendMessage(new TranslatableText("modularitemframe.message.link_established"), false);
                    nbt.remove(NBT_LINK);
                    driver.setTag(nbt);

                    targetBlockEntity.markDirty();
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (direction != EnumMode.VACUUM) return ActionResult.FAIL;
        if (!hasValidConnection(worldIn)) return ActionResult.FAIL;

        ItemStack held = playerIn.getStackInHand(hand);

        if (!held.isEmpty()) {
            ItemHelper.ejectStack(worldIn, linkedLoc, worldIn.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), held);
            held.setCount(0);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onRemove(World worldIn, BlockPos pos, Direction facing, PlayerEntity playerIn) {
        if (hasValidConnection(worldIn)) {
            ItemTeleporterModule targetModule = (ItemTeleporterModule) ((ModularFrameEntity) Objects.requireNonNull(worldIn.getBlockEntity(linkedLoc))).module;
            targetModule.linkedLoc = null;
            targetModule.direction = EnumMode.NONE;
            targetModule.reloadModel = true;
        }
    }

    @Override
    public void tick( World world,  BlockPos pos) {
        if (direction != EnumMode.VACUUM) return;
        if (ModularItemFrame.getConfig() .DisableAutomaticItemTransfer) return;
        if (!hasValidConnection(world)) return;
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

        List<ItemEntity> entities = world.getEntities(ItemEntity.class, getVacuumBB(pos), itemEntity -> true);
        for (ItemEntity entity : entities) {
            ItemStack entityStack = entity.getStack();
            if (!entity.isAlive() || entityStack.isEmpty()) continue;

            ItemHelper.ejectStack(world, linkedLoc, world.getBlockState(linkedLoc).get(ModularFrameBlock.FACING), entityStack);
            entity.remove();
            NetworkHandler.sendAround(world, blockEntity.getPos(), 32, new SpawnParticlesPacket(RegistryHelper.getId(ParticleTypes.EXPLOSION), entity.getBlockPos(), 1));
            break;
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = super.toTag();
        if (linkedLoc != null) {
            compound.putInt(NBT_LINKX, linkedLoc.getX());
            compound.putInt(NBT_LINKY, linkedLoc.getY());
            compound.putInt(NBT_LINKZ, linkedLoc.getZ());
            compound.putInt(NBT_DIR, direction.index);
        }
        return compound;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_LINKX))
            linkedLoc = new BlockPos(nbt.getInt(NBT_LINKX), nbt.getInt(NBT_LINKY), nbt.getInt(NBT_LINKZ));
        if (nbt.contains(NBT_DIR)) direction = EnumMode.values()[nbt.getInt(NBT_DIR)];
    }

    private boolean hasValidConnection( World world) {
        if (linkedLoc == null) return false;
        BlockEntity blockEntity = world.getBlockEntity(linkedLoc);
        if (!(blockEntity instanceof ModularFrameEntity)
                || !(((ModularFrameEntity) blockEntity).module instanceof ItemTeleporterModule)
                || ((ItemTeleporterModule) ((ModularFrameEntity) blockEntity).module).direction != EnumMode.DISPENSE)
            return false;
        return true;
    }

    private Box getVacuumBB(BlockPos pos) {
        int range = ModularItemFrame.getConfig().BaseVacuumRange + blockEntity.getRangeUpCount();
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
