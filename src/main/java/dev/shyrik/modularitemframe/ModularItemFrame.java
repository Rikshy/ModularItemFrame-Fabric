package dev.shyrik.modularitemframe;

import dev.shyrik.modularitemframe.init.ModularItemFrameConfig;
import dev.shyrik.modularitemframe.init.Registrar;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class ModularItemFrame implements ModInitializer {

    public static final String MOD_ID = "modularitemframe";

    @Override
    public void onInitialize() {
        AutoConfig.register(ModularItemFrameConfig.class, GsonConfigSerializer::new);

        Registrar.register();
    }

    public static ModularItemFrameConfig getConfig() {
        return AutoConfig.getConfigHolder(ModularItemFrameConfig.class).getConfig();
    }
}
