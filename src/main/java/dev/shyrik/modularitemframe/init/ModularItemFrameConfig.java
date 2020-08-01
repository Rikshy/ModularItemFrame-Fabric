package dev.shyrik.modularitemframe.init;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.*;

import static dev.shyrik.modularitemframe.ModularItemFrame.MOD_ID;

@Config(name=MOD_ID)
public class ModularItemFrameConfig implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int MaxFrameUpgrades = 5; //"Maximum number of upgrades a frame can hold"

    public boolean AllowFakePlayers = false; //"Allow fake players to interact with frames"

    @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
    public int TankFrameCapacity = 4; //"Base Fluid Capacity of the tank frame (buckets)"

    @ConfigEntry.BoundedDiscrete(min = 0, max = 1620)
    public int TankTransferRate = 162; //"Transferrate of the tank (drips) [0=disabled]"

    public boolean dropFluidOnTankRemove = false;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 1024)
    public int BaseTeleportRange = 64; //"Base teleport distance of the teleport module"

    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    public int BaseVacuumRange = 3; //"Base range of the vacuum frame"

    public boolean DisableAutomaticItemTransfer = false; //"Makes the Item Teleport Module to not vacuum items"
}
