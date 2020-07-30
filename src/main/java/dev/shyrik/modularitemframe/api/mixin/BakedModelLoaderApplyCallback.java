package dev.shyrik.modularitemframe.api.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

public interface BakedModelLoaderApplyCallback {

    Event<BakedModelLoaderApplyCallback> EVENT = EventFactory.createArrayBacked(BakedModelLoaderApplyCallback.class,
            (listeners) -> (modelLoader, resourceManager, profiler) -> {
                for (BakedModelLoaderApplyCallback listener : listeners) {
                    listener.apply(modelLoader, resourceManager, profiler);
                }
            });

    void apply(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler);
}
