package dev.shyrik.modularitemframe.mixin;

import dev.shyrik.modularitemframe.api.mixin.BlockResistanceAccessor;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.class)
public abstract class BlockResistanceAccessorMixin implements BlockResistanceAccessor {

    @Accessor("resistance")
    public abstract void setResistance(float resistance);
}
