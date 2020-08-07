package dev.shyrik.modularitemframe.common.block;

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.fluid.FixedFluidInv;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.item.*;
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

    private static final String NBT_MODULE = "frame_module";
    private static final String NBT_MODULE_DATA = "frame_module_data";
    private static final String NBT_UPGRADES = "upgrades";

    public ModuleBase module;
    List<UpgradeBase> upgrades = new ArrayList<>();
    
    public ModularFrameEntity() {
        super(Registrar.MODULAR_FRAME_ENTITY);
        setModule(new EmptyModule());
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
        return upgrades.size() <= ModularItemFrame.getConfig().maxFrameUpgrades;
    }

    public void dropUpgrades(PlayerEntity player, Direction facing) {
        for (UpgradeBase up : upgrades) {
            up.onRemove(world, pos, facing);

            ItemStack remain = new ItemStack(up.getParent());
            if (player != null) remain = InventoryHelper.givePlayer(player, remain);
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
        assert world != null;
        return world.getBlockState(pos).get(ModularFrameBlock.FACING);
    }

    public boolean hasAttachedEntity() {
        return getAttachedEntity() != null;
    }

    public BlockEntity getAttachedEntity() {
        assert world != null;
        return world.getBlockEntity(getAttachedPos());
    }

    public FixedItemInv getAttachedInventory() {
        assert world != null;
        return ItemAttributes.FIXED_INV.getFirstOrNull(world, getAttachedPos(), SearchOptions.inDirection(blockFacing()));
    }

    public FixedFluidInv getAttachedTank() {
        assert world != null;
        return FluidAttributes.FIXED_INV.getFirstOrNull(world, getAttachedPos(), SearchOptions.inDirection(blockFacing()));
    }

    public BlockState getAttachedBlock() {
        assert world != null;
        return world.getBlockState(getAttachedPos());
    }

    public BlockPos getAttachedPos() {
        return pos.offset(blockFacing().getOpposite());
    }

    public boolean isPowered() {
        assert world != null;
        return world.getReceivedRedstonePower(pos) > 0; }
    //endregion

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

    public void dropModule(Direction facing, PlayerEntity player) {
        ItemStack remain = new ItemStack(module.getParent());

        if (player != null) remain = InventoryHelper.givePlayer(player, remain);
        if (!remain.isEmpty()) ItemHelper.ejectStack(world, pos, facing, remain);

        module.onRemove(world, pos, facing, player);
        setModule(new EmptyModule());
        markDirty();
    }
    //endregion

    @Override
    public void tick() {
        assert world != null;
        if (world.getBlockEntity(pos) != this || isPowered()) return;
        module.tick(world, pos);
    }

    //region <syncing>
    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient)
            sync();
    }

    @Override
    public CompoundTag toTag(CompoundTag compound) {
        compound = super.toTag(compound);
        return toClientTag(compound);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag cmp) {
        super.fromTag(state, cmp);
        fromClientTag(cmp);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compound) {
        compound.putString(NBT_MODULE, module.getId().toString());
        compound.put(NBT_MODULE_DATA, module.toTag());


        ListTag upgradeList = new ListTag();
        for (UpgradeBase up : upgrades) {
            upgradeList.add(StringTag.of(up.getId().toString()));
        }
        compound.put(NBT_UPGRADES, upgradeList);
        return compound;
    }

    @Override
    public void fromClientTag(CompoundTag cmp) {
        if (module.getId().toString().equals(cmp.getString(NBT_MODULE))) {
            module.fromTag(cmp.getCompound(NBT_MODULE_DATA));
        } else {
            setModule(new Identifier(cmp.getString(NBT_MODULE)));
            module.fromTag(cmp.getCompound(NBT_MODULE_DATA));
            cmp.remove(NBT_MODULE_DATA);
        }
        upgrades = new ArrayList<>();
        for (Tag sub : cmp.getList(NBT_UPGRADES, 8)) {
            tryAddUpgrade(new Identifier(sub.asString()), false);
        }
    }
    //endregion
}
