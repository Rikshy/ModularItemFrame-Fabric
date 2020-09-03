package dev.shyrik.modularitemframe.util;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.InventoryFixedWrapper;
import net.minecraft.entity.player.PlayerEntity;

public class FixedInventoryWrapper extends InventoryFixedWrapper {
    public FixedInventoryWrapper(FixedItemInv inv) {
        super(inv);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }
}
