package dev.shyrik.modularitemframe.integration.waila;

import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class FrameWailaDataProvider implements IComponentProvider {

    private final Text moduleText = new TranslatableText("modularitemframe.tooltip.module");

    @Override
    public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
        BlockEntity tile = accessor.getBlockEntity();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (tile instanceof ModularFrameEntity && player != null) {
            ModularFrameEntity frame = (ModularFrameEntity)tile;
            ModuleBase module = frame.getModule();

            tooltip.add(moduleText.copy().append(module.getModuleName()));
            module.appendTooltips(tooltip);
            if (frame.getUpgradeCount() > 0) {
                tooltip.add(new TranslatableText("modularitemframe.tooltip.upgrades", frame.getUpgradeCount()));
                if (player.isSneaking()) {
                    Text base = new LiteralText(" - ");
                    frame.getUpgradesByType().forEach((name, upgrades) ->
                            tooltip.add(base.copy().append(new LiteralText(upgrades.size() + " ")).append(name)));
                }
            }
        }
    }
}
