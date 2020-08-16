package dev.shyrik.modularitemframe.common.network.packet;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.util.RegistryHelper;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SpawnParticlesPacket extends NetworkPacket {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "spawn_particles");

    private final Identifier particleId;
    private final BlockPos pos;
    private final int amount;

    private SpawnParticlesPacket(Identifier particleId, BlockPos pos, int amount) {
        this.particleId = particleId;
        this.pos = pos;
        this.amount = amount;
    }
    public SpawnParticlesPacket(ParticleType<?> particle, BlockPos pos, int amount) {
        this.particleId = RegistryHelper.getId(particle);
        this.pos = pos;
        this.amount = amount;
    }

    public static SpawnParticlesPacket decode(PacketByteBuf buf) {
        return new SpawnParticlesPacket(
                buf.readIdentifier(),
                buf.readBlockPos(),
                buf.readInt()
        );
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected void encode(PacketByteBuf buf) {
        buf.writeIdentifier(particleId);
        buf.writeBlockPos(pos);
        buf.writeInt(amount);
    }

    public static void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
        SpawnParticlesPacket packet = decode(packetByteBuf);

        packetContext.getTaskQueue().execute(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return;

            ParticleType<?> particle = RegistryHelper.getParticle(packet.particleId);
            if (particle != null) {
                for (int i = 0; i < packet.amount; i++) {
                    mc.particleManager.addParticle(
                            (ParticleEffect)particle,
                            packet.pos.getX(),
                            packet.pos.getY(),
                            packet.pos.getZ(),
                            0.0D,
                            mc.world.random.nextGaussian(),
                            0.0D);
                }
            }
        });
    }
}
