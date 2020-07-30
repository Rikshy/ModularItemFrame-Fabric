package dev.shyrik.modularitemframe.common.module.t2;

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

import java.util.List;

public class TrashCanModule extends ModuleBase {

    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_trashcan");
    public static final Identifier BG_LOC1 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_1");
    public static final Identifier BG_LOC2 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_2");
    public static final Identifier BG_LOC3 = new Identifier(ModularItemFrame.MOD_ID, "block/module_t2_trashcan_3");

    private List<Identifier> frontTex = ImmutableList.of(
            BG_LOC1,
            BG_LOC2,
            BG_LOC3
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
        return ModularFrameBlock.INNER_HARD_LOC;
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
            if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

            Inventory trash = blockEntity.getAttachedInventory();
            if (trash != null) {
                for (int slot = 0; slot < trash.size(); slot++) {
                    if (!trash.getStack(slot).isEmpty()) {
                        trash.setStack(slot, ItemStack.EMPTY);
                        NetworkHandler.sendAround(
                                world,
                                pos,
                                32,
                                new PlaySoundPacket(pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.4F, 0.7F));
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
