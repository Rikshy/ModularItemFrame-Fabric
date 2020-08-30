package dev.shyrik.modularitemframe.common.module.t1;

import alexiil.mc.lib.attributes.fluid.*;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv;
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;

public class TankModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_tank");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t1_tank");
    private static final Text NAME = new TranslatableText("modularitemframe.module.tank");

    private static final String NBT_MODE = "tank_mode";
    private static final String NBT_TANK = "tank";

    public EnumMode mode = EnumMode.NONE;
    private SimpleFixedFluidInv tank = new SimpleFixedFluidInv(1, FluidAmount.ofWhole(ModularItemFrame.getConfig().tankFrameCapacity));

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
    public Identifier backTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getModuleName() {
        return NAME;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        FluidVolume vol = tank.getInvFluid(0);
        if (!vol.amount().isZero()) {
            double amount = (float) vol.getAmount_F().as1620() / (float) tank.getMaxAmount_F(0).as1620();

            //only south cuz the matrix is already in that  direction
            FluidRenderFace face = FluidRenderFace
                    .createFlatFace(0.1875f, 0.1875f, 0.08f, 0.8125f, 0.1875f + amount * 0.625f, 0.08f, 1, Direction.SOUTH);

            vol.render(Collections.singletonList(face), buffer, matrixStack);
        }
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            if (ModularItemFrame.getConfig().tankTransferRate > 0) {
                int modeIdx = mode.getIndex() + 1;
                if (modeIdx == EnumMode.values().length) modeIdx = 0;
                mode = EnumMode.values()[modeIdx];
                player.sendMessage(new TranslatableText("modularitemframe.message.mode_change", mode.getName()), false);
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        FluidInvUtil.interactHandWithTank((FixedFluidInv) tank, player, hand);
        markDirty();
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient || frame.isPowered() || !canTick(world,60, 10)) return;
        if (mode == EnumMode.NONE || ModularItemFrame.getConfig().tankTransferRate <= 0) return;

        FixedFluidInv neighbor = frame.getAttachedTank();
        if (neighbor != null) {
            if (mode == EnumMode.DRAIN)
                FluidVolumeUtil.move((FluidExtractable) neighbor, tank, FluidAmount.of1620(ModularItemFrame.getConfig().tankTransferRate));
            else
                FluidVolumeUtil.move(tank, (FluidInsertable) neighbor, FluidAmount.of1620(ModularItemFrame.getConfig().tankTransferRate));
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        int newCapacity = (int) Math.pow(ModularItemFrame.getConfig().tankFrameCapacity, frame.getCapacityUpCount() + 1);
        SimpleFixedFluidInv tmp = new SimpleFixedFluidInv(1, FluidAmount.of1620(newCapacity));
        tmp.insert(tank.extract(tmp.getMaxAmount_F(0).min(tank.getMaxAmount_F(0))));
        tank = tmp;
        markDirty();
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        super.onRemove(world, pos, facing, player, moduleStack);
        if (!ModularItemFrame.getConfig().dropFluidOnTankRemove || tank.getInvFluid(0).amount().isZero())
            return;
        Fluid fluid = tank.getInvFluid(0).getRawFluid();

        if (fluid == null) return;

        int i = 0;
        for (Direction dir : Direction.values()) {
            if (dir == facing.getOpposite()) continue;
            if (i++ >= tank.getInvFluid(0).amount().whole)
                break;

            BlockState state = world.getBlockState(pos.offset(dir));
            Block block = state.getBlock();
            if (state.canBucketPlace(fluid))
                world.setBlockState(pos.offset(dir), fluid.getDefaultState().getBlockState());
            else if (block instanceof FluidFillable) {
                ((FluidFillable) block).tryFillWithFluid(world, pos, state, fluid.getDefaultState());
            }
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_MODE, mode.getIndex());
        tag.put(NBT_TANK, tank.toTag());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_TANK)) tank.fromTag(tag.getCompound(NBT_TANK));
        if (tag.contains(NBT_MODE))
            mode = ModularItemFrame.getConfig().tankTransferRate > 0 ? EnumMode.values()[tag.getInt(NBT_MODE)] : EnumMode.NONE;
    }

    public enum EnumMode {
        NONE(0, "modularitemframe.mode.no"),
        DRAIN(1, "modularitemframe.mode.in"),
        PUSH(2, "modularitemframe.mode.out");

        private final int index;
        private final String name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = nameIn;
        }

        public int getIndex() {
            return this.index;
        }

        @Environment(EnvType.CLIENT)
        public String getName() {
            return I18n.translate(this.name);
        }
    }
}

