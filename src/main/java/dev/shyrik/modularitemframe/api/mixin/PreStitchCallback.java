package dev.shyrik.modularitemframe.api.mixin;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Set;

public interface PreStitchCallback {

    Event<PreStitchCallback> EVENT = EventFactory.createArrayBacked(PreStitchCallback.class,
            (listeners) -> (ctx) -> {
                for (PreStitchCallback listener : listeners) {
                    listener.onStitch(ctx);
                }
            });

    void onStitch(Context ctx);

    class Context {

        private final Set<Identifier> sprites;

        public Context(Set<Identifier> sprites) {
            this.sprites = sprites;
        }

        public void addSprites(Identifier... sprites) {
            this.sprites.addAll(Arrays.asList(sprites));
        }
    }
}
