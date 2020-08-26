package dev.shyrik.modularitemframe.client;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.ModuleItem;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.util.RegistryHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.common.module.EmptyModule;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class FrameRenderer extends BlockEntityRenderer<ModularFrameEntity> {

    private BakedModel model = null;
    private Identifier currentFront = null;

    private final Random RANDOM = new Random(31100L);
    private final List<RenderLayer> layers =
            IntStream.range(0, 16).mapToObj((i) -> RenderLayer.getEndPortal(i + 1)).collect(ImmutableList.toImmutableList());

    private static final Map<Identifier, BakedModel> models = new HashMap<>();

    public FrameRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    //region <modelLoading>
    public static void onApplyModelLoader(ModelLoader modelLoader) {
        UnbakedModel unbakedFrame = modelLoader.getOrLoadModel(new Identifier(ModularItemFrame.MOD_ID, "block/modular_frame"));
        BakedModelManager bmMan = MinecraftClient.getInstance().getBakedModelManager();

        ModuleItem.getModuleIds().forEach(id -> {
            ModuleBase module = ModuleItem.createModule(id);
            assert module != null;
            if (module.hasModelVariants()) {
                for (Identifier front : module.getVariantFronts()) {
                    BakedModel bakedFrame = unbakedFrame.bake(modelLoader, mat -> {
                        if (mat.getTextureId().toString().contains("default_front"))
                            return bmMan.method_24153(mat.getAtlasId()).getSprite(front);
                        if (mat.getTextureId().toString().contains("default_back"))
                            return bmMan.method_24153(mat.getAtlasId()).getSprite(module.backTexture());
                        if (mat.getTextureId().toString().contains("default_inner"))
                            return bmMan.method_24153(mat.getAtlasId()).getSprite(module.innerTexture());
                        return bmMan.method_24153(mat.getAtlasId()).getSprite(mat.getTextureId());
                    }, ModelRotation.X0_Y0, RegistryHelper.getId(Registrar.MODULAR_FRAME));

                    models.put(front, bakedFrame);
                }
            } else {
                BakedModel bakedFrame = unbakedFrame.bake(modelLoader, mat -> {
                    if (mat.getTextureId().toString().contains("default_front"))
                        return bmMan.method_24153(mat.getAtlasId()).getSprite(module.frontTexture());
                    if (mat.getTextureId().toString().contains("default_back"))
                        return bmMan.method_24153(mat.getAtlasId()).getSprite(module.backTexture());
                    if (mat.getTextureId().toString().contains("default_inner"))
                        return bmMan.method_24153(mat.getAtlasId()).getSprite(module.innerTexture());
                    return bmMan.method_24153(mat.getAtlasId()).getSprite(mat.getTextureId());
                }, ModelRotation.X0_Y0, RegistryHelper.getId(Registrar.MODULAR_FRAME));

                models.put(module.frontTexture(), bakedFrame);
            }
        });

        models.put(EmptyModule.FG,
                unbakedFrame.bake(modelLoader, mat ->
                                bmMan.method_24153(mat.getAtlasId()).getSprite(mat.getTextureId()),
                                ModelRotation.X0_Y0,
                                RegistryHelper.getId(Registrar.MODULAR_FRAME)));
    }

    private BakedModel getBakedModel(ModuleBase module) {
        if (currentFront != module.frontTexture()) {
            currentFront = module.frontTexture();
            model = models.get(currentFront);
        }

        return model;
    }
    //endregion <modelLoading>

    @Override
    public void render(ModularFrameEntity frame, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrixStack.push();
        ModuleBase module = frame.getModule();
        BakedModel modelFrame = getBakedModel(module);

        rotateFrameOnFacing(frame.getFacing(), matrixStack);

        MinecraftClient.getInstance()
                .getBlockRenderManager()
                .getModelRenderer()
                .render(
                        matrixStack.peek(),
                        vertexConsumers.getBuffer(RenderLayer.getTranslucentNoCrumbling()),
                        frame.getCachedState(),
                        modelFrame,
                        1,
                        1,
                        1,
                        light,
                        overlay
                );

        matrixStack.push();
        module.specialRendering(this, matrixStack, tickDelta, vertexConsumers, light, overlay);
        matrixStack.pop();

        renderUpgrades(frame, matrixStack, vertexConsumers, light, overlay);

        matrixStack.pop();
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

    private void renderUpgrades(ModularFrameEntity frame, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (frame.getUpgradeCount() == 0) return;

        matrixStack.push();

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        int i = 0;
        for (UpgradeBase up : frame.getUpgrades()) {
            matrixStack.push();

            int side = i == 0 ? 0 : i / 5;
            int pos = i % 5;

            float sideOffset = side == 0 ? 0.85F : 0.15F;
            float posOffset = pos * 0.05F;

            if (side == 0 || side == 1) {
                matrixStack.translate(0.4F + posOffset, sideOffset, 0.11F);
            } else {
                matrixStack.translate(sideOffset, 0.6F - posOffset, 0.11F);
            }
            matrixStack.scale(0.05F, 0.05F, 0.05F);

            ItemStack renderStack = up.getItem().getStackForRender();
            itemRenderer.renderItem(renderStack, ModelTransformation.Mode.GUI, light, overlay, matrixStack, vertexConsumers);

            i++;
            matrixStack.pop();
        }

        matrixStack.pop();
    }

    //region <itemRender>
    public void renderInside(ItemStack stack, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        renderInside(stack, 0, 0.5F, ModelTransformation.Mode.FIXED, matrixStack, buffer, light, overlay);
    }
    public void renderInside(ItemStack stack, int rotation, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        renderInside(stack, rotation, 0.5F, ModelTransformation.Mode.FIXED, matrixStack, buffer, light, overlay);
    }

    public void renderInside(ItemStack stack, float rotation, float scale, ModelTransformation.Mode transformType, MatrixStack matrixStack, VertexConsumerProvider buffer, int light, int overlay) {
        if (stack.isEmpty())
            return;

        matrixStack.push();

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        matrixStack.translate(0.5F, 0.5F, 0.1F);
        matrixStack.multiply(new Quaternion(0.0F, 0.0F, rotation, true));
        matrixStack.scale(scale, scale, scale);

        BakedModel model = itemRenderer.getHeldItemModel(stack, null, null);
        if (model.hasDepth()) {
            matrixStack.multiply(new Quaternion(0F, 180.0F, 0.0F, true));
        }
        itemRenderer.renderItem(stack, transformType, light, overlay, matrixStack, buffer);

        matrixStack.pop();
    }
    //endregion <itemRender>

    //region <ender>
    public void renderEnder(ModularFrameEntity frame, MatrixStack matrixStack, VertexConsumerProvider bufferBuilder, float offset1, float offset2, float offset3) {
        double distance = frame.getPos().getSquaredDistance(dispatcher.camera.getPos(), true);
        int val = getPasses(distance);
        Matrix4f matrix4f = matrixStack.peek().getModel();

        enderMagic(0.15F, matrix4f, bufferBuilder.getBuffer(layers.get(0)), offset1, offset2, offset3);

        for (int i = 1; i < val; ++i) {
            enderMagic(2.0F / (float) (18 - i), matrix4f, bufferBuilder.getBuffer(layers.get(i)), offset1, offset2, offset3);
        }
    }

    private void enderMagic(float colorMultiplier, Matrix4f matrix, VertexConsumer buffer, float offset1, float offset2, float offset3) {
        float red = (RANDOM.nextFloat() * 0.5F + 0.1F) * colorMultiplier;
        float blue = (RANDOM.nextFloat() * 0.5F + 0.4F) * colorMultiplier;
        float green = (RANDOM.nextFloat() * 0.5F + 0.5F) * colorMultiplier;

        buffer.vertex(matrix, offset1, offset1, offset2).color(red, blue, green, 1.0F).next();
        buffer.vertex(matrix, offset3, offset1, offset2).color(red, blue, green, 1.0F).next();
        buffer.vertex(matrix, offset3, offset3, offset2).color(red, blue, green, 1.0F).next();
        buffer.vertex(matrix, offset1, offset3, offset2).color(red, blue, green, 1.0F).next();
    }

    private int getPasses(double d) {
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
    //endregion <ender>
}
