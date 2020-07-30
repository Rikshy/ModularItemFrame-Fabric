package dev.shyrik.modularitemframe.mixin;

import dev.shyrik.modularitemframe.api.mixin.IngredientGetMatchingStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Ingredient.class)
public abstract class IngredientMixin implements IngredientGetMatchingStacks {

    @Shadow
    private ItemStack[] matchingStacks;

    @Shadow
    private void cacheMatchingStacks() { }

    @Override
    public ItemStack[] getMatchingStacks() {
        this.cacheMatchingStacks();
        return this.matchingStacks;
    }
}
