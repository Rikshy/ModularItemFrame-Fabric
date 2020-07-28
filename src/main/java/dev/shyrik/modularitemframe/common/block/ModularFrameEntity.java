package dev.shyrik.modularitemframe.common.block;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.ModuleItem;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.UpgradeItem;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.common.module.EmptyModule;
import dev.shyrik.modularitemframe.common.upgrade.*;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class ModularFrameEntity extends BlockEntity implements BlockEntityClientSerializable, Tickable {

    private static final String NBTMODULE = "framemodule";
    private static final String NBTMODULEDATA = "framemoduledata";
    private static final String NBTUPGRADES = "upgrades";

    public ModuleBase module;
    List<UpgradeBase> upgrades = new ArrayList<>();
    
    public ModularFrameEntity() {
        super(Registrar.MODULAR_FRAME_ENTITY);
    }

    //region <upgrade>
    public boolean tryAddUpgrade(Identifier upgradeLoc) {
        return tryAddUpgrade(upgradeLoc, true);
    }

    public boolean tryAddUpgrade(Identifier upgradeLoc, boolean fireInsert) {
        UpgradeBase up = UpgradeItem.createUpgrade(upgradeLoc);
        if (up != null && countUpgradeOfType(up.getClass()) < up.getMaxCount()) {
            upgrades.add(up);
            if (fireInsert) {
                up.onInsert(world, pos, blockFacing());
                module.onFrameUpgradesChanged();
            }
            return true;
        }
        return false;
    }

    public boolean acceptsUpgrade() {
        return upgrades.size() <= ModularItemFrame.getConfig().MaxFrameUpgrades;
    }

    public void dropUpgrades(PlayerEntity playerIn, Direction facing) {
        for (UpgradeBase up : upgrades) {
            up.onRemove(world, pos, facing);

            ItemStack remain = new ItemStack(up.getParent());
            if (playerIn != null) remain = InventoryHelper.giveStack(playerIn.inventory, remain);
            if (!remain.isEmpty()) ItemHelper.ejectStack(world, pos, facing, remain);
        }

        module.onFrameUpgradesChanged();
        markDirty();
    }

    public int getSpeedUpCount() {
        return countUpgradeOfType(SpeedUpgrade.class);
    }

    public int getRangeUpCount() {
        return countUpgradeOfType(RangeUpgrade.class);
    }

    public int getCapacityUpCount() {
        return countUpgradeOfType(CapacityUpgrade.class);
    }

    public boolean isBlastResist() {
        return countUpgradeOfType(BlastResistUpgrade.class) >= 1;
    }

    public int countUpgradeOfType(Class<? extends UpgradeBase> clsUp) {
        int count = 0;
        for (UpgradeBase up : upgrades) {
            if (clsUp.isInstance(up)) count++;
        }
        return count;
    }
    //endregion

    //region <block>
    public Direction blockFacing() {
        return world.getBlockState(pos).get(ModularFrameBlock.FACING);
    }

    public boolean hasAttachedTile() {
        return getAttachedTile() != null;
    }

    public BlockEntity getAttachedTile() {
        return world.getBlockEntity(pos.offset(blockFacing().getOpposite()));
    }

    public Inventory getAttachedInventory() {
        BlockEntity neighbor = getAttachedTile();
        if (neighbor instanceof Inventory) {
            return (Inventory)neighbor;
        }

        return null;
    }

    public BlockState getAttachedBlock() {
        return world.getBlockState(getAttachedPos());
    }

    public BlockPos getAttachedPos() {
        return pos.offset(blockFacing());
    }

    public boolean isPowered() { return world.getReceivedRedstonePower(pos) > 0; }
    //rendregion

    //region <module>
    public void setModule(Identifier moduleLoc) {
        setModule(ModuleItem.createModule(moduleLoc));
    }

    private void setModule(ModuleBase mod) {
        module = mod == null ? new EmptyModule() : mod;
        module.setTile(this);
    }

    public boolean acceptsModule() {
        return module instanceof EmptyModule;
    }

    public void dropModule(Direction facing, PlayerEntity playerIn) {
        ItemStack remain = new ItemStack(module.getParent());

        if (playerIn != null) remain = InventoryHelper.giveStack(playerIn.inventory, remain);
        if (!remain.isEmpty()) ItemHelper.ejectStack(world, pos, facing, remain);

        module.onRemove(world, pos, facing, playerIn);
        setModule(new EmptyModule());
        markDirty();
    }
    //endregion

    @Override
    public void tick() {
        if (world.getBlockEntity(pos) != this || isPowered()) return;
        module.tick(world, pos);
    }

    //region <syncing>
//    @Override
//    public void markDirty() {
//        super.markDirty();
//        BlockState state = world.getBlockState(pos);
//        world.not.notifyBlockUpdate(pos, state, state, 1);
//        world.notifyBlockUpdate(pos, state, state, 2);
//    }

//    @Override
//    public void handleUpdateTag(CompoundTag tag) {
//        super.handleUpdateTag(tag);
//        markDirty();
//    }

//    @Override
//    public BlockEntityUpdateS2CPacket toUpdateTag() {
//        return new BlockEntityUpdateS2CPacket(pos, getType(),toTag(new CompoundTag()));
//    }

//    @Override
//    public SUpdateTileEntityPacket getUpdatePacket() {
//        return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
//        super.toUpdatePacket().onDataPacket(net, packet);
//        read(packet.getNbtCompound());
//    }

    @Override
    public CompoundTag toTag(CompoundTag compound) {
        compound = super.toTag(compound);
        compound.putString(NBTMODULE, module.getId().toString());
        compound.put(NBTMODULEDATA, module.toTag());


        ListTag upgradeList = new ListTag();
        for (UpgradeBase up : upgrades) {
            upgradeList.add(StringTag.of(up.getId().toString()));
        }
        compound.put(NBTUPGRADES, upgradeList);
        return compound;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag cmp) {
        super.fromTag(state, cmp);
        if (module.getId().toString().equals(cmp.getString(NBTMODULE))) {
            module.fromTag(cmp.getCompound(NBTMODULEDATA));
        } else {
            setModule(new Identifier(cmp.getString(NBTMODULE)));
            module.fromTag(cmp.getCompound(NBTMODULEDATA));
            cmp.remove(NBTMODULEDATA);
        }
        upgrades = new ArrayList<>();
        for (Tag sub : cmp.getList(NBTUPGRADES, 8)) {
            tryAddUpgrade(new Identifier(sub.asString()), false);
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compound) {
        compound.putString(NBTMODULE, module.getId().toString());
        compound.put(NBTMODULEDATA, module.toTag());


        ListTag upgradeList = new ListTag();
        for (UpgradeBase up : upgrades) {
            upgradeList.add(StringTag.of(up.getId().toString()));
        }
        compound.put(NBTUPGRADES, upgradeList);
        return compound;
    }

    @Override
    public void fromClientTag(CompoundTag cmp) {
        if (module.getId().toString().equals(cmp.getString(NBTMODULE))) {
            module.fromTag(cmp.getCompound(NBTMODULEDATA));
        } else {
            setModule(new Identifier(cmp.getString(NBTMODULE)));
            module.fromTag(cmp.getCompound(NBTMODULEDATA));
            cmp.remove(NBTMODULEDATA);
        }
        upgrades = new ArrayList<>();
        for (Tag sub : cmp.getList(NBTUPGRADES, 8)) {
            tryAddUpgrade(new Identifier(sub.asString()), false);
        }
    }
    //endregion
}
