package dev.shyrik.modularitemframe.api.util;

import dev.shyrik.modularitemframe.api.mixin.IngredientGetMatchingStacks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ItemHelper {

    public static boolean areItemsEqualIgnoreDurability(ItemStack[] toCheck, ItemStack itemStack) {
        for (ItemStack checkStack : toCheck) {
            if (ItemStack.areItemsEqualIgnoreDamage(checkStack, itemStack)) return true;
        }
        return false;
    }

    public static DefaultedList<IngredientGetMatchingStacks> getIngredients(Recipe recipe) {
        return (DefaultedList<IngredientGetMatchingStacks>)recipe.getPreviewInputs();
    }

    public static boolean simpleAreItemsEqual(ItemStack stack, ItemStack stack2) {
        return stack.getItem() == stack2.getItem();
    }

    public static boolean simpleAreStacksEqual(ItemStack stack, ItemStack stack2) {
        return stack.getItem() == stack2.getItem() && stack.getDamage() == stack2.getDamage();
    }

    public static void ejectStack(World world, BlockPos pos, Direction facing, ItemStack stack) {
        Vec3d position = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        Vec3d velocity = Vec3d.ZERO;

        switch (facing) {
            case UP:
                position = position.add(0.0D, -0.25D, 0.0D);
                velocity = velocity.add(0.0D, 0.2D, 0.0D);
                break;
            case DOWN:
                position = position.add(0.0D, -0.25D, 0.0D);
                break;
            case NORTH:
                position = position.add(0.0D, -0.25D, 0.25D);
                velocity = velocity.add(0.0D, 0.0D, -0.2D);
                break;
            case EAST:
                position = position.add(-0.25D, -0.25D, 0.0D);
                velocity = velocity.add(0.2D, 0.0D, 0.0D);
                break;
            case WEST:
                position = position.add(0.25D, -0.25D, 0.0D);
                velocity = velocity.add(-0.2D, 0.0D, 0.0D);
                break;
            case SOUTH:
                position = position.add(0.0D, -0.25D, -0.25D);
                velocity = velocity.add(0.0D, 0.0D, 0.2D);
                break;
        }

        ItemEntity item = new ItemEntity(world, position.x, position.y, position.z, stack);
        item.setVelocity(velocity.x, velocity.y, velocity.z);
        world.spawnEntity(item);
    }

    public static boolean increaseStackInList(List<ItemStack> list, ItemStack stack) {
        int idx = listContainsItemStackEqual(list, stack);
        if (idx >= 0) {
            ItemStack listStack = list.get(idx);
            listStack.increment(stack.getCount());
            return true;
        }
        return false;
    }

    public static int listContainsItemStackEqual(List<ItemStack> list, ItemStack stack) {
        for (int i = 0; i < list.size(); ++i) {
            if (simpleAreItemsEqual(stack, list.get(i))) return i;
        }
        return -1;
    }

    public static Recipe getRecipe(Inventory inventory, World world) {
        CraftingInventory craft = new CraftingInventory(new ScreenHandler(ScreenHandlerType.CRAFTING, 1) {
            @Override
            public boolean canUse(PlayerEntity player) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.isEmpty())
                continue;

            craft.setStack(i, stack.copy());
        }

        return world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craft, world).orElse(null);
    }
}
