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
    private final IScreenHandlerCallback callbacks;

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

    @Override
    public ItemStack onSlotClick(int slotId, int dragType_or_button, SlotActionType clickType, PlayerEntity player) {
        Slot slot = slotId < 0 ? null : getSlot(slotId);
        if (slot instanceof GhostSlot) {
            //if (clickType == SlotActionType.PICKUP || clickType == SlotActionType.PICKUP_ALL || clickType == SlotActionType.SWAP)
            {
                ItemStack dropping = player.inventory.getCursorStack();

                if (dropping.getCount() > 0) {
                    ItemStack copy = dropping.copy();
                    copy.setCount(1);
                    slot.setStack(copy);
                } else if (slot.getStack().getCount() > 0) {
                    slot.setStack(ItemStack.EMPTY);
                }

                return slot.getStack().copy();
            }

            //return ItemStack.EMPTY;
        }
        return super.onSlotClick(slotId, dragType_or_button, clickType, player);
    }

    private int transferCount = 0;

    @Override
    public final ItemStack transferSlot(PlayerEntity player, int slotIndex) {
        if (slotIndex < 8) {
            return ItemStack.EMPTY;
        }

        Slot slot = getSlot(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack stackCopy = stack.copy();

        int startIndex;
        int endIndex;

        if (slotIndex < 9) {
            return ItemStack.EMPTY;
        } else if (slotIndex < 18) {
            startIndex = 18;
            endIndex = 18 + 27 + 9;
        } else {
            startIndex = 9;
            endIndex = 18;
        }

        if (!insertItem(stack, startIndex, endIndex, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() == 0) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == stackCopy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return stackCopy;
    }

    @Override
    public void sendContentUpdates() {
        ItemStack stack = callbacks.onScreenHandlerMatrixChanged(matrix);
        craftResult.setStack(0, stack);
        ((ServerPlayerEntity)player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, 0, stack));

        super.sendContentUpdates();
    }
}
