package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t2.CraftingPlusModule;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import dev.shyrik.modularitemframe.api.mixin.IngredientGetMatchingStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class AutoCraftingModule extends CraftingPlusModule {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_auto_crafting");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID, "block/module_t3_auto_crafting");

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST_LOC;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.crafting_plus");
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            player.openHandledScreen(getScreenHandler(blockEntity.getCachedState(), world, pos));
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick(World world,  BlockPos pos) {
        if (world.isClient) return;
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

        Inventory handler = (Inventory) blockEntity.getAttachedInventory();
        if (handler != null) {
            autoCraft(handler, world, pos);
        }
    }

    @Environment(EnvType.CLIENT)
    private void autoCraft(Inventory inventory, World world, BlockPos pos) {
        if (recipe == null) reloadRecipe();

        if (recipe == null || recipe.getOutput().isEmpty() || !InventoryHelper.canCraft(inventory, ItemHelper.getIngredients(recipe)))
            return;

        ItemHelper.ejectStack(world, pos, blockEntity.blockFacing(), recipe.getOutput().copy());

        for (IngredientGetMatchingStacks ingredient : ItemHelper.getIngredients(recipe)) {
            if (ingredient.getMatchingStacks().length > 0) {
                InventoryHelper.removeFromInventory(inventory, ingredient.getMatchingStacks());
            }
        }

        NetworkHandler.sendAround(
                world,
                blockEntity.getPos(),
                32,
                new PlaySoundPacket(pos, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS, 0.3F, 0.7F));
    }
}
