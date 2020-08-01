package dev.shyrik.modularitemframe.client.helper;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.Random;
import java.util.function.Predicate;

public class EnderRenderHelper {
    private static final Random RANDOM = new Random(31100L);

    public static class RenderEndPosInfo {
        public RenderEndPosInfo(VertexConsumer buffer, Matrix4f matrix, float color1, float color2, float color3) {
            this.buffer = buffer;
            this.matrix = matrix;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
        }

        public Matrix4f matrix;
        public VertexConsumer buffer;
        public float color1, color2, color3;
    }

    public static void render(MatrixStack matrixStack, VertexConsumerProvider bufferBuilder, BlockPos pos, Vec3d projectedView, Predicate<RenderEndPosInfo> drawPos) {

        RANDOM.setSeed(31100L);
        double d0 = pos.getSquaredDistance(projectedView, true);
        int i = getPasses(d0);
        Matrix4f matrix4f = matrixStack.peek().getModel();
        foo(0.15F, matrix4f, bufferBuilder.getBuffer(RenderLayer.getEndPortal(i)), drawPos);

        for (int j = 1; j < i; ++j) {
            foo(2.0F / (float) (18 - j), matrix4f, bufferBuilder.getBuffer(RenderLayer.getEndPortal(i)), drawPos);
        }
    }

    private static void foo(float bar, Matrix4f matrix, VertexConsumer buffer, Predicate<RenderEndPosInfo> drawPos) {
        float f = (RANDOM.nextFloat() * 0.5F + 0.1F) * bar;
        float f1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * bar;
        float f2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * bar;

        drawPos.test(new RenderEndPosInfo(buffer, matrix, f, f1, f2));
    }

    private static int getPasses(double iteration) {
        int i;

        if (iteration > 36864.0D) {
            i = 1;
        } else if (iteration > 25600.0D) {
            i = 3;
        } else if (iteration > 16384.0D) {
            i = 5;
        } else if (iteration > 9216.0D) {
            i = 7;
        } else if (iteration > 4096.0D) {
            i = 9;
        } else if (iteration > 1024.0D) {
            i = 11;
        } else if (iteration > 576.0D) {
            i = 13;
        } else if (iteration > 256.0D) {
            i = 14;
        } else {
            i = 15;
        }

        return i;
    }
}
