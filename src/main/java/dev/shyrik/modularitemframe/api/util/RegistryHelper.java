package dev.shyrik.modularitemframe.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistryHelper {

    public static Identifier getId(Item item) {
        return Registry.ITEM.getId(item);
    }

    public static Identifier getId(ItemStack stack) {
        return getId(stack.getItem());
    }

    public static Identifier getId(Block block) {
        return Registry.BLOCK.getId(block);
    }

    public static Identifier getId(ParticleType<?> particleType) {
        return Registry.PARTICLE_TYPE.getId(particleType);
    }

    public static Identifier getId(SoundEvent soundEvent) {
        return Registry.SOUND_EVENT.getId(soundEvent);
    }

    public static ParticleType getParticle(Identifier id) {
        return Registry.PARTICLE_TYPE.get(id);
    }
}
