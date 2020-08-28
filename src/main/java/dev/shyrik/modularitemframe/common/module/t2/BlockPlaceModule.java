package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockPlaceModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_place");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_nyi");

    public static class FrameItemPlacementContext extends ItemPlacementContext {
        public FrameItemPlacementContext(World world, ItemStack itemStack, BlockPos placePos, Direction direction) {
            super(world, null, null, itemStack, new BlockHitResult(
                    new Vec3d(
                            placePos.getX(),
                            placePos.getY(),
                            placePos.getZ()),
                    direction,
                    placePos,
                    true));
        }
    }

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
        if (world.isClient || !canTick(world,60, 10)) return;
        FixedItemInv inventory = frame.getAttachedInventory();
        if (inventory == null) return;

        ItemStack itemToPlace = inventory.getExtractable().attemptExtraction(
                new ItemClassFilter(BlockItem.class), 1, Simulation.SIMULATE);

        if (!itemToPlace.isEmpty()) {
            Direction facing = frame.getFacing();

            for (int offset = 0; offset <= Math.pow(frame.getRangeUpCount() + 1, 2); offset++) {
                BlockPos placePos = pos.offset(facing, offset);

                ActionResult placeResult = ((BlockItem) itemToPlace.getItem()).place(
                        new FrameItemPlacementContext(world, itemToPlace, placePos, facing)
                );

                if (placeResult.isAccepted()) {
                    inventory.getExtractable().extract(1);
                    break;
                }
            }
        }
    }
}

