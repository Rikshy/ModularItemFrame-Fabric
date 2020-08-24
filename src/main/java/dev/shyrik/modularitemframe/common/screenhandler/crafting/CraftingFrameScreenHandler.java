package dev.shyrik.modularitemframe.common.screenhandler.crafting;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.api.util.GhostSlot;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

public class CraftingFrameScreenHandler extends GhostInventoryScreenHandler {

    private static final int FRAME_SLOTS_PER_ROW = 3;
    private static final int SLOTS_PER_ROW = 9;
    private static final int INV_ROWS = 3;
    /**
     * The object to send callbacks to.
     */
    private final IScreenHandlerCallback callbacks;

    private final FrameCrafting matrix;
    private final CraftingResultInventory craftResult = new CraftingResultInventory();
    private final PlayerEntity player;

    public CraftingFrameScreenHandler(int containerId, Inventory playerInventory, FixedItemInv frameInventory, PlayerEntity player, IScreenHandlerCallback callbacks) {
        super(ScreenHandlerType.CRAFTING, containerId);
        this.player = player;
        this.callbacks = callbacks;

        matrix = new FrameCrafting(this, frameInventory, 3, 3);
        matrix.markDirty();

        addSlot(new CraftingResultSlot(player, matrix, craftResult, 0, 124, 35) {
            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return false;
            }
        });
        for (int row = 0; row < FRAME_SLOTS_PER_ROW; ++row) {
            for (int col = 0; col < FRAME_SLOTS_PER_ROW; ++col) {
                addSlot(new GhostSlot(matrix, col + row * FRAME_SLOTS_PER_ROW, 30 + col * 18, 17 + row * 18));
            }
        }


        if (playerInventory != null) {
            for (int row = 0; row < INV_ROWS; ++row) {
                for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                    addSlot(new Slot(playerInventory, col + row * SLOTS_PER_ROW + SLOTS_PER_ROW, 8 + col * 18, 84 + row * 18));
                }
            }

            for (int col = 0; col < SLOTS_PER_ROW; ++col) {
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return callbacks.isUsableByPlayer(player);
    }

    @Override
    public void sendContentUpdates() {
        ItemStack stack = callbacks.onScreenHandlerMatrixChanged(matrix);
        craftResult.setStack(0, stack);
        ((ServerPlayerEntity)player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, stack));

        super.sendContentUpdates();
    }
}
