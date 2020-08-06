package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.minecraft.util.Identifier;

public class CapacityUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_capacity");

    @Override
    public int getMaxCount() {
        return 5;
    }

    @Override
    public Identifier getId() {
        return ID;
    }
}
