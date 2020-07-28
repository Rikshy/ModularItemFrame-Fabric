package dev.shyrik.modularitemframe.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.OptionalInt;
import java.util.UUID;

public class FakePlayer extends ServerPlayerEntity {

    public FakePlayer(ServerWorld world, GameProfile name) {
        super(world.getServer(), world, name, new ServerPlayerInteractionManager(world));
    }

    @Override public Vec3d getPos(){ return new Vec3d(0, 0, 0); }
    @Override public void sendMessage(Text message, MessageType type, UUID senderUuid) {}
    @Override public void sendMessage(Text component, boolean actionBar) {}
    @Override public void increaseStat(Stat par1StatBase, int par2){}
    @Override public OptionalInt openHandledScreen(NamedScreenHandlerFactory nameableContainerProvider) {
        return OptionalInt.empty();
    }
    @Override public boolean isInvulnerableTo(DamageSource source){ return true;}
    @Override public boolean isAffectedBySplashPotions() {return false;}
    @Override public boolean shouldDamagePlayer(PlayerEntity player) {return false;}
    @Override public void onDeath(DamageSource source){}
    @Override public void tick(){}
    @Override public void setClientSettings(ClientSettingsC2SPacket clientSettingsC2SPacket) {}
    @Override public boolean canHaveStatusEffect(StatusEffectInstance effect) {return false;}
}
