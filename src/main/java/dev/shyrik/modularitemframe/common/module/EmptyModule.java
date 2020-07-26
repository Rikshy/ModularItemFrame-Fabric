package dev.shyrik.modularitemframe.common.module;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EmptyModule extends ModuleBase {
    private static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_empty");
    private static final Identifier FG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/default_front");
    private static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/default_back");

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return FG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier backTexture() {
        return BG_LOC;
    }

    @Override
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.empty");
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult trace) {
        return ActionResult.PASS;
    }
}
