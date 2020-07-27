package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
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

public class NullifyModule extends ModuleBase {
    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t1_null");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_null");
    private static final String NBT_LASTSTACK = "laststack";

    private ItemStack lastStack = ItemStack.EMPTY;

    private final FluidStack lavaStack;
    private final TextureAtlasSprite still;
    private final TextureAtlasSprite flowing;

    @Override
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        switch (blockEntity.blockFacing()) {
            case UP:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.3f, 0.07f, 0.3f, 0.7f, 0.07f, 0.7f);
                break;
            case DOWN:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.3f, 0.93f, 0.3f, 0.7f, 0.93f, 0.7f);
                break;
            case NORTH:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.3f, 0.3f, 0.93f, 0.7f, 0.7f, 0.93f);
                break;
            case EAST:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.07f, 0.3f, 0.3f, 0.07f, 0.7f, 0.7f);
                break;
            case WEST:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.93f, 0.3f, 0.3f, 0.93f, 0.7f, 0.7f);
                break;
            case SOUTH:
                FrameFluidRenderer.renderFluidCuboid(lavaStack, matrixStack, buffer, combinedLight, 0.3f, 0.3f, 0.07f, 0.7f, 0.7f, 0.07f);
                break;
        }
    }

    public NullifyModule() {
        super();
        lavaStack = new FluidStack(Fluids.LAVA, 1000);
        still = RandomUtils.getSprite(Fluids.LAVA.getAttributes().getStillTexture());//Minecraft.getInstance().getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).getSprite(lava.getAttributes().getStillTexture());
        flowing = RandomUtils.getSprite(Fluids.LAVA.getAttributes().getFlowingTexture());// ModelBakery.LOCATION_LAVA_FLOW.getSprite();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.null");
    }

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
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (!worldIn.isClient) {
            ItemStack held = playerIn.getStackInHand(hand);
            if (!playerIn.isSneaking() && !held.isEmpty()) {
                if (ItemHelper.simpleAreStacksEqual(held, lastStack)) {
                    if (held.getCount() + lastStack.getCount() > lastStack.getMaxCount())
                        lastStack.setCount(lastStack.getMaxCount());
                    else lastStack.increment(held.getCount());
                } else {
                    lastStack = held.copy();
                }
                held.setCount(0);
                NetworkHandler.sendAround(new PlaySoundPacket(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH.getId(), SoundCategory.BLOCKS.getName(), 0.4F, 0.7F), worldIn, blockEntity.getPos(), 32);
            } else if (playerIn.isSneaking() && held.isEmpty() && !lastStack.isEmpty()) {
                playerIn.setStackInHand(hand, lastStack);
                lastStack = ItemStack.EMPTY;
                NetworkHandler.sendAround(new PlaySoundPacket(pos, SoundEvents.ENTITY_ENDER_PEARL_THROW.getId(), SoundCategory.BLOCKS.getName(), 0.4F, 0.7F), worldIn, blockEntity.getPos(), 32);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = super.toTag();
        compound.put(NBT_LASTSTACK, lastStack.toTag(new CompoundTag));
        return compound;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_LASTSTACK)) lastStack = ItemStack.fromTag(nbt.getCompound(NBT_LASTSTACK));
    }
}
