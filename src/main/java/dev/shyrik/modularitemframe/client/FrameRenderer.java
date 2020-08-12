package dev.shyrik.modularitemframe.client;

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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FrameRenderer extends BlockEntityRenderer<ModularFrameEntity> {

    private BakedModel model = null;
    private Identifier currentFront = null;
    private static final Map<Identifier, BakedModel> models = new HashMap<>();

    public FrameRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }
    public BlockEntityRenderDispatcher getDispatcher() { return dispatcher; }

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

    @Override
    public void render(ModularFrameEntity frame, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrixStack.push();
        ModuleBase module = frame.getModule();
        BakedModel modelFrame = getBakedModel(module);

        rotateFrameOnFacing(frame.blockFacing(), matrixStack);

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

        matrixStack.pop();

        module.specialRendering(this, matrixStack, tickDelta, vertexConsumers, light, overlay);
    }

    private void renderUpgrades(ModularFrameEntity frame) {
        for (UpgradeBase up : frame.getUpgrades()) {

        }
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
