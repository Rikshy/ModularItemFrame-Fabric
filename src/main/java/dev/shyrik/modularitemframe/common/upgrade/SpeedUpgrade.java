package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.minecraft.util.Identifier;

public class SpeedUpgrade extends UpgradeBase {
    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "upgrade_speed");

    @Override
    public int getMaxCount() {
        return 5;
    }

    @Override
    public Identifier getId() {
        return LOC;
    }
}
