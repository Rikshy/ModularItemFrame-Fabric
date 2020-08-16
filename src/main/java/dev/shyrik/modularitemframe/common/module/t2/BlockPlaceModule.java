package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
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
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");

    public static class FrameItemPlacementContext extends ItemPlacementContext {

        private final BlockPos placementPos;

        public FrameItemPlacementContext(World world, ItemStack itemStack, BlockHitResult blockHitResult) {
            super(world, null, null, itemStack, blockHitResult);
            this.canReplaceExisting = true;
            this.placementPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            this.canReplaceExisting = world.getBlockState(blockHitResult.getBlockPos()).canReplace(this);
        }

//        public static FrameItemPlacementContext create(World world, ItemStack itemStack, BlockPos blockPos, Direction direction ) {
//            return new FrameItemPlacementContext(world, null, null, itemStack, new BlockHitResult(
//                    new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
//                    direction,
//                    blockPos,
//                    true)
//            );
//        }

        public BlockPos getBlockPos() {
            return this.canReplaceExisting ? super.getBlockPos() : this.placementPos;
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
        FixedItemInv attInventory = this.blockEntity.getAttachedInventory();
        ItemStack itemToPlace = attInventory.getExtractable().extract(1);

        if (itemToPlace.getItem() instanceof BlockItem ) {

            BlockPos placePos = pos.offset(this.blockEntity.getFacing(), 1);

            ActionResult placeResult = ((BlockItem) itemToPlace.getItem()).place( new FrameItemPlacementContext(
                    world,
                    itemToPlace,
                    new BlockHitResult(
                            new Vec3d(
                                    placePos.getX(),
                                    placePos.getY(),
                                    placePos.getZ()),
                            this.blockEntity.getFacing(),
                            placePos,
                            true)
                    )
            );

        }
    }
}

