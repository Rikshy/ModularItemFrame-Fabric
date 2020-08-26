package dev.shyrik.modularitemframe.common.block;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleItem;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import dev.shyrik.modularitemframe.api.UpgradeItem;
import dev.shyrik.modularitemframe.common.item.ScrewdriverItem;
import dev.shyrik.modularitemframe.common.module.EmptyModule;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.List;

public class ModularFrameBlock extends Block implements BlockEntityProvider  {

    public static final DirectionProperty FACING = DirectionProperty.of("facing", Direction.values());
    public static final AbstractBlock.Settings DEFAULT_SETTINGS = AbstractBlock.Settings
            .of(Material.WOOD)
            .sounds(BlockSoundGroup.WOOD)
            .strength(4)
            .nonOpaque();

    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 1.75, 14);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(2, 14.25, 2, 14, 16, 14);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(14, 2, 16, 2, 14, 14.25);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(2, 2, 0, 14, 14, 1.75);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 2, 2, 1.75, 14, 14);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(16, 2, 2, 14.25, 14, 14);

    public static final Identifier INNER_DEF = new Identifier(ModularItemFrame.MOD_ID, "block/hard_inner");
    public static final Identifier INNER_HARD = new Identifier(ModularItemFrame.MOD_ID, "block/hard_inner");
    public static final Identifier INNER_HARDEST = new Identifier(ModularItemFrame.MOD_ID, "block/hardest_inner");

    //region <initialize>
    public ModularFrameBlock(AbstractBlock.Settings props) {
        super(props);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case UP:
                return UP_SHAPE;
            case DOWN:
                return DOWN_SHAPE;
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case EAST:
                return EAST_SHAPE;
            case WEST:
                return WEST_SHAPE;
        }

        return super.getOutlineShape(state, world, pos, context);
    }
    //endregion

    //region <block-entity>
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ModularFrameEntity();
    }

    private ModularFrameEntity getBE(World world, BlockPos pos) {
        return (ModularFrameEntity) world.getBlockEntity(pos);
    }
    //endregion

    //region <interaction>
    @Override
    @SuppressWarnings("deprecation")
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        getBE(world, pos).module.onBlockClicked(world, pos, player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result;
        ModularFrameEntity blockEntity = getBE(world, pos);
        ItemStack handStack = player.getStackInHand(hand);
        Item handItem = handStack.getItem();
        Direction side = hit.getSide();
        if (handItem instanceof ScrewdriverItem) {
            if (!world.isClient) {
                if (side == state.get(FACING)) {
                    if (hitModule(side, pos, hit.getPos())) {
                        switch (ScrewdriverItem.getMode(handStack)) {
                            case REMOVE_MOD:
                                blockEntity.dropModule(player, side);
                                break;
                            case REMOVE_UP:
                                blockEntity.dropUpgrades(player, side);
                                break;
                            case INTERACT:
                                blockEntity.module.screw(world, pos, player, handStack);
                                break;
                        }

                        blockEntity.markDirty();
                    }
                }
            }
            result = ActionResult.SUCCESS;
        } else if (handItem instanceof ModuleItem && blockEntity.acceptsModule()) {
            if (!world.isClient) {
                blockEntity.setModule(((ModuleItem) handItem).createModule(), handStack);
                if (!player.isCreative()) player.getStackInHand(hand).decrement(1);
                blockEntity.markDirty();
            }
            result = ActionResult.SUCCESS;
        } else if (handItem instanceof UpgradeItem && blockEntity.acceptsUpgrade()) {
            if (!world.isClient) {
                if (blockEntity.tryAddUpgrade(((UpgradeItem) handItem).createUpgrade(), handStack)) {
                    if (!player.isCreative()) player.getStackInHand(hand).decrement(1);
                    blockEntity.markDirty();
                }
            }
            result = ActionResult.SUCCESS;
        } else result = blockEntity.module.onUse(world, pos, state, player, hand, side, hit);
        return result;
    }

    public static boolean hitModule(Direction side, BlockPos pos, Vec3d hitVec) {
        double x = Math.abs(Math.abs(hitVec.x) - Math.abs(pos.getX()));
        double y = Math.abs(Math.abs(hitVec.y) - Math.abs(pos.getY()));
        double z = Math.abs(Math.abs(hitVec.z) - Math.abs(pos.getZ()));

        switch (side) {
            case DOWN:
            case UP:
                return x > 0.17F && x < 0.83F && z > 0.17F && z < 0.83F;
            case NORTH:
            case SOUTH:
                return x > 0.17F && x < 0.83F && y > 0.20F && y < 0.80F;
            case WEST:
            case EAST:
                return z > 0.17F && z < 0.83F && y > 0.20F && y < 0.80F;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return getBE(world, pos).module.getScreenHandler(state, world, pos);
    }

    public static ActionResult onPlayerInteracted(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        BlockState blockState = world.getBlockState(hitResult.getBlockPos());

        if (blockState.getBlock() instanceof ModularFrameBlock &&
                player.getStackInHand(hand).getItem() instanceof ScrewdriverItem) {

            ActionResult actionResult = blockState.onUse(world, player, hand, hitResult);
            if (actionResult.isAccepted()) {
                return actionResult;
            }
        }
        return ActionResult.PASS;
    }
    //endregion

    //region <placing/breaking>
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction side = state.get(FACING);
        return canAttachTo(world, pos.offset(side.getOpposite()), side);
    }

    public boolean canAttachTo(BlockView world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        return (state.isSideSolidFullSquare(world, pos, side) || state.getMaterial().isSolid())
                && !RepeaterBlock.isRedstoneGate(state)
                && !(state.getBlock() instanceof ModularFrameBlock);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClient) {
            Direction side = state.get(FACING);
            if (!canAttachTo(world, pos.offset(side.getOpposite()), side)) {
                world.breakBlock(pos, true);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDroppedStacks(state, builder);
        drops.add(new ItemStack(asItem()));

        ModularFrameEntity tile = (ModularFrameEntity)builder.get(LootContextParameters.BLOCK_ENTITY);
        if (tile != null) {
            if (!(tile.module instanceof EmptyModule)) {
                ItemStack modStack = new ItemStack(tile.module.getItem());
                drops.add(modStack);
                tile.module.onRemove(builder.getWorld(), tile.getPos(), state.get(FACING), null, modStack);
            }
            for (UpgradeBase upgrade : tile.upgrades) {
                ItemStack upStack = new ItemStack(upgrade.getItem());
                drops.add(upStack);
                upgrade.onRemove(builder.getWorld(), tile.getPos(), state.get(FACING), upStack);
            }
        }
        return drops;
    }
    //endregion

    //region <other>
    @Override
    public boolean canMobSpawnInside() {
        return false;
    }
    //endregion
}
