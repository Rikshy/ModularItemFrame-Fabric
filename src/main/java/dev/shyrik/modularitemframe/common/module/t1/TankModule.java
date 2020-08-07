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
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_tank");

    private static final String NBT_MODE = "tank_mode";
    private static final String NBT_TANK = "tank";

    public EnumMode mode = EnumMode.NONE;
    private SimpleFixedFluidInv tank = new SimpleFixedFluidInv(1, FluidAmount.ofWhole(ModularItemFrame.getConfig().tankFrameCapacity));

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Identifier frontTexture() {
        return BG;
    }

    @Override
    public Identifier backTexture() {
        return BG;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.tank");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        FluidVolume vol = tank.getInvFluid(0);
        if (!vol.amount().isZero()) {
            double amount = (float) vol.getAmount_F().as1620() / (float) tank.getMaxAmount_F(0).as1620();
            FluidRenderFace face = null;
            switch (blockEntity.blockFacing()) {
                case UP:
                    face = FluidRenderFace.createFlatFace(0.2d, 0.08d, 0.2d, 0.8d, 0.08d, 0.2d + amount * 0.6d, 1, blockEntity.blockFacing());
                    break;
                case DOWN:
                    face = FluidRenderFace.createFlatFace(0.2f, 0.92f, 0.2f, 0.8f, 0.92f, 0.2f + amount * 0.6f, 1, blockEntity.blockFacing());
                    break;
                case NORTH:
                    face = FluidRenderFace.createFlatFace(0.2f, 0.2f, 0.92f, 0.8f, 0.2f + amount * 0.6f, 0.92f, 1, blockEntity.blockFacing());
                    break;
                case EAST:
                    face = FluidRenderFace.createFlatFace(0.08f, 0.2f, 0.2f, 0.08f, 0.2f + amount * 0.6f, 0.8f, 1, blockEntity.blockFacing());
                    break;
                case WEST:
                    face = FluidRenderFace.createFlatFace(0.92f, 0.2f, 0.2f, 0.92f, 0.2f + amount * 0.6f, 0.8f, 1, blockEntity.blockFacing());
                    break;
                case SOUTH:
                    face = FluidRenderFace.createFlatFace(0.2f, 0.2f, 0.08f, 0.8f, 0.2f + amount * 0.6f, 0.08f, 1, blockEntity.blockFacing());
                    break;
            }

            vol.render(Collections.singletonList(face), buffer, matrixStack);
        }
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            if (ModularItemFrame.getConfig().tankTransferRate > 0) {
                int modeIdx = mode.getIndex() + 1;
                if (modeIdx == EnumMode.values().length) modeIdx = 0;
                mode = EnumMode.VALUES[modeIdx];
                player.sendMessage(new TranslatableText("modularitemframe.message.mode_change", mode.getName()), false);
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        ActionResult result = FluidInvUtil.interactHandWithTank((FixedFluidInv) tank, player, hand).asActionResult();
        blockEntity.markDirty();
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (!world.isClient && mode != EnumMode.NONE && ModularItemFrame.getConfig().tankTransferRate > 0) {
            if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

            FixedFluidInv neighbor = blockEntity.getAttachedTank();
            if (neighbor != null) {
                if (mode == EnumMode.DRAIN)
                    FluidVolumeUtil.move((FluidExtractable) neighbor, tank, FluidAmount.of1620(ModularItemFrame.getConfig().tankTransferRate));
                else
                    FluidVolumeUtil.move(tank, (FluidInsertable) neighbor, FluidAmount.of1620(ModularItemFrame.getConfig().tankTransferRate));
            }
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        int newCapacity = (int) Math.pow(ModularItemFrame.getConfig().tankFrameCapacity, blockEntity.getCapacityUpCount() + 1);
        SimpleFixedFluidInv tmp = new SimpleFixedFluidInv(1, FluidAmount.of1620(newCapacity));
        tmp.insert(tank.extract(tmp.getMaxAmount_F(0).min(tank.getMaxAmount_F(0))));
        tank = tmp;
        blockEntity.markDirty();
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        super.onRemove(world, pos, facing, player);
        if (!ModularItemFrame.getConfig().dropFluidOnTankRemove && tank.getInvFluid(0).amount().isZero())
            return;
        Fluid fluid = tank.getInvFluid(0).getRawFluid();

        for ( Direction dir : Direction.values()) {
            if (dir == facing.getOpposite()) continue;
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
            mode = ModularItemFrame.getConfig().tankTransferRate > 0 ? EnumMode.VALUES[tag.getInt(NBT_MODE)] : EnumMode.NONE;
    }

    public enum EnumMode {
        NONE(0, "modularitemframe.mode.no"),
        DRAIN(1, "modularitemframe.mode.in"),
        PUSH(2, "modularitemframe.mode.out");

        public static final EnumMode[] VALUES = new EnumMode[3];

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


        static {
            for (EnumMode enummode : values())
                VALUES[enummode.index] = enummode;
        }
    }
}

