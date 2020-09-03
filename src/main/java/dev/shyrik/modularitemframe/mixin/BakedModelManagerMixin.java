package dev.shyrik.modularitemframe.mixin;

import dev.shyrik.modularitemframe.mixin.interfaces.ApplyModelLoaderCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BakedModelManager.class)
public abstract class BakedModelManagerMixin {

    @Inject(method = "apply", at = @At("TAIL"))
    private void onApply(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        ApplyModelLoaderCallback.EVENT.invoker().apply(modelLoader);
    }
}