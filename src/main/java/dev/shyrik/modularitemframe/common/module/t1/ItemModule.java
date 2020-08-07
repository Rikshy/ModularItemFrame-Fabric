package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ItemModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_item");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_item");

    private static final String NBT_DISPLAY = "display";
    private static final String NBT_ROTATION = "rotation";

    private int rotation = 0;
    private ItemStack displayItem = ItemStack.EMPTY;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.item");
    }

    private void rotate(PlayerEntity player) {
        if (player.isSneaking()) {
            rotation += 20;
        } else {
            rotation -= 20;
        }
        if (rotation >= 360 || rotation <= -360) rotation = 0;
        blockEntity.markDirty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        ItemRenderHelper.renderOnFrame(displayItem, blockEntity.blockFacing(), rotation, 0.1F, ModelTransformation.Mode.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            rotate(player);
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack copy = player.getStackInHand(hand).copy();
            copy.setCount(1);
            displayItem = copy;
            blockEntity.markDirty();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        tag.putInt(NBT_ROTATION, rotation);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(tag.getCompound(NBT_DISPLAY));
        if (tag.contains(NBT_ROTATION)) rotation = tag.getInt(NBT_ROTATION);
    }
}
