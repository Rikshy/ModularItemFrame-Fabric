package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SpeedUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_speed");
    private static final Text NAME = new TranslatableText("modularitemframe.upgrade.speed");

    @Override
    public int getMaxCount() {
        return 5;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getName() {
        return NAME;
    }
}
