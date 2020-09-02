package dev.shyrik.modularitemframe.common.screenhandler.crafting;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.api.util.GhostSlot;
import dev.shyrik.modularitemframe.common.screenhandler.GhostInventoryScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class CraftingFrameScreenHandler extends GhostInventoryScreenHandler {

    private static final int FRAME_SLOTS_PER_ROW = 3;
    /**
     * The object to send callbacks to.
     */
    private final IScreenHandlerCallback callbacks;

    private final FrameCrafting matrix;
    private final CraftingResultInventory craftResult = new CraftingResultInventory();
    private final PlayerEntity player;

    public CraftingFrameScreenHandler(int containerId, PlayerEntity player, FixedItemInv frameInventory, IScreenHandlerCallback callbacks) {
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

            @Override
            public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
                return ItemStack.EMPTY;
            }
        });

        for (int row = 0; row < FRAME_SLOTS_PER_ROW; ++row) {
            for (int col = 0; col < FRAME_SLOTS_PER_ROW; ++col) {
                addSlot(new GhostSlot(matrix, col + row * FRAME_SLOTS_PER_ROW, 30 + col * 18, 17 + row * 18));
            }
        }

        addPlayerInventory(player.inventory);
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

    @Override
    public ItemStack onSlotClick(int slotId, int dragType_or_button, SlotActionType clickType, PlayerEntity player) {
        if (slotId == 9)
            return ItemStack.EMPTY;
        return super.onSlotClick(slotId, dragType_or_button, clickType, player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        if (slotIndex == 9)
            return ItemStack.EMPTY;
        return super.transferSlot(player, slotIndex);
    }
}
