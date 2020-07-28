package dev.shyrik.modularitemframe.common.network.packet;

import dev.shyrik.modularitemframe.ModularItemFrame;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TeleportPacket extends NetworkPacket {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "teleport");

    private BlockPos pos;

    public TeleportPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static TeleportPacket decode(PacketByteBuf buf) {
        return new TeleportPacket(buf.readBlockPos());
    }

    @Environment(EnvType.CLIENT)
    public static void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        packetContext.getTaskQueue().execute(() -> {
            TeleportPacket packet = decode(packetByteBuf);

            MinecraftClient mc = MinecraftClient.getInstance();
            mc.inGameHud.getBossBarHud().clear();
            mc.getSoundManager().play(new PositionedSoundInstance(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.AMBIENT, 0.4F, 1F, packet.pos));
            for (int i = 0; i < 128; i++) {
                mc.world.addParticle(ParticleTypes.PORTAL,
                        packet.pos.getX() + (mc.world.random.nextDouble() - 0.5) * 3,
                        packet.pos.getY() + mc.world.random.nextDouble() * 3,
                        packet.pos.getZ() + (mc.world.random.nextDouble() - 0.5) * 3,
                        (mc.world.random.nextDouble() - 0.5) * 2,
                        -mc.world.random.nextDouble(),
                        (mc.world.random.nextDouble() - 0.5) * 2);
            }
        });
    }
}
