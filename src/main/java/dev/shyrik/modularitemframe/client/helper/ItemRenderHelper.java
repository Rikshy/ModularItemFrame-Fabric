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
    private static void center(MatrixStack matrixStack, Direction facing, float rotation) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(0.5F, 0.5F, 0.9F);
                matrixStack.multiply(new Quaternion(0.0F, -180.0F, rotation, true));
                break;
            case SOUTH:
                matrixStack.translate(0.5F, 0.5F, 0.1F);
                matrixStack.multiply(new Quaternion(0.0F, 0.0F, rotation, true));
                break;
            case WEST:
                matrixStack.translate(0.9F, 0.5F, 0.5F);
                matrixStack.multiply(new Quaternion(-rotation, -90.0F, 0.0F, true));
                break;
            case EAST:
                matrixStack.translate(0.1F, 0.5F, 0.5F);
                matrixStack.multiply(new Quaternion(rotation, 90.0F, 0.0F, true));
                break;
            case DOWN:
                matrixStack.translate(0.5F, 0.9F, 0.5F);
                matrixStack.multiply(new Quaternion(90.0F, 0.0F, rotation, true));
                break;
            case UP:
                matrixStack.translate(0.5F, 0.1F, 0.5F);
                matrixStack.multiply(new Quaternion(-90.0F, 0.0F, rotation, true));
                break;
        }
    }

    public static void renderInside(ItemStack stack, Direction facing, float rotation, float scale, ModelTransformation.Mode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        if (stack.isEmpty())
            return;

        matrixStack.push();

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        center(matrixStack, facing, rotation);
        matrixStack.scale(scale, scale, scale);

        BakedModel model = itemRenderer.getHeldItemModel(stack, null, null);
        if (model.hasDepth()) {
            matrixStack.multiply(new Quaternion(0F, 180.0F, 0.0F, true));
        }
        itemRenderer.renderItem(stack, transformType, light, overlay, matrixStack, buffer);

        matrixStack.pop();
    }

    public static void renderInside(ItemStack stack, Direction facing, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        renderInside(stack, facing, 0, 0.5F, ModelTransformation.Mode.FIXED, matrixStack, buffer, light, overlay);
    }
    public static void renderInside(ItemStack stack, Direction facing, int rotation, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        renderInside(stack, facing, rotation, 0.5F, ModelTransformation.Mode.FIXED, matrixStack, buffer, light, overlay);
    }

    public static void renderOnFrame(ItemStack stack, Direction facing, float offset, float scale, ModelTransformation.Mode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        if (stack.isEmpty())
            return;
        matrixStack.pop();
    }
}
