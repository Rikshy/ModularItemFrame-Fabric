package dev.shyrik.modularitemframe.util.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;

import java.util.Optional;

public interface ExplosionEvent {

    Event<CanDestroyBlock> CAN_DESTROY_BLOCK_EVENT = EventFactory.createArrayBacked(CanDestroyBlock.class,
            (listeners) -> (explosion, world, pos, blockState, power) -> {
                Optional<Boolean> ret = Optional.empty();
                for (CanDestroyBlock listener : listeners) {
                    ret = listener.onCanDestroyBlock(explosion, world, pos, blockState, power);
                    if (ret.isPresent())
                        return ret;
                }
                return ret;
            });

    Event<GetBlastResistance> GET_BLAST_RESISTANCE_EVENT = EventFactory.createArrayBacked(GetBlastResistance.class,
            (listeners) -> (explosion, world, pos, blockState, fluidState) -> {
                Optional<Float> ret = Optional.empty();
                for (GetBlastResistance listener : listeners) {
                    ret = listener.onGetBlastResistance(explosion, world, pos, blockState, fluidState);
                    if (ret.isPresent())
                        return ret;
                }
                return ret;
            });

    static void register(Full listener) {
        CAN_DESTROY_BLOCK_EVENT.register(listener);
        GET_BLAST_RESISTANCE_EVENT.register(listener);
    }

    interface CanDestroyBlock {
        Optional<Boolean> onCanDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, float power);
    }

    interface GetBlastResistance {
        Optional<Float> onGetBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState);
    }

    interface Full extends CanDestroyBlock, GetBlastResistance {

    }
}
