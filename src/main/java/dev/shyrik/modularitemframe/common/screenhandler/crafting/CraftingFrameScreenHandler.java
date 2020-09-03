package dev.shyrik.modularitemframe.common.screenhandler.crafting;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import dev.shyrik.modularitemframe.common.screenhandler.GhostCraftingResultSlot;
import dev.shyrik.modularitemframe.common.screenhandler.GhostCraftingSlots;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import dev.shyrik.modularitemframe.util.FixedInventoryWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class CraftingFrameScreenHandler extends GhostInventoryScreenHandler {

    private static final int FRAME_SLOTS_PER_ROW = 3;
    private static final int FRAME_SLOTS_PER_COL = 3;
    /**
     * The object to send callbacks to.
     */
    private final IScreenHandlerCallback callbacks;

    private final FixedItemInv craftResult = new DirectFixedItemInv(1);
    private final PlayerEntity player;
    private CraftingRecipe currentRecipe;

    public CraftingFrameScreenHandler(int containerId, PlayerEntity player, FixedItemInv frameInventory, IScreenHandlerCallback callbacks) {
        super(ScreenHandlerType.CRAFTING, containerId);
        this.player = player;
        this.callbacks = callbacks;

        addSlot(new GhostCraftingResultSlot(this, craftResult, 0, 124, 35));

        for (int row = 0; row < FRAME_SLOTS_PER_ROW; ++row) {
            for (int col = 0; col < FRAME_SLOTS_PER_COL; ++col) {
                addSlot(new GhostCraftingSlots(this, frameInventory, col + row * FRAME_SLOTS_PER_ROW, 30 + col * 18, 17 + row * 18));
            }
        }

        addPlayerInventory(player.inventory);
        onContentChanged(new FixedInventoryWrapper(frameInventory));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        CraftingInventory ci = new CraftingInventory(new ScreenHandler(null, 0) {
            @Override
            public boolean canUse(PlayerEntity player) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            ci.setStack(i, slots.get(i + 1).getStack());
        }

        if (currentRecipe == null || !currentRecipe.matches(ci, player.world)) {
            currentRecipe = player.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, ci, player.world).orElse(null);
            callbacks.setRecipe(currentRecipe);
            slots.get(0).setStack(currentRecipe == null ? ItemStack.EMPTY : currentRecipe.getOutput());
            sendContentUpdates();
        }
    }
}
