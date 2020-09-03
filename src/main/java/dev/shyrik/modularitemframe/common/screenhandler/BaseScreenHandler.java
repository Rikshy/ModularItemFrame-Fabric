package dev.shyrik.modularitemframe.common.screenhandler;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public abstract class BaseScreenHandler extends ScreenHandler {
    protected BaseScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    public void onSlotChanged(FixedSlot slot) {

    }
}
