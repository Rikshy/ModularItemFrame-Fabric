package dev.shyrik.modularitemframe.common.upgrade;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.UpgradeBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

public class SecurityUpgrade extends UpgradeBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "upgrade_security");
    private static final Text NAME = new TranslatableText("modularitemframe.upgrade.security");

    private static final String NBT_PLAYER = "player_id";

    private UUID playerId = null;

    @Override
    public int getMaxCount() {
        return 1;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Text getName() {
        return NAME;
    }

    @Override
    public void onInsert(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack upStack) {
        playerId = player.getUuid();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putUuid(NBT_PLAYER, playerId);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        playerId = tag.contains(NBT_PLAYER) ? tag.getUuid(NBT_PLAYER) : null;
    }

    public boolean hasAccess(PlayerEntity player) {
        return player.getUuid().compareTo(playerId) == 0;
    }
}
