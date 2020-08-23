package dev.shyrik.modularitemframe.api.util;

import net.minecraft.entity.player.PlayerEntity;

public class ExperienceHelper {

    public static final int MAX_LEVEL = 21862;
    private static final int[] xpLevels = new int[MAX_LEVEL + 1];

    static {
        int res = 0;
        for (int i = 0; i <= MAX_LEVEL; i++) {
            if (res < 0) {
                res = Integer.MAX_VALUE;
            }
            xpLevels[i] = res;
            res += getXpBarCapacity(i);
        }
    }

    public static int getMaxXp() {
        return xpLevels[MAX_LEVEL];
    }

    public static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level > MAX_LEVEL) {
            return Integer.MAX_VALUE;
        }
        return xpLevels[level];
    }

    public static int getXpBarCapacity(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    public static int getLevelForExperience(int experience) {
        for (int i = 1; i < xpLevels.length; i++) {
            if (xpLevels[i] > experience) {
                return i - 1;
            }
        }
        return xpLevels.length;
    }

    public static int getPlayerXP(PlayerEntity player) {
        return (int) (getExperienceForLevel(player.experienceLevel) + player.experienceProgress);
    }
}
