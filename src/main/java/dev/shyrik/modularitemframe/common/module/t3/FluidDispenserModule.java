package dev.shyrik.modularitemframe.common.module.t3;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FluidDispenserModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_fluid_dispenser");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_fluid_dispenser");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.fluid_dispenser");
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient || frame.isPowered() || !canTick(world,60, 10)) return;
        Direction facing = frame.getFacing();
        if (!world.isAir(pos.offset(facing))) return;

        FixedFluidInv neighbor = frame.getAttachedTank();
        if (neighbor == null) return;

        FluidVolume attempt = neighbor.getExtractable().attemptAnyExtraction(FluidAmount.BUCKET, Simulation.SIMULATE);
        if (attempt.amount().isLessThan(FluidAmount.BUCKET))
            return;

        BlockPos target = pos.offset(frame.getFacing());
        BlockState state = world.getBlockState(target);
        Block block = state.getBlock();
        Fluid fluid = attempt.getRawFluid();

        if (fluid == null) return;

        if (state.canBucketPlace(fluid)) {
            world.setBlockState(target, fluid.getDefaultState().getBlockState());
        } else if (block instanceof FluidFillable) {
            ((FluidFillable) block).tryFillWithFluid(world, pos, state, fluid.getDefaultState());
        }

        neighbor.getExtractable().extract(FluidAmount.BUCKET);
    }
}
