package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.api.mixin.PreStitchCallback;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.init.ClientSetup;
import dev.shyrik.modularitemframe.init.Registrar;
import dev.shyrik.modularitemframe.api.mixin.BakedModelLoaderApplyCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

public class ModularItemFrameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Here we will put client-only registration code
        PreStitchCallback.EVENT.register(ClientSetup::stitch);
        BakedModelLoaderApplyCallback.EVENT.register(FrameRenderer::onApplyModelLoader);
        BlockEntityRendererRegistry.INSTANCE.register(Registrar.MODULAR_FRAME_ENTITY, FrameRenderer::new);
    }
}
