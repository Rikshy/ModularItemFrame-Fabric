package dev.shyrik.modularitemframe.common.module.t2;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class FanModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_fan");
    public static final Identifier BG1 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_fan1");
    public static final Identifier BG2 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_fan2");
    public static final Identifier BG3 = new Identifier(ModularItemFrame.MOD_ID, "module/module_t2_fan3");

    private static final List<Identifier> frontTex = ImmutableList.of(
            BG1, BG2, BG3
    );
    public static final double strengthScaling = 0.09;

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
    public List<Identifier> getVariantFronts() {
        return frontTex;
    }

    @Override
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_HARD;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.fan");
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace) {
        return ActionResult.FAIL;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (frame.isPowered()) return;
        if (world.isClient) {
            if (world.getTime() % 10 == 0) {
                texIndex = texIndex < frontTex.size() - 1 ? texIndex + 1 : 0;
            }
        }
        List<Entity> entities = world.getEntitiesByClass(Entity.class, getFanBox(), entity ->
                (entity instanceof LivingEntity || entity instanceof ItemEntity) && !entity.isSneaky() && entity.isAlive());
        if (entities.isEmpty()) return;
        Direction facing = frame.getFacing();
        double xVel = facing.getOffsetX() * strengthScaling;
        double yVel = facing.getOffsetY() * strengthScaling;
        double zVel = facing.getOffsetZ() * strengthScaling;
        entities.forEach(livingEntity -> livingEntity.addVelocity(xVel, yVel, zVel));
    }

    private Box getFanBox() {
        BlockPos pos = frame.getPos();
        int range = frame.getRangeUpCount() + ModularItemFrame.getConfig().scanZoneRadius;
        switch (frame.getFacing()) {
            case DOWN:
                return new Box(pos.add(0, 1, 0), pos.add(1, -range + 1, 1));
            case UP:
                return new Box(pos.add(0, -1, 0), pos.add(1, range, 1));
            case NORTH:
                return new Box(pos.add(0, 0, 1), pos.add(1, 1, -range + 1));
            case SOUTH:
                return new Box(pos.add(0, 0, -1), pos.add(1, 1, range));
            case WEST:
                return new Box(pos.add(1, 0, 0), pos.add(-range + 1, 1, 1));
            case EAST:
                return new Box(pos.add(-1, 0, 0), pos.add(range, 1, 1));
        }
        return new Box(pos, pos.add(1, 1, 1));
    }
}
