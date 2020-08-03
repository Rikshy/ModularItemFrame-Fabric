package dev.shyrik.modularitemframe.common.module.t3;

import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ExperienceHelper;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class XPModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID,"module_t3_xp");
    public static final Identifier BG_LOC = new Identifier(ModularItemFrame.MOD_ID,"block/module_t3_xp");
    private static final int MAX_XP = 21862;

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
        return BG_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARDEST_LOC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.xp");
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            if (player.isSneaking()) drainContainerXpToReachPlayerLevel(player, 0);
            else drainContainerXpToReachPlayerLevel(player, player.experienceLevel + 1);
            blockEntity.markDirty();
        }
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (player instanceof FakePlayer) return ActionResult.FAIL;

        if (!world.isClient) {
            if (player.isSneaking()) drainPlayerXpToReachPlayerLevel(player, 0);
            else drainPlayerXpToReachPlayerLevel(player, player.experienceLevel - 1);
            blockEntity.markDirty();
        }
        return ActionResult.SUCCESS;
    }

    private void drainPlayerXpToReachPlayerLevel(PlayerEntity player, int level) {
        int targetXP = ExperienceHelper.getExperienceForLevel(level);
        int drainXP = ExperienceHelper.getPlayerXP(player) - targetXP;
        if (drainXP <= 0) {
            return;
        }
        drainXP = addExperience(drainXP);
        if (drainXP > 0) {
            player.addExperience(-drainXP); // EXP Handler wurde zT in PlayerEntity eingebaut, aber ich habe keine tabelle gefunden für die absoluten Zahlen pro lvl
        }
    }

    private int addExperience(int xpToAdd) {
        int j = MAX_XP - experience;
        if (xpToAdd > j) {
            xpToAdd = j;
        }

        experience += xpToAdd;
        levels = ExperienceHelper.getLevelForExperience(experience);
        //experience = (experience - ExperienceHelper.getExperienceForLevel(levels)) / ExperienceHelper.getXpBarCapacity(levels);
        return xpToAdd;
    }

    private void drainContainerXpToReachPlayerLevel(PlayerEntity player, int level) {
        int requiredXP = level == 0 ? experience : ExperienceHelper.getExperienceForLevel(level) - ExperienceHelper.getPlayerXP(player);

        requiredXP = Math.min(experience, requiredXP);

        addExperience(-requiredXP);
        player.addExperience(requiredXP);
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        super.onRemove(world, pos, facing, player);
        if (player == null || player instanceof FakePlayer)
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, experience));
        else player.addExperience(experience);
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (experience >= MAX_XP) return;
        if (world.getTime() % (60 - 10 * blockEntity.getSpeedUpCount()) != 0) return;

        List<ExperienceOrbEntity> entities = world.getEntities(ExperienceOrbEntity.class, getVacuumBox(pos), experienceOrbEntity -> true);
        for (ExperienceOrbEntity entity : entities) {
            if (!entity.isAlive()) continue;

            addExperience(entity.getExperienceAmount());
        }
    }

    private Box getVacuumBox(BlockPos pos) {
        int range = ModularItemFrame.getConfig().vacuumRange + blockEntity.getRangeUpCount();
        switch (blockEntity.blockFacing()) {
            case DOWN:
                return new Box(pos.add(-range, 0, -range), pos.add(range, -range, range));
            case UP:
                return new Box(pos.add(-range, 0, -range), pos.add(range, range, range));
            case NORTH:
                return new Box(pos.add(-range, -range, 0), pos.add(range, range, -range));
            case SOUTH:
                return new Box(pos.add(-range, -range, 0), pos.add(range, range, range));
            case WEST:
                return new Box(pos.add(0, -range, -range), pos.add(range, range, range));
            case EAST:
                return new Box(pos.add(0, -range, -range), pos.add(-range, range, range));
        }
        return new Box(pos, pos.add(1, 1, 1));
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
}
