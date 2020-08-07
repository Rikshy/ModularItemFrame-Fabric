package dev.shyrik.modularitemframe.init;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.*;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import static dev.shyrik.modularitemframe.ModularItemFrame.MOD_ID;

@Config(name=MOD_ID)
public class ModularItemFrameConfig implements ConfigData {

    @Comment("Maximum number of upgrades a frame can hold [default=5]")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int maxFrameUpgrades = 5;

    @Comment("Base fluid capacity of the tank frame (buckets) [default=4]")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
    public int tankFrameCapacity = 4;

    @Comment("Base transfer rate of the tank (drips) [0=disabled | 1620=1Bucket] [default=162]")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1620)
    public int tankTransferRate = 162;

    @Comment("Tank module will spill content when removing the module. [default=false]")
    public boolean dropFluidOnTankRemove = false;

    @Comment("Base teleport distance of the teleport module")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 256)
    public int teleportRange = 64;

    @Comment("Base range of the vacuum module pickup range")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public int vacuumRange = 3;

    @Override
    public void validatePostLoad() {
        if (tankTransferRate > 1620) {
            tankTransferRate = 1620;
        }
        if (tankFrameCapacity > 32) {
            tankFrameCapacity = 32;
        } else if (tankFrameCapacity < 1) {
            tankFrameCapacity = 1;
        }

        if (teleportRange > 256) {
            teleportRange = 256;
        }

        if (vacuumRange > 16) {
            vacuumRange = 16;
        }
    }
}
