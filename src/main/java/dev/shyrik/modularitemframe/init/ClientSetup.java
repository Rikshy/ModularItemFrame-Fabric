package dev.shyrik.modularitemframe.init;

import dev.shyrik.modularitemframe.api.mixin.PreStitchCallback;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.module.t1.*;
import dev.shyrik.modularitemframe.common.module.t2.*;
import dev.shyrik.modularitemframe.common.module.t3.*;

public class ClientSetup {

    public static void stitch(PreStitchCallback.Context ctx) {
        ctx.addSprites(
                ModularFrameBlock.INNER_HARDEST_LOC,
                ModularFrameBlock.INNER_HARD_LOC,

                CraftingModule.BG_LOC,
                IOModule.BG_LOC,
                ItemModule.BG_LOC,
                NullifyModule.BG_LOC,
                StorageModule.BG_LOC,
                TankModule.BG_LOC,

                CraftingPlusModule.BG_LOC,
                DispenseModule.BG_LOC,
                TrashCanModule.BG_LOC1,
                TrashCanModule.BG_LOC2,
                TrashCanModule.BG_LOC3,
                UseModule.BG_LOC,
                VacuumModule.BG_LOC,

                AutoCraftingModule.BG_LOC,
                FluidDispenserModule.BG_LOC,
                ItemTeleportModule.BG_IN,
                ItemTeleportModule.BG_OUT,
                ItemTeleportModule.BG_NONE,
                XPModule.BG_LOC
        );
    }
}
