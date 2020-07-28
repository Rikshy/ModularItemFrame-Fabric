package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FluidDispenserModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_fluid_dispenser");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_fluid_dispenser");

    private static final int BUCKET_VOLUME = 1000;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.fluid_dispenser");
    }

    @Override
    public ActionResult onUse( World worldIn,  BlockPos pos,  BlockState state,  PlayerEntity playerIn,  Hand hand,  Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick( World world,  BlockPos pos) {
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) == 0) return;
        Direction facing = blockEntity.blockFacing();
        if (!world.isAir(pos.offset(facing))) return;

        BlockEntity neighbor = blockEntity.getAttachedTile();
        if (neighbor == null) return;

        //IFluidHandler handler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).orElse(null);
        //if (handler == null) return;

        //if (FluidUtil.tryPlaceFluid(null, world, Hand.MAIN_HAND, pos.offset(facing.getOpposite()), handler, handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE)))
        //    handler.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
    }

}
