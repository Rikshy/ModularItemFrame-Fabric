package dev.shyrik.modularitemframe.api.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class RandomUtils {

    private static Function<Identifier, Sprite> func;

    @Environment(EnvType.CLIENT)
    public static Sprite getSprite(Identifier textureLocation) {
        if (func == null)
            func = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

        return func.apply(textureLocation);
    }
}
