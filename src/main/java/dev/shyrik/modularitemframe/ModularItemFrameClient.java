package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.api.mixin.ApplyModelLoaderCallback;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t1.*;
import dev.shyrik.modularitemframe.common.module.t2.*;
import dev.shyrik.modularitemframe.common.module.t3.*;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;

import java.util.Arrays;

public class ModularItemFrameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.register();
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
                .register((spriteAtlasTexture, registry) -> stitch(registry));
        ApplyModelLoaderCallback.EVENT.register(FrameRenderer::onApplyModelLoader);
        BlockEntityRendererRegistry.INSTANCE.register(Registrar.MODULAR_FRAME_ENTITY, FrameRenderer::new);
    }

    public void stitch(ClientSpriteRegistryCallback.Registry registry) {
        Arrays.asList(
                ModularFrameBlock.INNER_HARDEST,
                ModularFrameBlock.INNER_HARD,

                CraftingModule.BG,
                IOModule.BG,
                ItemModule.BG,
                NullifyModule.BG,
                StorageModule.BG,
                TankModule.BG,

                CraftingPlusModule.BG,
                DispenseModule.BG,
                TrashCanModule.BG1,
                TrashCanModule.BG2,
                TrashCanModule.BG3,
                UseModule.BG,
                VacuumModule.BG,

                AutoCraftingModule.BG,
                FluidDispenserModule.BG,
                ItemTeleportModule.BG_IN,
                ItemTeleportModule.BG_OUT,
                ItemTeleportModule.BG_NONE,
                XPModule.BG
        ).forEach(registry::register);
    }
}
