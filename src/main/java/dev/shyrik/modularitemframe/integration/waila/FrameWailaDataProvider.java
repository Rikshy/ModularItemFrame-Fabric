package dev.shyrik.modularitemframe.integration.waila;

import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class FrameWailaDataProvider implements IComponentProvider {

    private final Text moduleText = new TranslatableText("modularitemframe.tooltip.module");

    @Override
    public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
        BlockEntity tile = accessor.getBlockEntity();
        if (tile instanceof ModularFrameEntity) {
            ModularFrameEntity frame = (ModularFrameEntity)tile;
            ModuleBase module = frame.getModule();

            tooltip.add(moduleText.copy().append(module.getModuleName()));
            module.appendTooltips(tooltip);
        }
    }
}
