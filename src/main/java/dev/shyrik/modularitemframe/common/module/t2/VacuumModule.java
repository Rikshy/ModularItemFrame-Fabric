package dev.shyrik.modularitemframe.common.module.t2;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.SpawnParticlesPacket;
import dev.shyrik.modularitemframe.init.ModularItemFrameConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class VacuumModule extends ModuleBase {

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t2_vacuum");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_vacuum");
    private static final String NBT_MODE = "rangemode";
    private static final String NBT_RANGEX = "rangex";
    private static final String NBT_RANGEY = "rangey";
    private static final String NBT_RANGEZ = "rangez";

    private EnumMode mode = EnumMode.X;
    private int rangeX = ModularItemFrame.getConfig().BaseVacuumRange;
    private int rangeY = ModularItemFrame.getConfig().BaseVacuumRange;
    private int rangeZ = ModularItemFrame.getConfig().BaseVacuumRange;

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.vacuum");
    }

    @Override
    public void screw(World world,  BlockPos pos,  PlayerEntity playerIn, ItemStack driver) {
        if (world.isClient) return;

        if (playerIn.isSneaking()) {
            int modeIdx = mode.getIndex() + 1;
            if (modeIdx == EnumMode.values().length) modeIdx = 0;
            mode = EnumMode.VALUES[modeIdx];
            playerIn.sendMessage(new TranslatableText("modularitemframe.message.vacuum_mode_change", mode.getName()), false);
        } else {
            adjustRange(playerIn);
        }
        blockEntity.markDirty();
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick( World world,  BlockPos pos) {
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

        Inventory handler = (Inventory) blockEntity.getAttachedInventory();
        if (handler != null) {
            List<ItemEntity> entities = world.getEntities(ItemEntity.class, getVacuumBB(pos));
            for (ItemEntity entity : entities) {
                ItemStack entityStack = entity.getStack();
                if (!entity.isAlive() || entityStack.isEmpty() || InventoryHelper.getFittingSlot(handler, entityStack) < 0)
                    continue;

                ItemStack remain = InventoryHelper.giveStack(handler, entityStack);
                if (remain.isEmpty()) entity.remove();
                else entity.setStack(remain);
                NetworkHandler.sendAround(new SpawnParticlesPacket(ParticleTypes.EXPLOSION.getRegistryName(), entity.getPos(), 1), entity.world, entity.getPos(), 32);
                break;
            }
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        int maxRange = ModularItemFrame.getConfig().BaseVacuumRange + blockEntity.getRangeUpCount();
        rangeX = Math.min(rangeX, maxRange);
        rangeY = Math.min(rangeY, maxRange);
        rangeZ = Math.min(rangeZ, maxRange);
    }


    @Override
    public CompoundTag toTag() {
        CompoundTag nbt = super.toTag();
        nbt.putInt(NBT_MODE, mode.getIndex());
        nbt.putInt(NBT_RANGEX, rangeX);
        nbt.putInt(NBT_RANGEY, rangeY);
        nbt.putInt(NBT_RANGEZ, rangeZ);
        return nbt;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_MODE)) mode = EnumMode.VALUES[nbt.getInt(NBT_MODE)];
        if (nbt.contains(NBT_RANGEX)) rangeX = nbt.getInt(NBT_RANGEX);
        if (nbt.contains(NBT_RANGEY)) rangeY = nbt.getInt(NBT_RANGEY);
        if (nbt.contains(NBT_RANGEZ)) rangeZ = nbt.getInt(NBT_RANGEZ);
    }

    // vorher AxisAlignedBB jetzt Box
    private Box getVacuumBB(BlockPos pos) {
        switch (blockEntity.blockFacing()) {
            case DOWN:
                return new Box(pos.add(-rangeX, 0, -rangeZ), pos.add(rangeX, -rangeY, rangeZ));
            case UP:
                return new Box(pos.add(-rangeX, 0, -rangeZ), pos.add(rangeX, rangeY, rangeZ));
            case NORTH:
                return new Box(pos.add(-rangeX, -rangeY, 0), pos.add(rangeX, rangeY, -rangeZ));
            case SOUTH:
                return new Box(pos.add(-rangeX, -rangeY, 0), pos.add(rangeX, rangeY, rangeZ));
            case WEST:
                return new Box(pos.add(0, -rangeY, -rangeZ), pos.add(rangeX, rangeY, rangeZ));
            case EAST:
                return new Box(pos.add(0, -rangeY, -rangeZ), pos.add(-rangeX, rangeY, rangeZ));
        }
        return new Box(pos, pos.add(1, 1, 1));
    }

    private void adjustRange( PlayerEntity playerIn) {
        int maxRange = ModularItemFrame.getConfig().BaseVacuumRange + blockEntity.getRangeUpCount();
        if (maxRange > 1) {
            int r = 0;
            switch (mode) {
                case X:
                    rangeX++;
                    if (rangeX > maxRange) rangeX = 1;
                    r = rangeX;
                    break;
                case Y:
                    rangeY++;
                    if (rangeY > maxRange) rangeY = 1;
                    r = rangeY;
                    break;
                case Z:
                    rangeZ++;
                    if (rangeZ > maxRange) rangeZ = 1;
                    r = rangeZ;
                    break;
            }
            playerIn.sendMessage(new TranslatableText("modularitemframe.message.vacuum_range_change", mode.getName(), r), false);
        }
    }

    public enum EnumMode {
        X(0, "x"), Y(1, "y"), Z(2, "z");

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

        public String getName() {
            return this.name;
        }


        static {
            for (EnumMode enummode : values())
                VALUES[enummode.index] = enummode;
        }
    }
}
