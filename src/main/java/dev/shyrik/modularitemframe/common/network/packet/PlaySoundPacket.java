package dev.shyrik.modularitemframe.common.network.packet;

import dev.shyrik.modularitemframe.ModularItemFrame;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Optional;

public class PlaySoundPacket extends NetworkPacket {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "play_sound");

    private final Identifier soundId;
    private final BlockPos pos;
    private final String soundCategory;
    private final float volume;
    private final float pitch;

    public PlaySoundPacket(BlockPos pos, Identifier soundId, String soundCategory, float volume, float pitch) {
        this.soundId = soundId;
        this.pos = pos;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public void encode(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(soundId);
        buf.writeString(soundCategory);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    public static PlaySoundPacket decode(PacketByteBuf buf) {
        return new PlaySoundPacket(
                buf.readBlockPos(),
                buf.readIdentifier(),
                buf.readString(32767),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    @Environment(EnvType.CLIENT)
    public static void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        packetContext.getTaskQueue().execute(() -> {
            PlaySoundPacket packet = decode(packetByteBuf);

            SoundEvent sound = Registry.SOUND_EVENT.get(packet.soundId);
            Optional<SoundCategory> cat = Arrays.stream(SoundCategory.values())
                    .filter(sc -> sc.getName().equals(packet.soundCategory)).findFirst();

            if (sound != null && cat.isPresent())
                MinecraftClient.getInstance()
                        .getSoundManager()
                        .play(new PositionedSoundInstance(sound, cat.get(), packet.volume, packet.pitch, packet.pos));
        });
    }
}
