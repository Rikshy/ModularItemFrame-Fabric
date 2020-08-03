package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.init.ModularItemFrameConfig;
import dev.shyrik.modularitemframe.init.Registrar;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModularItemFrame implements ModInitializer {

    public static final String MOD_ID = "modularitemframe";

    public static ItemGroup GROUP = new ItemGroup(1, MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registrar.SCREWDRIVER);
        }
    };

    @Override
    public void onInitialize() {
        AutoConfig.register(ModularItemFrameConfig.class, JanksonConfigSerializer::new);

        UseBlockCallback.EVENT.register(ModularFrameBlock::onPlayerInteracted);
        Registrar.register();
    }

    public static ModularItemFrameConfig getConfig() {
        return AutoConfig.getConfigHolder(ModularItemFrameConfig.class).getConfig();
    }
}
