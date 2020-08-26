package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.impl.CombinedFixedItemInv;
import alexiil.mc.lib.attributes.item.impl.DirectFixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import dev.shyrik.modularitemframe.common.screenhandler.crafting.CraftingFrameScreenHandler;
import dev.shyrik.modularitemframe.common.screenhandler.crafting.FrameCrafting;
import dev.shyrik.modularitemframe.common.screenhandler.crafting.IScreenHandlerCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.CraftingRecipe;
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

import java.util.Arrays;
import java.util.Optional;

public class CraftingModule extends ModuleBase implements IScreenHandlerCallback {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_crafting");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_crafting");

    private static final String NBT_GHOST_INVENTORY = "ghost_inventory";
    private static final String NBT_DISPLAY = "display";
    private static final String NBT_MODE = "cp_mode";

    public EnumMode mode = EnumMode.PLAYER;
    protected CraftingRecipe recipe;
    private ItemStack displayItem = ItemStack.EMPTY;
    private final DirectFixedItemInv ghostInventory = new DirectFixedItemInv(9);

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.crafting");
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
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        renderer.renderInside(displayItem, matrixStack, buffer, light, overlay);
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            if (player.isSneaking()) {
                int modeIdx = mode.getIndex() + 1;
                if (modeIdx == EnumMode.values().length) modeIdx = 0;
                mode = EnumMode.values()[modeIdx];
                player.sendMessage(new TranslatableText(mode.getName()), false);
            } else {
                player.openHandledScreen(getScreenHandler(frame.getCachedState(), world, pos));
            }

            markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            if (!hasValidRecipe()) {
                player.openHandledScreen(getScreenHandler(state, world, pos));
                markDirty();
            } else
                craft(player, player.isSneaking());
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public NamedScreenHandlerFactory getScreenHandler(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((id, playerInventory, player) ->
                new CraftingFrameScreenHandler(
                        id,
                        player,
                        ghostInventory,
                        this),
                new TranslatableText("gui.modularitemframe.crafting.name")
        );
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_MODE, mode.getIndex());
        tag.put(NBT_DISPLAY, displayItem.toTag(new CompoundTag()));
        tag.put(NBT_GHOST_INVENTORY, ghostInventory.toTag());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_MODE)) mode = EnumMode.values()[tag.getInt(NBT_MODE)];
        if (tag.contains(NBT_DISPLAY)) displayItem = ItemStack.fromTag(tag.getCompound(NBT_DISPLAY));
        if (tag.contains(NBT_GHOST_INVENTORY)) ghostInventory.fromTag(tag.getCompound(NBT_GHOST_INVENTORY));
    }

    private void craft(PlayerEntity player, boolean fullStack) {
        final FixedItemInv workingInv = getWorkingInventories(player.inventory);
        reloadRecipe();

        if (workingInv == null || recipe == null || recipe.getOutput().isEmpty() || !InventoryHelper.canCraft(workingInv, recipe))
            return;

        int craftAmount = fullStack ? Math.min(InventoryHelper.countPossibleCrafts(workingInv, recipe), 64) : 1;
        do {
            ItemStack remain = InventoryHelper.givePlayer(player, recipe.getOutput());
            if (!remain.isEmpty()) ItemHelper.ejectStack(player.world, frame.getPos(), frame.getFacing(), remain);

            InventoryHelper.removeIngredients(workingInv, recipe);
        } while (--craftAmount > 0);
        NetworkHandler.sendAround(
                player.world,
                frame.getPos(),
                32,
                new PlaySoundPacket(frame.getPos(), SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS));
    }

    protected FixedItemInv getWorkingInventories(Inventory playerInventory) {
        FixedItemInv neighborInventory = frame.getAttachedInventory();
        FixedItemInv fixedPlayerInv = new FixedInventoryVanillaWrapper(playerInventory);
        switch (mode) {
            case HYBRID:
                if (neighborInventory == null)
                    return fixedPlayerInv;
                return CombinedFixedItemInv.create(Arrays.asList(neighborInventory, fixedPlayerInv));
            case ATTACHED:
                return neighborInventory;
            case PLAYER:
                return fixedPlayerInv;
        }

        return null;
    }

    protected boolean hasValidRecipe() {
        if (recipe == null) reloadRecipe();
        return recipe != null && !recipe.getOutput().isEmpty();
    }

    protected void reloadRecipe() {
        recipe = ItemHelper.getRecipe(ghostInventory, frame.getWorld());
        displayItem = recipe != null ? recipe.getOutput().copy() : ItemStack.EMPTY;
        markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return frame.getPos().isWithinDistance(player.getPos(), 64);
    }

    @Override
    public ItemStack onScreenHandlerMatrixChanged(FrameCrafting matrix) {
        World world = frame.getWorld();
        displayItem = ItemStack.EMPTY;
        if (world != null) {
            recipe = null;
            Optional<CraftingRecipe> optional = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, matrix, world);
            if (optional.isPresent()) {
                recipe = optional.get();
                displayItem = recipe.getOutput();
            }

            markDirty();
        }
        return displayItem;
    }

    public enum EnumMode {
        HYBRID(0, "modularitemframe.mode.hybrid_inv"),
        ATTACHED(1, "modularitemframe.mode.attached_inv"),
        PLAYER(2, "modularitemframe.mode.player_inv");

        private final int index;
        private final String name;

        EnumMode(int indexIn, String nameIn) {
            index = indexIn;
            name = nameIn;
        }

        public int getIndex() {
            return this.index;
        }

        @Environment(EnvType.CLIENT)
        public String getName() {
            return I18n.translate(this.name);
        }
    }
}
