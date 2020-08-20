package dev.shyrik.modularitemframe.common.module.t1;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;

public class NullifyModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t1_null");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_null");

    private static final String NBT_LAST_STACK = "last_stack";

    private ItemStack lastStack = ItemStack.EMPTY;

    private final FluidVolume lava = FluidKeys.get(Fluids.LAVA).withAmount(FluidAmount.ONE);

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
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.null");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        FluidRenderFace face = null;
        switch (frame.getFacing()) {
            case UP:
                face = FluidRenderFace.createFlatFace(0.3f, 0.07f, 0.3f, 0.7f, 0.07f, 0.7f, 1, frame.getFacing());
                break;
            case DOWN:
                face = FluidRenderFace.createFlatFace(0.3f, 0.93f, 0.3f, 0.7f, 0.93f, 0.7f, 1, frame.getFacing());
                break;
            case NORTH:
                face = FluidRenderFace.createFlatFace(0.3f, 0.3f, 0.93f, 0.7f, 0.7f, 0.93f, 1, frame.getFacing());
                break;
            case EAST:
                face = FluidRenderFace.createFlatFace(0.07f, 0.3f, 0.3f, 0.07f, 0.7f, 0.7f, 1, frame.getFacing());
                break;
            case WEST:
                face = FluidRenderFace.createFlatFace(0.93f, 0.3f, 0.3f, 0.93f, 0.7f, 0.7f, 1, frame.getFacing());
                break;
            case SOUTH:
                face = FluidRenderFace.createFlatFace(0.3f, 0.3f, 0.07f, 0.7f, 0.7f, 0.07f, 1, frame.getFacing());
                break;
        }

        lava.render(Collections.singletonList(face), buffer, matrixStack);
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (!player.isSneaking() && !held.isEmpty()) {
                if (ItemHelper.simpleAreStacksEqual(held, lastStack)) {
                    if (held.getCount() + lastStack.getCount() > lastStack.getMaxCount())
                        lastStack.setCount(lastStack.getMaxCount());
                    else lastStack.increment(held.getCount());
                } else {
                    lastStack = held.copy();
                }
                held.setCount(0);
                NetworkHandler.sendAround(
                        world,
                        frame.getPos(),
                        32,
                        new PlaySoundPacket(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.4F, 0.7F));
            } else if (player.isSneaking() && held.isEmpty() && !lastStack.isEmpty()) {
                player.setStackInHand(hand, lastStack);
                lastStack = ItemStack.EMPTY;
                NetworkHandler.sendAround(
                        world,
                        frame.getPos(),
                        32,
                        new PlaySoundPacket(pos, SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.BLOCKS, 0.4F, 0.7F));
            }
        }
        return ActionResult.SUCCESS;
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
