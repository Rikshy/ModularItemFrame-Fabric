package dev.shyrik.modularitemframe.mixin;

import dev.shyrik.modularitemframe.util.event.ExplosionEvent;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {

    @Inject(method = "canDestroyBlock", at = @At("RETURN"), cancellable = true)
    public void canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power, CallbackInfoReturnable<Boolean> cir) {
        Optional<Boolean> ret = ExplosionEvent.CAN_DESTROY_BLOCK_EVENT.invoker().onCanDestroyBlock(explosion, world, pos, state, power);
        ret.ifPresent(cir::setReturnValue);
    }

    @Inject(method = "getBlastResistance", at = @At("RETURN"), cancellable = true)
    public void getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Optional<Float>> cir) {
        Optional<Float> ret = ExplosionEvent.GET_BLAST_RESISTANCE_EVENT.invoker().onGetBlastResistance(explosion, world, pos, blockState, fluidState);
        if (ret.isPresent())
            cir.setReturnValue(ret);
    }
}
