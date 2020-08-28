package dev.shyrik.modularitemframe.common.module.t3;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.util.InventoryHelper;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t2.CraftingModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoCraftingModule extends CraftingModule {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t3_auto_crafting");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "module/module_t3_auto_crafting");

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
        return I18n.translate("modularitemframe.module.crafting_plus");
    }

    @Override
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
        if (!world.isClient) {
            player.openHandledScreen(getScreenHandler(frame.getCachedState(), world, pos));
            markDirty();
        }
    }

    @Override
    public void tick(World world,  BlockPos pos) {
        if (world.isClient || !canTick(world,60, 10)) return;

        FixedItemInv handler = frame.getAttachedInventory();
        if (handler != null) {
            autoCraft(handler, world, pos);
        }
    }

    private void autoCraft(FixedItemInv inventory, World world, BlockPos pos) {
        if (recipe == null) reloadRecipe();

        if (recipe == null || recipe.getOutput().isEmpty() || !InventoryHelper.canCraft(inventory, recipe))
            return;

        ItemHelper.ejectStack(world, pos, frame.getFacing(), recipe.getOutput().copy());

        InventoryHelper.removeIngredients(inventory, recipe);

        world.playSound(null, frame.getPos(), SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS, 1F, 1F);
    }
}
