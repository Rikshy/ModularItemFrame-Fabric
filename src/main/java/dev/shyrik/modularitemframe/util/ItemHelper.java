package dev.shyrik.modularitemframe.util;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemHelper {

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

    public static CraftingRecipe getRecipe(FixedItemInv inventory, World world) {
        CraftingInventory craft = new CraftingInventory(new ScreenHandler(ScreenHandlerType.CRAFTING, 1) {
            @Override
            public boolean canUse(PlayerEntity player) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getInvStack(i);

            if (stack.isEmpty())
                continue;

            craft.setStack(i, stack.copy());
        }

        return world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craft, world).orElse(null);
    }
}
