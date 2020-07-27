package dev.shyrik.modularitemframe.common.screenhandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IScreenHandlerCallback {

    /**
     * Is this usable by the specified player?
     *
     * @param player The player
     * @return Is this usable by the specified player?
     */
    boolean isUsableByPlayer(PlayerEntity player);

    /**
     * Called when a {@link net.minecraft.screen.ScreenHandler} is changed.
     *
     * @param matrix The crafting matrix
     */
    ItemStack onScreenHandlerMatrixChanged(FrameCrafting matrix);
}
