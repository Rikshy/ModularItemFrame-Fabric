package dev.shyrik.modularitemframe.api.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.ModelLoader;

public interface ApplyModelLoaderCallback {

    Event<ApplyModelLoaderCallback> EVENT = EventFactory.createArrayBacked(ApplyModelLoaderCallback.class,
            (listeners) -> (modelLoader) -> {
                for (ApplyModelLoaderCallback listener : listeners) {
                    listener.apply(modelLoader);
                }
            });

    void apply(ModelLoader modelLoader);
}
