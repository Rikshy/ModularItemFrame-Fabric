package dev.shyrik.modularitemframe.common.network.packet;

import dev.shyrik.modularitemframe.ModularItemFrame;
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

    private PlaySoundPacket(BlockPos pos, Identifier soundId, String soundCategory, float volume, float pitch) {
        this.soundId = soundId;
        this.pos = pos;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySoundPacket(BlockPos pos, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        this.soundId = sound.getId();
        this.pos = pos;
        this.soundCategory = soundCategory.getName();
        this.volume = volume;
        this.pitch = pitch;
    }
    public PlaySoundPacket(BlockPos pos, SoundEvent sound, SoundCategory soundCategory) {
        this(pos, sound, soundCategory, 0.4f, 0.7f);
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

    public static void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        PlaySoundPacket packet = decode(packetByteBuf);

        packetContext.getTaskQueue().execute(() -> {

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
