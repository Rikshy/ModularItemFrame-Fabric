package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.util.ItemHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DispenseModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_dispense");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_dispense");
    private static final Text NAME = new TranslatableText("modularitemframe.module.dispense");

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
    public Text getName() {
        return NAME;
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (!held.isEmpty()) {
                ItemHelper.ejectStack(world, pos, facing.getOpposite(), held.copy());
                held.setCount(0);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient || frame.isPowered() || !canTick(world,60, 10)) return;

        FixedItemInv inventory = frame.getAttachedInventory();
        if (inventory != null) {
            ItemStack extracted = inventory.getExtractable().filtered(frame.getItemFilter()).extract(1);
            if (!extracted.isEmpty()) {
                ItemHelper.ejectStack(world, pos, frame.getFacing(), extracted);
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
            }
        }
    }
}
