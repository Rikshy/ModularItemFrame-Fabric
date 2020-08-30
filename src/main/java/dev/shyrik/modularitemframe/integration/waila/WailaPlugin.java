package dev.shyrik.modularitemframe.integration.waila;

import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

public class WailaPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        final FrameWailaDataProvider provider = new FrameWailaDataProvider();

        registrar.registerComponentProvider(provider, TooltipPosition.BODY, ModularFrameEntity.class);
    }
}
