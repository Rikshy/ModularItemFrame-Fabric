package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class BlockBreakModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_break");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");

    private int breakProgress = 0;
    private BlockState lastTarget = null;
    private BlockPos lastPos = null;
    private int breakId = -1;

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
        return I18n.translate("modularitemframe.module.block_breaker");
    }

    @Override
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        //TODO implement fancy tool moving animation
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        return ActionResult.PASS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient) return;
        if (blockEntity.isPowered()) return;

        BlockPos targetPos;
        BlockState targetState;
        int offset = 1;

        do {
            targetPos = pos.offset(blockEntity.blockFacing(), offset);
            targetState = world.getBlockState(targetPos);
        } while (targetState.isAir() && offset++ <= blockEntity.getRangeUpCount());

        float hardness = targetState.getHardness(world, targetPos);

        if (targetState.isAir() || hardness < 0) {
            breakProgress = 0;
            lastTarget = null;
            lastPos = null;
            return;
        }

        if (targetState != lastTarget || !targetPos.equals(lastPos)) {
            breakProgress = 0;
            lastTarget = targetState;
            lastPos = targetPos;
            breakId = world.random.nextInt();
        }

        if (world.getTime() % Math.max(20 * hardness - 10 * blockEntity.getSpeedUpCount(), 1) != 0) return;

        if (++breakProgress >= 10) {
            FixedItemInv inv = blockEntity.getAttachedInventory();
            boolean drop = true;
            if (inv != null) {
                drop = false;

                BlockEntity targetEntity = targetState.getBlock().hasBlockEntity() ? world.getBlockEntity(pos) : null;
                List<ItemStack> drops = Block.getDroppedStacks(targetState,(ServerWorld) world, targetPos, targetEntity);
                for (ItemStack dropStack : drops) {
                    ItemStack remain = inv.getInsertable().insert(dropStack);
                    if (!remain.isEmpty()) {
                        ItemHelper.ejectStack(world, pos, blockEntity.blockFacing(), remain);
                    }
                }
            }

            world.breakBlock(targetPos, drop);
        } else {
            world.setBlockBreakingInfo(breakId, targetPos, breakProgress);
        }
    }
}
