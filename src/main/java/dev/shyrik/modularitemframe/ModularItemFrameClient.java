package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.api.mixin.ApplyModelLoaderCallback;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t1.*;
import dev.shyrik.modularitemframe.common.module.t2.*;
import dev.shyrik.modularitemframe.common.module.t3.*;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;

import java.util.Arrays;

public class ModularItemFrameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
                .register((spriteAtlasTexture, registry) -> stitch(registry));
        ApplyModelLoaderCallback.EVENT.register(FrameRenderer::onApplyModelLoader);
        UseBlockCallback.EVENT.register(ModularFrameBlock::onPlayerInteracted);
        BlockEntityRendererRegistry.INSTANCE.register(Registrar.MODULAR_FRAME_ENTITY, FrameRenderer::new);
    }

    public void stitch(ClientSpriteRegistryCallback.Registry registry) {
        Arrays.asList(
                ModularFrameBlock.INNER_HARDEST_LOC,
                ModularFrameBlock.INNER_HARD_LOC,

                CraftingModule.BG_LOC,
                IOModule.BG_LOC,
                ItemModule.BG_LOC,
                NullifyModule.BG_LOC,
                StorageModule.BG_LOC,
                TankModule.BG_LOC,

                CraftingPlusModule.BG_LOC,
                DispenseModule.BG_LOC,
                TrashCanModule.BG_LOC1,
                TrashCanModule.BG_LOC2,
                TrashCanModule.BG_LOC3,
                UseModule.BG_LOC,
                VacuumModule.BG_LOC,

                AutoCraftingModule.BG_LOC,
                FluidDispenserModule.BG_LOC,
                ItemTeleportModule.BG_IN,
                ItemTeleportModule.BG_OUT,
                ItemTeleportModule.BG_NONE,
                XPModule.BG_LOC
        ).forEach(registry::register);
    }
}
