package dev.shyrik.modularitemframe.common.module.t2;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import com.mojang.authlib.GameProfile;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.api.ModuleBase;
import dev.shyrik.modularitemframe.api.util.ItemHelper;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayer;
import dev.shyrik.modularitemframe.api.util.fake.FakePlayerFactory;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.World;

import java.util.UUID;

public class SlayModule extends ModuleBase {
    public static final Identifier ID = new Identifier(ModularItemFrame.MOD_ID, "module_t2_slay");
    public static final Identifier BG = new Identifier(ModularItemFrame.MOD_ID, "block/module_nyi");

    private static final GameProfile DEFAULT_CLICKER = new GameProfile(UUID.nameUUIDFromBytes("modularitemframe".getBytes()), "[Frame Clicker]");

    private static final String NBT_WEAPON = "weapon";
    private static final String NBT_ROTATION = "rotation";

    private int rotation = 0;
    private ItemStack weapon = ItemStack.EMPTY;

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
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
        Direction facing = blockEntity.getFacing();
        switch (facing) {
            case DOWN:
            case NORTH:
            case UP:
            case SOUTH:
                renderer.renderInside(weapon, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, light, overlay);
                break;
            case WEST:
            case EAST:
                matrixStack.multiply(new Quaternion(0, 90.0F, 0.0F, true));
                matrixStack.translate(-1, 0 ,0);
                renderer.renderInside(weapon, rotation, 0.5F, ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, matrixStack, buffer, light, overlay);
                break;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getModuleName() {
        return I18n.translate("modularitemframe.module.slay");
    }

    @Override
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
        super.onRemove(world, pos, facing, player);
        ItemHelper.ejectStack(world, pos, facing, weapon);
    }

    @Override
    public ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isEmpty()) {
                player.setStackInHand(hand, weapon.copy());
                weapon.setCount(0);
            } else {
                if (weapon.isEmpty()) {
                    weapon = held.copy();
                    player.setStackInHand(hand, ItemStack.EMPTY);
                }
            }

            markDirty();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void tick(World world, BlockPos pos) {
        if (!world.isClient) {
            if (weapon.isEmpty()) {
                weapon = getNextStack();
                rotation = 0;
            } else {
                if (rotation >= 360) {
                    rotation -= 360;
                    hitIt(world, pos);
                }
                rotation += 15 * (blockEntity.getSpeedUpCount() + 1);
            }

            markDirty();
        }
    }

    private void hitIt(World world, BlockPos pos) {
        FakePlayer player = FakePlayerFactory.get(world, DEFAULT_CLICKER);

        for(MobEntity entity : world.getEntitiesByClass(MobEntity.class, getAttackBox(pos), mobEntity -> true)) {
            player.setStackInHand(Hand.MAIN_HAND, weapon);
            player.attack(entity);
            //displayItem.damage(1, player, p -> {});

            //TODO item damage handling?
        }
    }

    private Box getAttackBox(BlockPos pos) {
        int range = 1 + blockEntity.getRangeUpCount();
        switch (blockEntity.getFacing()) {
            case DOWN:
            case UP:
                return new Box(pos.add(-range, 0, -range), pos.add(range, 0, range));
            case NORTH:
            case SOUTH:
                return new Box(pos.add(-range, -range, 0), pos.add(range, range, 0));
            case WEST:
            case EAST:
                return new Box(pos.add(0, -range, -range), pos.add(0, range, range));
        }
        return new Box(pos, pos.add(1, 1, 1));
    }

    private ItemStack getNextStack() {
        FixedItemInv handler = blockEntity.getAttachedInventory();
        if (handler != null) {
            return handler.getExtractable().extract(new ItemClassFilter(SwordItem.class), 1);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();
        tag.put(NBT_WEAPON, weapon.toTag(new CompoundTag()));
        tag.putInt(NBT_ROTATION, rotation);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        if (tag.contains(NBT_WEAPON)) weapon = ItemStack.fromTag(tag.getCompound(NBT_WEAPON));
        if (tag.contains(NBT_ROTATION)) rotation = tag.getInt(NBT_ROTATION);
    }
}
