package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TankModule extends ModuleBase {

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t1_tank");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_tank");
    private static final String NBT_MODE = "tankmode";
    private static final String NBT_TANK = "tank";

    private int BUCKET_VOLUME = 1000;

    public EnumMode mode = EnumMode.NONE;
    private FluidTank tank = new FluidTank(ConfigValues.TankFrameCapacity);

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    public Identifier backTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.tank");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (tank != null && tank.getFluidAmount() > 0) {
            FluidStack fluid = tank.getFluid();
            float amount = (float) tank.getFluidAmount() / (float) tank.getCapacity();

            switch (blockEntity.blockFacing()) {
                case UP:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.2f, 0.08f, 0.2f, 0.8f, 0.08f, 0.2f + amount * 0.6f);
                    break;
                case DOWN:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.2f, 0.92f, 0.2f, 0.8f, 0.92f, 0.2f + amount * 0.6f);
                    break;
                case NORTH:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.2f, 0.2f, 0.92f, 0.8f, 0.2f + amount * 0.6f, 0.92f);
                    break;
                case EAST:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.08f, 0.2f, 0.2f, 0.08f, 0.2f + amount * 0.6f, 0.8f);
                    break;
                case WEST:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.92f, 0.2f, 0.2f, 0.92f, 0.2f + amount * 0.6f, 0.8f);
                    break;
                case SOUTH:
                    FrameFluidRenderer.renderFluidCuboid(fluid, matrixStack, buffer, combinedLight, 0.2f, 0.2f, 0.08f, 0.8f, 0.2f + amount * 0.6f, 0.08f);
                    break;
            }
        }
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity playerIn, ItemStack driver) {
        if (!world.isClient) {
            if (ConfigValues.TankTransferRate > 0) {
                int modeIdx = mode.getIndex() + 1;
                if (modeIdx == EnumMode.values().length) modeIdx = 0;
                mode = EnumMode.VALUES[modeIdx];
                playerIn.sendMessage(new TranslationTextComponent("modularitemframe.message.mode_change", mode.getName()));
            }
        }
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        ItemStack stack = playerIn.getStackInHand(hand);
        FluidUtil.interactWithFluidHandler(playerIn, hand, tank);
        blockEntity.markDirty();
        return FluidUtil.getFluidHandler(stack) != null ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    @Override
    public void tick( World world, BlockPos pos) {
        if (!world.isClient && mode != EnumMode.NONE && ConfigValues.TankTransferRate > 0) {
            if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

            BlockEntity neighbor = blockEntity.getAttachedTile();
            if (neighbor != null) {
                FluidHandler handler = blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, blockEntity.blockFacing().getOpposite()).orElse(null);
                if (handler != null) {
                    if (mode == EnumMode.DRAIN)
                        FluidUtil.tryFluidTransfer(tank, handler, ConfigValues.TankTransferRate, true);
                    else FluidUtil.tryFluidTransfer(handler, tank, ConfigValues.TankTransferRate, true);
                    blockEntity.markDirty();
                }
            }
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        int newCapacity = (int) Math.pow(ConfigValues.TankFrameCapacity / (float) BUCKET_VOLUME, blockEntity.getCapacityUpCount() + 1) * BUCKET_VOLUME;
        tank.setCapacity(newCapacity);
        blockEntity.markDirty();
    }

    @Override
    public void onRemove(World worldIn, BlockPos pos, Direction facing, PlayerEntity playerIn) {
        super.onRemove(worldIn, pos, facing, playerIn);
        for ( Direction face : Direction.values()) {
            if (face == facing.getOpposite()) continue;
            if (FluidUtil.tryPlaceFluid(null, worldIn, Hand.MAIN_HAND, pos.offset(facing.getOpposite()), tank, tank.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE)))
                tank.drain(BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag nbt = super.toTag();
        nbt.putInt(NBT_MODE, mode.getIndex());
        nbt.put(NBT_TANK, tank.toTag(new CompoundTag()));
        return nbt;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_TANK)) tank.fromTag(nbt.getCompound(NBT_TANK));
        if (nbt.contains(NBT_MODE))
            mode = ConfigValues.TankTransferRate > 0 ? EnumMode.VALUES[nbt.getInt(NBT_MODE)] : EnumMode.NONE;
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

