package dev.shyrik.modularitemframe.common.network;

import dev.shyrik.modularitemframe.common.network.packet.NetworkPacket;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import dev.shyrik.modularitemframe.common.network.packet.SpawnParticlesPacket;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class NetworkHandler {

    public static void register() {
        ClientSidePacketRegistry.INSTANCE.register(PlaySoundPacket.ID, PlaySoundPacket::accept);
        ClientSidePacketRegistry.INSTANCE.register(SpawnParticlesPacket.ID, SpawnParticlesPacket::accept);
    }

    public static void sendAround(World world, BlockPos pos, double radius, NetworkPacket packet) {
        Stream<PlayerEntity> players = PlayerStream.around(world, pos, radius);

        players.forEach(player ->
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet.getId(), packet.toBuffer()));
    }
}
