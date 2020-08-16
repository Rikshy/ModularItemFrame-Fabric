package dev.shyrik.modularitemframe.api.util.fake;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;


import java.lang.ref.WeakReference;
import java.util.*;

public class FakePlayerFactory {
    private static final Map<World, Map<GameProfile, WeakReference<FakePlayer>>> PLAYERS = new WeakHashMap<>();

    public static FakePlayer get(World world, GameProfile profile) {
        return PLAYERS.computeIfAbsent(world, p -> new HashMap<>()).computeIfAbsent(profile, p -> {
            FakePlayer player = new FakePlayer((ServerWorld)world, profile);
            player.networkHandler = new SpaghettiNetworkHandler(player);
            return new WeakReference<>(player);
        }).get();
    }

    public static void unloadPlayers(ServerWorld world) {
        if (PLAYERS.containsKey(world)) {
            PLAYERS.get(world).values().forEach(fp -> fp = null);
            PLAYERS.remove(world);
        }
    }
}
