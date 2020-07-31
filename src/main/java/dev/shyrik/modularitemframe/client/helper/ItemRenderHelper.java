package dev.shyrik.modularitemframe.client.helper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class ItemRenderHelper {
    private static void translate(MatrixStack matrixStack, Direction facing, float offset) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(0.5F, 0.5F, 1 - offset);
                break;
            case SOUTH:
                matrixStack.translate(0.5F, 0.5F, offset);
                break;
            case WEST:
                matrixStack.translate(1 - offset, 0.5F, 0.5F);
                break;
            case EAST:
                matrixStack.translate(offset, 0.5F, 0.5F);
                break;
            case DOWN:
                matrixStack.translate(0.5F, 1 - offset, 0.5F);
                break;
            case UP:
                matrixStack.translate(0.5F, offset, 0.5F);
                break;
        }
    }
    private static void rotate(MatrixStack matrixStack, Direction facing, float rotation) {
        switch (facing) {
            case NORTH:
                matrixStack.multiply(new Quaternion(0.0F, -180.0F, -rotation, true));
                break;
            case SOUTH:
                matrixStack.multiply(new Quaternion(0.0F, 0.0F, rotation, true));
                break;
            case WEST:
                matrixStack.multiply(new Quaternion(-rotation, -90.0F, 0.0F, true));
                break;
            case EAST:
                matrixStack.multiply(new Quaternion(rotation, 90.0F, 0.0F, true));
                break;
            case DOWN:
                matrixStack.multiply(new Quaternion(90.0F, rotation, 0.0F, true));
                break;
            case UP:
                matrixStack.multiply(new Quaternion(-90.0F, -rotation, 0.0F, true));
                break;
        }
    }

    public static void renderOnFrame(ItemStack stack, Direction facing, float rotation, float offset, ModelTransformation.Mode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        if (!stack.isEmpty()) {
            matrixStack.push();

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

            translate(matrixStack, facing, offset);
            rotate(matrixStack, facing, rotation);
            matrixStack.scale(0.5F, 0.5F, 0.5F);

            //RenderHelper.enableStandardItemLighting();

            BakedModel model = itemRenderer.getHeldItemModel(stack, null, null);
            if (model.hasDepth()) {
                matrixStack.multiply(new Quaternion(0F, 180.0F, 0.0F, true));
            }
            itemRenderer.renderItem(stack, transformType, combinedLight, combinedOverlay, matrixStack, buffer);
            //RenderHelper.disableStandardItemLighting();

            matrixStack.pop();
        }
    }
}
