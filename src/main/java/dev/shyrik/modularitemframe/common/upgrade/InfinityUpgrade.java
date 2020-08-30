package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class InfinityUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_infinity");
    private static final Text NAME = new TranslatableText("modularitemframe.upgrade.infinity");

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Text getName() {
        return NAME;
    }
}
