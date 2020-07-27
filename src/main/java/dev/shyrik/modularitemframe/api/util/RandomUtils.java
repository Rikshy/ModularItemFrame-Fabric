package dev.shyrik.modularitemframe.api.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.inventory.;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class RandomUtils {

    private static Function<Identifier, SpriteAtlasTexture> func;

    public static SpriteAtlasTexture getSprite(Identifier textureLocation) {
        if (func == null)
            func = MinecraftClient.getInstance().getSpriteAtlas(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

        return func.apply(textureLocation);
    }
}
