package dev.shyrik.modularitemframe.client;

import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class FrameRenderer extends BlockEntityRenderer<ModularFrameEntity> {

    private UnbakedModel model = null;

    public FrameRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }
    public BlockEntityRenderDispatcher getDispatcher() { return dispatcher; }

    private BakedModel getBakedModel(ModularFrameEntity blockEntity) {
        if (model == null) {
            try {
                model = null;//MinecraftClient.getInstance().getDataFixer()..getBakedModelManager() ModelLoader.instance().getUnbakedModel(new ResourceLocation(ModularItemFrame.MOD_ID,"block/modular_frame"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;//blockEntity.module.bakeModel(ModelLoader.instance(), model);
    }

    @Override
    public void render(ModularFrameEntity entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrixStack.push();
        BakedModel modelFrame = getBakedModel(entity);

        rotateFrameOnFacing(entity.blockFacing(), matrixStack);


        MinecraftClient.getInstance()
                .getBlockRenderManager()
                .getModelRenderer()
                .render(
                        matrixStack.peek(),
                        vertexConsumers.getBuffer(RenderLayer.getTranslucentNoCrumbling()),
                        entity.getCachedState(),
                        modelFrame,
                        1,
                        1,
                        1,
                        light,
                        overlay
                );

        matrixStack.pop();

        entity.module.specialRendering(this, matrixStack, tickDelta, vertexConsumers, light, overlay);
    }

    private void rotateFrameOnFacing(Direction facing, MatrixStack matrixStack) {
        switch (facing) {
            case NORTH:
                matrixStack.translate(1.0F, 0.0F, 1.0F);
                matrixStack.multiply(new Quaternion( 0.0F, 180.0F, 0.0F, true));
                break;
            case SOUTH:
                break;
            case WEST:
                matrixStack.multiply(new Quaternion(90.0F, -90.0F, 90.0F, true));
                matrixStack.translate(0.0F, 0.0F, -1.0F);
                break;
            case EAST:
                matrixStack.multiply(new Quaternion(-90.0F, 90.0F, 90.0F, true));
                matrixStack.translate(-1.0F, 0.0F, 0.0F);
                break;
            case DOWN:
                matrixStack.translate(0.0F, 1.0F, 0.0F);
                matrixStack.multiply(new Quaternion(90.0F, 0.0F, 0.0F, true));
                break;
            case UP:
                matrixStack.translate(0.0F, 0.0F, 1.0F);
                matrixStack.multiply(new Quaternion(-90.0F, 0.0F, 0.0F, true));
                break;
        }
    }
}
