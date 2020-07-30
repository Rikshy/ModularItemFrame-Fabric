package dev.shyrik.modularitemframe.common.screenhandler;

import dev.shyrik.modularitemframe.api.util.GhostSlot;
import dev.shyrik.modularitemframe.api.util.SlotHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class CraftingFrameScreenHandler extends ScreenHandler {

    private static final int FRAME_SLOTS_PER_ROW = 3;
    private static final int SLOTS_PER_ROW = 9;
    private static final int INV_ROWS = 3;
    /**
     * The object to send callbacks to.
     */
    private IScreenHandlerCallback callbacks;

    private boolean craftMatrixChanged = false;
    private final FrameCrafting matrix;
    private final CraftingResultInventory craftResult = new CraftingResultInventory();
    private final PlayerEntity player;

    public CraftingFrameScreenHandler(int containerId, Inventory playerInventory, Inventory frameInventory, PlayerEntity player, IScreenHandlerCallback callbacks) {
        super(ScreenHandlerType.CRAFTING, containerId);
        this.player = player;
        this.callbacks = callbacks;

        matrix = new FrameCrafting(this, frameInventory, 3, 3);
        matrix.onCraftMatrixChanged();

        addSlot(new CraftingResultSlot(player, this.matrix, this.craftResult, 0, 124, 35) {
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

    /**
     * Callback for when the matrix matrix is changed.
     */
    @Override
    public void onContentChanged(Inventory inventoryIn) {
        craftMatrixChanged = true;
    }

    @Override
    public ItemStack onSlotClick(int slotId, int dragType_or_button, SlotActionType clickType, PlayerEntity player) {
        Slot slot = slotId < 0 ? null : getSlot(slotId);
        if (slot instanceof GhostSlot) {
            return SlotHelper.ghostSlotClick(slot, dragType_or_button, clickType, player);
        }
        return super.onSlotClick(slotId, dragType_or_button, clickType, player);
    }

    private int transferCount = 0;

    @Override
    public final ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        if (transferCount < 1) {
            transferCount++;
            return SlotHelper.transferStackInSlot(slots, player, slotIndex);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void sendContentUpdates() {
        if (craftMatrixChanged) {
            craftMatrixChanged = false;
            ItemStack stack = callbacks.onScreenHandlerMatrixChanged(matrix);
            craftResult.setStack(0, stack);
            ((ServerPlayerEntity)player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, stack));
        }

        super.sendContentUpdates();
    }
}
