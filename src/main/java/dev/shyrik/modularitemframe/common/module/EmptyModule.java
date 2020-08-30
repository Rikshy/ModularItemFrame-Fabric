package dev.shyrik.modularitemframe.common.module;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EmptyModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_empty");
    public static final Identifier FG = new Identifier(ModularItemFrame.MOD_ID, "block/default_front");
    private static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/default_back");
    private static final Text NAME = new TranslatableText("modularitemframe.module.empty");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return FG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier backTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getModuleName() {
        return NAME;
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        return ActionResult.PASS;
    }
}
