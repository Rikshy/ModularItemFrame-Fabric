package dev.shyrik.modularitemframe.common.module.t1;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import dev.shyrik.modularitemframe.common.screenhandler.CraftingFrameScreenHandler;
import dev.shyrik.modularitemframe.common.screenhandler.FrameCrafting;
import dev.shyrik.modularitemframe.common.screenhandler.IScreenHandlerCallback;
import dev.shyrik.modularitemframe.mixin.IngredientGetMatchingStacks;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class CraftingModule extends ModuleBase implements IScreenHandlerCallback {

    public static final Identifier LOC = new Identifier(ModularItemFrame.MOD_ID, "module_t1_crafting");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t1_crafting");
    private static final String NBT_GHOSTINVENTORY = "ghostinventory";
    private static final String NBT_DISPLAY = "display";

    protected Recipe recipe;
    private ItemStack displayItem = ItemStack.EMPTY;
    private final SimpleInventory ghostInventory = new SimpleInventory(9);

    @Override
    public Identifier getId() {
        return LOC;
    }

    @Override
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.crafting");
    }

    @Override
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
        //FrameItemRenderer.renderOnFrame(displayItem, tile.blockFacing(), 0, 0.1F, TransformType.FIXED, matrixStack, buffer, combinedLight, combinedOverlay);
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity playerIn, ItemStack driver) {
        if (!world.isClient) {
            playerIn.openHandledScreen(getScreenHandler(blockEntity.getCachedState(), world, pos));
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, BlockHitResult hit) {
        if (!hasValidRecipe())
            playerIn.openHandledScreen(getScreenHandler(state, worldIn, pos));
        else {
            if (!worldIn.isClient) {
                if (playerIn.isSneaking()) craft(playerIn, true);
                else craft(playerIn, false);
            }
        }
        blockEntity.markDirty();
        return ActionResult.SUCCESS;
    }

    private void craft(PlayerEntity player, boolean fullStack) {
        final Inventory playerInventory = player.inventory;
        final Inventory workingInv = getWorkingInventories(playerInventory);
        reloadRecipe();

        if (workingInv == null || recipe == null || recipe.getOutput().isEmpty() || !InventoryHelper.canCraft(workingInv, recipe.getPreviewInputs()))
            return;

        int craftAmount = fullStack ? Math.min(InventoryHelper.countPossibleCrafts(workingInv, recipe), 64) : 1;
        do {
            ItemStack remain = InventoryHelper.giveStack(playerInventory, recipe.getOutput()); //use playerinventory here!
            if (!remain.isEmpty()) ItemHelper.ejectStack(player.world, blockEntity.getPos(), blockEntity.blockFacing(), remain);

            for (IngredientGetMatchingStacks ingredient : ItemHelper.getIngredients(recipe)) {
                if (ingredient.getMatchingStacks().length > 0) {
                    InventoryHelper.removeFromInventory(workingInv, ingredient.getMatchingStacks());
                }
            }
        } while (--craftAmount > 0);
        NetworkHandler.sendAround(player.world, blockEntity.getPos(), 32, new PlaySoundPacket(blockEntity.getPos(), SoundEvents.BLOCK_LADDER_STEP.getId(), SoundCategory.BLOCKS.getName(), 0.4F, 0.7F));
    }

    protected Inventory getWorkingInventories(Inventory playerInventory) {
        return playerInventory;
    }

    protected boolean hasValidRecipe() {
        if (recipe == null) reloadRecipe();
        return recipe != null && !recipe.getOutput().isEmpty();
    }

    protected void reloadRecipe() {
        recipe = ItemHelper.getRecipe(ghostInventory, blockEntity.getWorld());
        displayItem = recipe != null ? recipe.getOutput().copy() : ItemStack.EMPTY;
        blockEntity.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return blockEntity.getPos().isWithinDistance(player.getPos(), 64);
    }

    @Override
    public ItemStack onScreenHandlerMatrixChanged(FrameCrafting matrix) {
        World world = blockEntity.getWorld();
        displayItem = ItemStack.EMPTY;
        recipe = null;
        Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, matrix, world);
        if (optional.isPresent()) {
            recipe = optional.get();
            displayItem = recipe.getOutput();
        }

        blockEntity.markDirty();
        return displayItem;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag compound = super.toTag();
        compound.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        compound.put(NBT_GHOSTINVENTORY, ghostInventory.getTags());
        return compound;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        if (nbt.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(nbt.getCompound(NBT_DISPLAY));
        if (nbt.contains(NBT_GHOSTINVENTORY)) ghostInventory.readTags(nbt.getList(NBT_GHOSTINVENTORY, 0));
    }

    @Override
    public NamedScreenHandlerFactory getScreenHandler(BlockState state, World worldIn, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((id, playerInventory, player) ->
                new CraftingFrameScreenHandler(
                        id,
                        player.inventory,
                        ghostInventory,
                        player,
                        this),
                new TranslatableText("gui.modularitemframe.crafting.name")
        );
    }
}
