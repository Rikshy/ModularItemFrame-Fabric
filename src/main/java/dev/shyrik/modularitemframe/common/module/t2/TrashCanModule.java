package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.network.NetworkHandler;
import dev.shyrik.modularitemframe.common.network.packet.PlaySoundPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.List;

public class TrashCanModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_trashcan");
    public static final Identifier BG1 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_1");
    public static final Identifier BG2 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_2");
    public static final Identifier BG3 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_3");

    private List<Identifier> frontTex = ImmutableList.of(
            BG1,
            BG2,
            BG3
    );
    private int texIndex = 0;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier frontTexture() {
        return frontTex.get(texIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.trash_can");
    }

    @Override
    public List<Identifier> getVariantFronts() {
        return frontTex;
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (!world.isClient) {
            if (!canTick(world,60, 10)) return;

            FixedItemInv trash = blockEntity.getAttachedInventory();
            if (trash != null) {
                for (int slot = 0; slot < trash.getSlotCount(); slot++) {
                    if (!trash.getInvStack(slot).isEmpty()) {
                        trash.setInvStack(slot, ItemStack.EMPTY, Simulation.ACTION);
                        NetworkHandler.sendAround(
                                world,
                                pos,
                                32,
                                new PlaySoundPacket(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS));
                        break;
                    }
                }
            }
        } else {
            if (world.getTime() % 10 == 0) {
                texIndex = texIndex < frontTex.size() - 1 ? texIndex + 1 : 0;
            }
        }
    }
}
