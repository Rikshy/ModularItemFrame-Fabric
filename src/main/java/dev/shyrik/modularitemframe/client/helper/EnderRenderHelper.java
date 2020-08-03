package dev.shyrik.modularitemframe.client.helper;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class EnderRenderHelper {
    private static final Random RANDOM = new Random(31100L);
    private static final List<RenderLayer> layers =
            IntStream.range(0, 16).mapToObj((i) -> RenderLayer.getEndPortal(i + 1)).collect(ImmutableList.toImmutableList());

    public static class EndRenderFace {
        public EndRenderFace(float offset1, float offset2, float offset3, Direction side) {
            this.offset1 = offset1;
            this.offset2 = offset2;
            this.offset3 = offset3;
            this.side = side;
        }
        public float offset1, offset2, offset3;
        public Direction side;
    }

    public static void render(ModularFrameEntity frame, MatrixStack matrixStack, VertexConsumerProvider bufferBuilder, Vec3d projectedView, List<EndRenderFace> faces) {
        EndRenderFace face = faces.stream().filter(endRenderFace -> endRenderFace.side == frame.blockFacing()).findFirst().orElse(null);
        if (face == null)
            return;

        double distance = frame.getPos().getSquaredDistance(projectedView, true);
        int val = getPasses(distance);
        Matrix4f matrix4f = matrixStack.peek().getModel();

        magic(0.15F, matrix4f, bufferBuilder.getBuffer(layers.get(0)), face);

        for (int i = 1; i < val; ++i) {
            magic(2.0F / (float) (18 - i), matrix4f, bufferBuilder.getBuffer(layers.get(i)), face);
        }
    }

    private static void magic(float colorMultiplier, Matrix4f matrix, VertexConsumer buffer, EndRenderFace face) {
        float red = (RANDOM.nextFloat() * 0.5F + 0.1F) * colorMultiplier;
        float blue = (RANDOM.nextFloat() * 0.5F + 0.4F) * colorMultiplier;
        float green = (RANDOM.nextFloat() * 0.5F + 0.5F) * colorMultiplier;

        switch (face.side){
            case DOWN:
                buffer.vertex(matrix, face.offset1, face.offset2, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset1, face.offset2, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset2, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset2, face.offset3).color(red, blue, green, 1.0F).next();
                break;
            case UP:
                buffer.vertex(matrix, face.offset1, face.offset2, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset1, face.offset2, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset2, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset2, face.offset1).color(red, blue, green, 1.0F).next();
                break;
            case NORTH:
                buffer.vertex(matrix, face.offset3, face.offset1, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset1, face.offset1, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset1, face.offset3, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset3, face.offset2).color(red, blue, green, 1.0F).next();
                break;
            case SOUTH:
                buffer.vertex(matrix, face.offset1, face.offset1, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset1, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset3, face.offset3, face.offset2).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset1, face.offset3, face.offset2).color(red, blue, green, 1.0F).next();
                break;
            case WEST:
                buffer.vertex(matrix, face.offset2, face.offset1, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset1, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset3, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset3, face.offset1).color(red, blue, green, 1.0F).next();
                break;
            case EAST:
                buffer.vertex(matrix, face.offset2, face.offset1, face.offset3).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset1, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset3, face.offset1).color(red, blue, green, 1.0F).next();
                buffer.vertex(matrix, face.offset2, face.offset3, face.offset2).color(red, blue, green, 1.0F).next();
                break;
        }
    }

    private static int getPasses(double d) {
        if (d > 36864.0D) {
            return 1;
        } else if (d > 25600.0D) {
            return 3;
        } else if (d > 16384.0D) {
            return 5;
        } else if (d > 9216.0D) {
            return 7;
        } else if (d > 4096.0D) {
            return 9;
        } else if (d > 1024.0D) {
            return 11;
        } else if (d > 576.0D) {
            return 13;
        } else {
            return d > 256.0D ? 14 : 15;
        }
    }
}
