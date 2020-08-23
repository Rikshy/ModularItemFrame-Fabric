package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.SpawnParticlesPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class VacuumModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_vacuum");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_vacuum");

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
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.vacuum");
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (world.isClient || !canTick(world,60, 10)) return;

        FixedItemInv handler = frame.getAttachedInventory();
        if (handler != null) {
            List<ItemEntity> entities = world.getEntitiesByClass(ItemEntity.class, getScanBox(), itemEntity -> true);
            for (ItemEntity entity : entities) {
                ItemStack entityStack = entity.getStack();
                ItemInsertable inserter = handler.getInsertable();
                if (!entity.isAlive() ||
                        entityStack.isEmpty() ||
                        inserter.attemptInsertion(entityStack, Simulation.SIMULATE).getCount() == entityStack.getCount())
                    continue;

                ItemStack remain = inserter.insert(entityStack);
                if (remain.isEmpty()) entity.remove();
                else entity.setStack(remain);
                NetworkHandler.sendAround(
                        entity.world,
                        entity.getBlockPos(),
                        32,
                        new SpawnParticlesPacket(ParticleTypes.EXPLOSION, entity.getBlockPos(), 1));
                break;
            }
        }
    }
}
