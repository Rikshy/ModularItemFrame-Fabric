package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.SpawnParticlesPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_vacuum");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_vacuum");
    
    private static final String NBT_MODE = "range_mode";
    private static final String NBT_RANGE_X = "range_x";
    private static final String NBT_RANGE_Y = "range_y";
    private static final String NBT_RANGE_Z = "range_z";

    private EnumMode mode = EnumMode.X;
    private int rangeX = ModularItemFrame.getConfig().vacuumRange;
    private int rangeY = ModularItemFrame.getConfig().vacuumRange;
    private int rangeZ = ModularItemFrame.getConfig().vacuumRange;

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
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.vacuum");
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (world.isClient) return;

        if (player.isSneaking()) {
            int modeIdx = mode.getIndex() + 1;
            if (modeIdx == EnumMode.values().length) modeIdx = 0;
            mode = EnumMode.VALUES[modeIdx];
            player.sendMessage(new TranslatableText("modularitemframe.message.vacuum_mode_change", mode.getName()), false);
        } else {
            adjustRange(player);
        }
        blockEntity.markDirty();
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0 && world.isClient) return;

        FixedItemInv handler = blockEntity.getAttachedInventory();
        if (handler != null) {
            List<ItemEntity> entities = world.getEntitiesByClass(ItemEntity.class, getVacuumBox(pos), itemEntity -> true);
            for (ItemEntity entity : entities) {
                ItemStack entityStack = entity.getStack();
                ItemInsertable inserter = handler.getInsertable();
                if (!entity.isAlive() ||
                        entityStack.isEmpty() ||
                        inserter.attemptInsertion(entityStack, Simulation.SIMULATE).getCount() == entityStack.getCount())
                    continue;

                ItemStack remain = inserter.insert(entityStack);
                if (remain.isEmpty()) entity.remove();
                else entity.setStack(remain);
                NetworkHandler.sendAround(
                        entity.world,
                        entity.getBlockPos(),
                        32,
                        new SpawnParticlesPacket(ParticleTypes.EXPLOSION, entity.getBlockPos(), 1));
                break;
            }
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        super.onFrameUpgradesChanged();

        int maxRange = ModularItemFrame.getConfig().vacuumRange + blockEntity.getRangeUpCount();
        rangeX = Math.min(rangeX, maxRange);
        rangeY = Math.min(rangeY, maxRange);
        rangeZ = Math.min(rangeZ, maxRange);
    }


    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_MODE, mode.getIndex());
        tag.putInt(NBT_RANGE_X, rangeX);
        tag.putInt(NBT_RANGE_Y, rangeY);
        tag.putInt(NBT_RANGE_Z, rangeZ);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_MODE)) mode = EnumMode.VALUES[tag.getInt(NBT_MODE)];
        if (tag.contains(NBT_RANGE_X)) rangeX = tag.getInt(NBT_RANGE_X);
        if (tag.contains(NBT_RANGE_Y)) rangeY = tag.getInt(NBT_RANGE_Y);
        if (tag.contains(NBT_RANGE_Z)) rangeZ = tag.getInt(NBT_RANGE_Z);
    }

    private Box getVacuumBox(BlockPos pos) {
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

    private void adjustRange(PlayerEntity player) {
        int maxRange = ModularItemFrame.getConfig().vacuumRange + blockEntity.getRangeUpCount();
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
            player.sendMessage(new TranslatableText("modularitemframe.message.vacuum_range_change", mode.getName(), r), false);
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
