package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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

import java.util.List;

public class TrashCanModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_trashcan");
    public static final Identifier BG1 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_trashcan_1");
    public static final Identifier BG2 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_trashcan_2");
    public static final Identifier BG3 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_trashcan_3");
    private static final Text NAME = new TranslatableText("modularitemframe.module.trash_can");

    private final List<Identifier> frontTex = ImmutableList.of(
            BG1,
            BG2,
            BG3
    );

    private static final String NBT_LAST_STACK = "last_stack";

    private ItemStack lastStack = ItemStack.EMPTY;
    private int texIndex = 0;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return frontTex.get(texIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<Identifier> getVariantFronts() {
        return frontTex;
    }

    @Override
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getModuleName() {
        return NAME;
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (!player.isSneaking() && !held.isEmpty() && frame.getItemFilter().matches(held)) {
                if (ItemHelper.simpleAreStacksEqual(held, lastStack)) {
                    if (held.getCount() + lastStack.getCount() > lastStack.getMaxCount())
                        lastStack.setCount(lastStack.getMaxCount());
                    else lastStack.increment(held.getCount());
                } else {
                    lastStack = held.copy();
                }
                held.setCount(0);
                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 1F, 0.7F);
            } else if (player.isSneaking() && held.isEmpty() && !lastStack.isEmpty()) {
                player.setStackInHand(hand, lastStack);
                lastStack = ItemStack.EMPTY;
                world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 1F, 0.7F);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (frame.isPowered()) return;

        if (!world.isClient) {
            if (!canTick(world,60, 10)) return;

            FixedItemInv trash = frame.getAttachedInventory();
            if (trash != null) {
                for (int slot = 0; slot < trash.getSlotCount(); slot++) {
                    if (!trash.getInvStack(slot).isEmpty() && frame.getItemFilter().matches(trash.getInvStack(slot))) {
                        trash.setInvStack(slot, ItemStack.EMPTY, Simulation.ACTION);
                        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 1F, 0.7F);
                        break;
                    }
                }
            }
        } else {
            if (world.getTime() % 10 == 0) {
                texIndex = texIndex < frontTex.size() - 1 ? texIndex + 1 : 0;
            }
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_LAST_STACK, lastStack.toTag(new CompoundTag()));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_LAST_STACK)) lastStack = ItemStack.fromTag(tag.getCompound(NBT_LAST_STACK));
    }
}
