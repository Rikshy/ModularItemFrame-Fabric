package dev.shyrik.modularitemframe.api;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpgradeItem extends Item {
    private final Identifier upgradeId;

    private static final Map<Identifier, Pair<UpgradeItem, Class<? extends UpgradeBase>>> UPGRADES = new HashMap<>();

    public UpgradeItem(Settings prop, Class<? extends UpgradeBase> upgradeClass, Identifier upgradeId) {
        super(prop);
        this.upgradeId = upgradeId;
        UPGRADES.put(upgradeId, new Pair<>(this, upgradeClass));
    }

    public UpgradeBase createUpgrade() {
        return createUpgrade(upgradeId);
    }

    public static UpgradeBase createUpgrade(Identifier id) {
        try {
            Optional<Pair<UpgradeItem, Class<? extends UpgradeBase>>> set = UPGRADES.keySet().stream().filter(r -> r.toString().equals(id.toString())).findAny().map(UPGRADES::get);
            if (set.isPresent()) {
                UpgradeBase upgrade = set.get().getRight().newInstance();
                upgrade.parent = set.get().getLeft();
                return upgrade;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }

        return null;
    }
}
