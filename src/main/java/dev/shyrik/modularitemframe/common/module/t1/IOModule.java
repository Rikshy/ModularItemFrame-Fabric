package dev.shyrik.modularitemframe.common.module.t1;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.util.InventoryHelper;
import dev.shyrik.modularitemframe.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
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

public class IOModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_io");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t1_io");
    private static final Text NAME = new TranslatableText("modularitemframe.module.io");

    private static final String NBT_LAST = "last_click";
    private static final String NBT_LAST_STACK = "last_stack";
    private static final String NBT_DISPLAY = "display";

    private long lastClick;
    private ItemStack displayItem = ItemStack.EMPTY;
    private ItemStack lastStack = ItemStack.EMPTY;

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
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        renderer.renderInside(displayItem, matrixStack, buffer, light, overlay);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getName() {
        return NAME;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            FixedItemInv handler = frame.getAttachedInventory();
            if (handler != null) {
                ItemExtractable extractor = handler.getExtractable().filtered(frame.getItemFilter());
                ItemStack attempt = extractor.attemptAnyExtraction(1, Simulation.SIMULATE);
                if (!attempt.isEmpty()) {
                    int amount = player.isSneaking() ? attempt.getMaxCount() : 1;
                    ItemStack extract = extractor.extract(amount);
                    extract = InventoryHelper.givePlayer(player, extract);
                    if (!extract.isEmpty()) ItemHelper.ejectStack(world, pos, frame.getFacing(), extract);
                    lastStack = extractor.attemptAnyExtraction(1, Simulation.SIMULATE);
                    markDirty();
                }
            }
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            FixedItemInv handler = frame.getAttachedInventory();
            if (handler != null) {
                ItemStack held = player.getStackInHand(hand);
                ItemInsertable insertable = handler.getInsertable().filtered(frame.getItemFilter());
                long time = world.getTime();

                if (time - lastClick <= 8L && !player.isSneaking() && !lastStack.isEmpty())
                    InventoryHelper.giveAllPossibleStacks(insertable, player.inventory, lastStack, held);
                else if (!held.isEmpty()) {
                    ItemStack heldCopy = held.copy();
                    heldCopy.setCount(1);
                    if (insertable.insert(heldCopy).isEmpty()){
                        held.decrement(1);

                        lastStack = heldCopy;
                        lastClick = time;
                    }
                }

                markDirty();
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if(!world.isClient) {
            FixedItemInv handler = frame.getAttachedInventory();
            if (handler != null) {
                ItemStack attempt = handler.getExtractable()
                        .filtered(frame.getItemFilter())
                        .attemptAnyExtraction(1, Simulation.SIMULATE);
                if (!ItemStack.areItemsEqual(attempt, displayItem)) {
                    displayItem = attempt;
                    markDirty();
                }
            }
        }
    }

    @Override
    public  CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_LAST, lastClick);
        tag.put(NBT_LAST_STACK, lastStack.toTag(new CompoundTag()));
        tag.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains(NBT_LAST)) lastClick = tag.getLong(NBT_LAST);
        if (tag.contains(NBT_LAST_STACK)) lastStack = ItemStack.fromTag(tag.getCompound(NBT_LAST_STACK));
        if (tag.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(tag.getCompound(NBT_DISPLAY));
    }
}
