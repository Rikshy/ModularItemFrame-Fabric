package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ExperienceHelper;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class XPModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID,"module_t3_xp");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID,"module/module_t3_xp");
    private static final Text NAME = new TranslatableText("modularitemframe.module.xp");

    private static final String NBT_XP = "xp";
    private static final String NBT_LEVEL = "level";

    private int experience;
    private int levels;

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
        return ModularFrameBlock.INNER_HARDEST;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getModuleName() {
        return NAME;
    }

    @Override
    public void appendTooltips(List<Text> tooltips) {
        tooltips.add(new TranslatableText("modularitemframe.tooltip.xp.level", levels));
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            if (player.isSneaking()) drainContainerXpToReachPlayerLevel(player, 0);
            else drainContainerXpToReachPlayerLevel(player, player.experienceLevel + 1);
            markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            if (player.isSneaking()) drainPlayerXpToReachPlayerLevel(player, 0);
            else drainPlayerXpToReachPlayerLevel(player, player.experienceLevel - 1);
            markDirty();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
        super.onRemove(world, pos, facing, player, moduleStack);
        if (player == null)
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, experience));
        else player.addExperience(experience);
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (levels >= ExperienceHelper.MAX_LEVEL) return;
        if (world.isClient || frame.isPowered() || !canTick(world,60, 10)) return;

        boolean gotXp = false;
        List<ExperienceOrbEntity> entities = world.getEntitiesByClass(ExperienceOrbEntity.class, getScanBox(), experienceOrbEntity -> true);
        for (ExperienceOrbEntity entity : entities) {
            if (!entity.isAlive()) continue;

            int xp = entity.getExperienceAmount();
            int prevLvl = levels;
            int remain = addExperience(xp);
            if (prevLvl != levels) {
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1F, 1F);
            }
            if (remain != xp) {
                entity.kill();
                gotXp = true;
            }
        }
        if (gotXp) {
            markDirty();
            world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1F, 1F);
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.putInt(NBT_XP, experience);
        tag.putInt(NBT_LEVEL, levels);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_XP)) experience = tag.getInt(NBT_XP);
        if (tag.contains(NBT_LEVEL)) levels = tag.getInt(NBT_LEVEL);
    }

    private void drainPlayerXpToReachPlayerLevel(PlayerEntity player, int level) {
        int targetXP = ExperienceHelper.getExperienceForLevel(level);
        int drainXP = ExperienceHelper.getPlayerXP(player) - targetXP;
        if (drainXP <= 0) {
            return;
        }
        drainXP = addExperience(drainXP);
        if (drainXP > 0) {
            player.addExperience(-drainXP);
        }
    }

    private void drainContainerXpToReachPlayerLevel(PlayerEntity player, int level) {
        int requiredXP = level == 0 ? experience : ExperienceHelper.getExperienceForLevel(level) - ExperienceHelper.getPlayerXP(player);

        requiredXP = Math.min(experience, requiredXP);

        addExperience(-requiredXP);
        player.addExperience(requiredXP);
    }

    private int addExperience(int xpToAdd) {
        int j = ExperienceHelper.getMaxXp() - experience;
        if (xpToAdd > j) {
            xpToAdd = j;
        }

        experience += xpToAdd;
        levels = ExperienceHelper.getLevelForExperience(experience);
        return xpToAdd;
    }
}
