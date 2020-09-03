package dev.shyrik.modularitemframe.common.screenhandler.crafting;

import net.minecraft.recipe.CraftingRecipe;

public interface IScreenHandlerCallback {

    /**
     * Called when a {@link net.minecraft.screen.ScreenHandler} is changed.
     *
     * @param recipe The crafting matrix
     */
    void setRecipe(CraftingRecipe recipe);
}
