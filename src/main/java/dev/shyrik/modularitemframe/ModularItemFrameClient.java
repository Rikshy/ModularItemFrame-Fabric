package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.api.mixin.ApplyModelLoaderCallback;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t1.*;
import dev.shyrik.modularitemframe.common.module.t2.*;
import dev.shyrik.modularitemframe.common.module.t2.CraftingModule;
import dev.shyrik.modularitemframe.common.module.t3.*;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.screen.PlayerScreenHandler;

import java.util.Arrays;

public class ModularItemFrameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .register((spriteAtlasTexture, registry) -> stitch(registry));
        ApplyModelLoaderCallback.EVENT.register(FrameRenderer::onApplyModelLoader);
        BlockEntityRendererRegistry.INSTANCE.register(Registrar.MODULAR_FRAME_ENTITY, FrameRenderer::new);
    }

    public void stitch(ClientSpriteRegistryCallback.Registry registry) {
        Arrays.asList(
                ModularFrameBlock.INNER_HARDEST,
                ModularFrameBlock.INNER_HARD,

                IOModule.BG,
                ItemModule.BG,
                StorageModule.BG,
                TankModule.BG,

                BlockBreakModule.BG,
                BlockPlaceModule.BG,
                CraftingModule.BG,
                DispenseModule.BG,
                SlayModule.BG,
                VacuumModule.BG,
                TrashCanModule.BG1,
                TrashCanModule.BG2,
                TrashCanModule.BG3,
                FanModule.BG,

                AutoCraftingModule.BG,
                FluidDispenserModule.BG,
                TeleportModule.BG,
                ItemTeleportModule.BG_IN,
                ItemTeleportModule.BG_OUT,
                ItemTeleportModule.BG_NONE,
                XPModule.BG
        ).forEach(registry::register);
    }
}
