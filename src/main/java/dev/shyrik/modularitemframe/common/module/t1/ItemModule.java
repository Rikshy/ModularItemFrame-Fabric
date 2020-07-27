package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
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
    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t1_item");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_item");
    private static final String NBT_DISPLAY = "display";
    private static final String NBT_ROTATION = "rotation";

    private int rotation = 0;
    private ItemStack displayItem = ItemStack.EMPTY;

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
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
        FrameItemRenderer.renderOnFrame(displayItem, blockEntity.blockFacing(), rotation, 0.1F, TransformType.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    public void screw(World world, BlockPos pos, PlayerEntity playerIn, ItemStack driver) {
        if (!world.isClient) {
            rotate(playerIn);
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (!worldIn.isClient) {
            ItemStack copy = playerIn.getStackInHand(hand).copy();
            copy.setCount(1);
            displayItem = copy;
            blockEntity.markDirty();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = new CompoundTag();
        compound.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        compound.putInt(NBT_ROTATION, rotation);
        return compound;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(nbt.getCompound(NBT_DISPLAY));
        if (nbt.contains(NBT_ROTATION)) rotation = nbt.getInt(NBT_ROTATION);
    }
}
