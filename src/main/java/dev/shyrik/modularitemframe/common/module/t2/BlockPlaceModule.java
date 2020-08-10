package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public class BlockPlaceModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_place");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");



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
        return I18n.translate("modularitemframe.module.block_placer");
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        return ActionResult.PASS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        FixedItemInv attInventory = this.blockEntity.getAttachedInventory();
        BlockPos placePos = this.blockEntity.getPos();
        Item itemToPlace = attInventory.getExtractable().extract(1).getItem();

        if (itemToPlace != null ) {

            placePos.offset(this.blockEntity.blockFacing(), 1);
            if (world.canSetBlock(placePos) && itemToPlace instanceof BlockItem) {
                world.setBlockState(placePos, ((BlockItem) itemToPlace).getBlock().getDefaultState());
                
            }

        }
    }
}
