package dev.shyrik.modularitemframe.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.UUID;

public class FakePlayer extends ServerPlayerEntity {

    public FakePlayer(ServerWorld world, GameProfile name)
    {
        super(world.getServer(), world, name, new ServerPlayerInteractionManager(world));
    }

    @Override public Vector3d getPositionVec(){ return new Vector3d(0, 0, 0); }
    @Override public BlockPos func_233580_cy_(){ return BlockPos.ORIGIN; }
    @Override public void sendStatusMessage(TextComponent chatComponent, boolean actionBar){}
    @Override public void sendMessage(TextComponent component, UUID p_145747_2_) {}
    @Override public void addStat(Stat par1StatBase, int par2){}
    //@Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
    @Override public boolean isInvulnerableTo(DamageSource source){ return true; }
    @Override public boolean canAttackPlayer(PlayerEntity player){ return false; }
    @Override public void onDeath(DamageSource source){ return; }
    @Override public void tick(){ return; }
    @Override public void handleClientSettings(ClientSettingsC2SPacket pkt){ return; }
    @Override public MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }

}
