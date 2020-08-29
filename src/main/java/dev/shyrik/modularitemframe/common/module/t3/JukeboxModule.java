package dev.shyrik.modularitemframe.common.module.t3;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public class JukeboxModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_jukebox");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_nyi");

    private static final String NBT_JUKEBOX = "jukebox";
    private static final String NBT_CURRENT = "current_song";

    private DirectFixedItemInv jukebox = new DirectFixedItemInv(9);
    private int currentSong = -1;
    private int rotation = 0;

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
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.jukebox");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        if (currentSong > 0 && currentSong < jukebox.getSlotCount()) {
            renderer.renderInside(jukebox.getInvStack(currentSong), rotation, matrixStack, buffer, light, overlay);
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (held.getItem() instanceof MusicDiscItem) {
                if (jukebox.attemptInsertion(held, Simulation.SIMULATE).isEmpty()) {
                    jukebox.insert(held.split(1));
                    markDirty();
                }
            } else if (held.isEmpty() && player.isSneaking()) {
                ItemStack ejectStack;
                if (currentSong >= 0) {
                    ejectStack = jukebox.extract(currentSong, 1);
                    stop(world);
                } else {
                    ejectStack = jukebox.extract(1);
                }
                if (!ejectStack.isEmpty())
                    ItemHelper.ejectStack(world, pos, frame.getFacing(), ejectStack);
                markDirty();
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (player.isSneaking()) {
            playPrevious(world);
        } else {
            playNext(world);
        }
        markDirty();
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient) {
            if (rotation >= 360) {
                rotation = 0;
            } else {
                rotation += 5;
            }
        }
    }

    @Override
    public void onFrameUpgradesChanged() {
        int newCapacity = 9 * (frame.getCapacityUpCount() + 1);
        DirectFixedItemInv tmp = new DirectFixedItemInv(newCapacity);
        for (int slot = 0; slot < jukebox.getSlotCount(); slot++) {
            if (slot < tmp.getSlotCount())
                tmp.setInvStack(slot, jukebox.getInvStack(slot), Simulation.ACTION);
            else
                ItemHelper.ejectStack(frame.getWorld(), frame.getPos(), frame.getFacing(), jukebox.getInvStack(slot));
        }
        jukebox = tmp;

        if (currentSong >= jukebox.getSlotCount()) {
            stop(Objects.requireNonNull(frame.getWorld()));
            currentSong = -1;
        }

        markDirty();
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        stop(world);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_JUKEBOX, jukebox.toTag());
        tag.putInt(NBT_CURRENT, currentSong);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_JUKEBOX)) jukebox.fromTag(tag.getCompound(NBT_JUKEBOX));
        if (tag.contains(NBT_CURRENT)) currentSong = tag.getInt(NBT_CURRENT);
    }

    private void stop(World world) {
        currentSong = -1;
        world.syncWorldEvent(1010, frame.getPos(), 0);
    }

    private void play(World world, ItemStack songStack) {
        world.syncWorldEvent(1010, frame.getPos(), Item.getRawId(songStack.getItem()));
    }

    private void playNext(World world) {
        int prevSong = currentSong;
        ItemStack songStack;
        do {
            if (currentSong + 1 >= jukebox.getSlotCount()) {
                currentSong = -1;
            }
            songStack = jukebox.getInvStack(++currentSong);
        } while (songStack.isEmpty() && prevSong != currentSong);

        if (songStack.isEmpty())
            return;

        if (prevSong != currentSong)
            play(world, songStack);
    }

    private void playPrevious(World world) {
        int prevSong = currentSong;
        ItemStack songStack;
        do {
            if (currentSong - 1 <= 0) {
                currentSong = jukebox.getSlotCount();
            }
            songStack = jukebox.getInvStack(--currentSong);
        } while (songStack.isEmpty() && prevSong != currentSong);

        if (songStack.isEmpty())
            return;

        if (prevSong != currentSong)
            play(world, songStack);
    }
}
