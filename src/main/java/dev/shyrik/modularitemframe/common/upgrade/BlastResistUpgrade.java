package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.minecraft.util.Identifier;

public class BlastResistUpgrade extends UpgradeBase {
    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "upgrade_resist");

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public Identifier getId() {
        return LOC;
    }
}
