package dev.shyrik.modularitemframe.api.util.fake;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;


import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class FakePlayerHelper {
    private static final Map<World, Map<GameProfile, FakePlayer>> PLAYERS = new WeakHashMap<>();

    /**
     * Only store this as a WeakReference, or you'll cause memory leaks.
     */
    public static FakePlayer getPlayer(World world, GameProfile profile) {
        return PLAYERS.computeIfAbsent(world, p -> new HashMap<>()).computeIfAbsent(profile, p -> {
            FakePlayer player = new FakePlayer((ServerWorld)world, profile);
            player.networkHandler = new SpaghettiNetworkHandler(player);
            return player;
        });
    }

    /**
     * Sets up for a fake player to be usable to right click things.  This player will be put at the center of the using side.
     * @param player The player.
     * @param pos The position of the using tile entity.
     * @param direction The direction to use in.
     * @param toHold The stack the player will be using.  Should probably come from an ItemStackHandler or similar.
     */
    public static void setupFakePlayerForUse(FakePlayer player, BlockPos pos, Direction direction, ItemStack toHold, boolean sneaking) {
        player.inventory.main.set(player.inventory.selectedSlot, toHold);
        //float pitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;
        float yaw = direction == Direction.SOUTH ? 0 : direction == Direction.WEST ? 90 : direction == Direction.NORTH ? 180 : -90;
        Vec3i sideVec = direction.getVector();
        Direction.Axis a = direction.getAxis();
        Direction.AxisDirection ad = direction.getDirection();
        double x = a == Direction.Axis.X && ad == Direction.AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getX() / 1.9D;
        double y = 0.5 + sideVec.getY() / 1.9D;
        double z = a == Direction.Axis.Z && ad == Direction.AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getZ() / 1.9D;
        player.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
        player.setHeadYaw(yaw);
        //player.applyRotation(BlockR)
        //pitch ?;
        if (!toHold.isEmpty()) player.getAttributes().addTemporaryModifiers(toHold.getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.setSneaking(sneaking);
    }

    /**
     * Cleans up the fake player after use.
     * @param player The player.
     * @param resultStack The stack that was returned from right/leftClickInDirection.
     * @param oldStack The previous stack, from before use.
     */
    public static void cleanupFakePlayerFromUse(FakePlayer player, ItemStack resultStack, ItemStack oldStack, Consumer<ItemStack> stackCallback) {
        if (!oldStack.isEmpty()) player.getAttributes().removeModifiers(oldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.inventory.main.set(player.inventory.selectedSlot, ItemStack.EMPTY);
        stackCallback.accept(resultStack);
        if (!player.inventory.isEmpty()) player.inventory.dropAll();
        player.setSneaking(false);
    }

    /**
     * Uses whatever the player happens to be holding in the given direction.
     * @param player The player.
     * @param world The world of the calling tile entity.  It may be a bad idea to use {@link FakePlayer#getEntityWorld()}.
     * @param pos The pos of the calling tile entity.
     * @param side The direction to use in.
     * @param sourceState The state of the calling tile entity, so we don't click ourselves.
     * @return The remainder of whatever the player was holding.  This should be set back into the tile's stack handler or similar.
     */
    public static ItemStack rightClickInDirection(FakePlayer player, World world, BlockPos pos, Direction side, BlockState sourceState, int range) {
        Vec3d base = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d look = player.getRotationVector();
        Vec3d target = base.add(look.x * range, look.y * range, look.z * range);
        HitResult trace = world.rayTrace(new RayTraceContext(base, target, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player));
        HitResult traceEntity = traceEntities(player, base, target, world);
        HitResult toUse = trace == null ? traceEntity : trace;

        if (trace != null && traceEntity != null) {
            double d1 = trace.getPos().distanceTo(base);
            double d2 = traceEntity.getPos().distanceTo(base);
            toUse = traceEntity.getType() == HitResult.Type.ENTITY && d1 > d2 ? traceEntity : trace;
        }

        if (toUse == null) return player.getMainHandStack();

        ItemStack itemstack = player.getMainHandStack();
        if (toUse.getType() == BlockHitResult.Type.ENTITY) {
            if (processUseEntity(player, world, ((EntityHitResult)toUse).getEntity(), toUse, PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT)) return player.getMainHandStack();
            else if (processUseEntity(player, world, ((EntityHitResult)toUse).getEntity(), null, PlayerInteractEntityC2SPacket.InteractionType.INTERACT)) return player.getMainHandStack();
        } else if (toUse.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)toUse).getBlockPos();
            BlockState state = world.getBlockState(blockpos);
            if (state != sourceState && state.getMaterial() != Material.AIR) {
                ActionResult resultType = player.interactionManager.interactBlock(player, world, itemstack, Hand.MAIN_HAND, (BlockHitResult)toUse);
                if (resultType == ActionResult.SUCCESS) return player.getMainHandStack();
            }
        }

        if(toUse.getType() == HitResult.Type.MISS) {
            for(int i = 1; i <= range; i++) {
                BlockState state = world.getBlockState(pos.offset(side, i));
                if (state != sourceState && state.getMaterial() != Material.AIR) {
                    player.interactionManager.interactBlock(player, world, itemstack, Hand.MAIN_HAND, (BlockHitResult) toUse);
                    return player.getMainHandStack();
                }
            }
        }

        //if (itemstack.isEmpty() && (toUse.getType() == HitResult.Type.MISS)) ForgeHooks.onEmptyClick(player, Hand.MAIN_HAND);
        if (!itemstack.isEmpty()) player.interactionManager.interactItem(player, world, itemstack, Hand.MAIN_HAND);
        return player.getMainHandStack();
    }

    /**
     * Attacks with whatever the player happens to be holding in the given direction.
     * @param player The player.
     * @param world The world of the calling tile entity.  It may be a bad idea to use {@link FakePlayer#getEntityWorld()}.
     * @param pos The pos of the calling tile entity.
     * @param side The direction to attack in.
     * @param sourceState The state of the calling tile entity, so we don't click ourselves.
     * @return The remainder of whatever the player was holding.  This should be set back into the tile's stack handler or similar.
     */
    public static ItemStack leftClickInDirection(FakePlayer player, World world, BlockPos pos, Direction side, BlockState sourceState, int range) {
        Vec3d base = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d look = player.getRotationVector();
        Vec3d target = base.add(look.x * range, look.y * range, look.z * range);
        HitResult  trace = world.rayTrace(new RayTraceContext(base, target, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player));
        HitResult  traceEntity = traceEntities(player, base, target, world);
        HitResult  toUse = trace == null ? traceEntity : trace;

        if (trace != null && traceEntity != null) {
            double d1 = trace.getPos().distanceTo(base);
            double d2 = traceEntity.getPos().distanceTo(base);
            toUse = traceEntity.getType() == HitResult.Type.ENTITY && d1 > d2 ? traceEntity : trace;
        }

        if (toUse == null)
            return player.getMainHandStack();

        if (toUse.getType() == HitResult.Type.ENTITY) {
            if (processUseEntity(player, world, ((EntityHitResult)toUse).getEntity(), null, PlayerInteractEntityC2SPacket.InteractionType.ATTACK))
                return player.getMainHandStack();
        } else if (toUse.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)toUse).getBlockPos();
            BlockState state = world.getBlockState(blockpos);
            if (state != sourceState && state.getMaterial() != Material.AIR) {
                player.interactionManager.processBlockBreakingAction(blockpos, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, ((BlockHitResult)toUse).getSide(), player.server.getWorldHeight());
                return player.getMainHandStack();
            }
        }

        if(toUse.getType() == HitResult.Type.MISS) {
            for(int i = 1; i <= 5; i++) {
                BlockState state = world.getBlockState(pos.offset(side, i));
                if (state != sourceState && state.getMaterial() != Material.AIR) {
                    player.interactionManager.processBlockBreakingAction(pos.offset(side, i), PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, side.getOpposite(), player.server.getWorldHeight());
                    return player.getMainHandStack();
                }
            }
        }

        //if (itemstack.isEmpty() && (toUse.getType() == HitResult.Type.MISS)) ForgeHooks.onEmptyLeftClick(player);
        return player.getMainHandStack();
    }

    /**
     * Traces for an entity.
     * @param player The player.
     * @param world The world of the calling tile entity.
     * @return A ray trace result that will likely be of type entity, but may be type block, or null.
     */
    public static HitResult traceEntities(FakePlayer player, Vec3d base, Vec3d target, World world) {
        Entity pointedEntity = null;
        HitResult result = null;
        Vec3d vec3d3 = null;
        Box search = new Box(base.x, base.y, base.z, target.x, target.y, target.z).expand(.5, .5, .5);
        List<Entity> list = world.getEntities(player, search, entity -> EntityPredicates.EXCEPT_SPECTATOR.test(entity) && entity != null && entity.collides());
        double d2 = 5;

        for (Entity entity1 : list) {
            Box aabb = entity1.getBoundingBox().expand(entity1.getCollisionBox().getAverageSideLength());
            Optional<Vec3d> hitVec = aabb.rayTrace(base, target);

            if (aabb.contains(base)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    vec3d3 = hitVec.orElse(base);
                    d2 = 0.0D;
                }
            } else if (hitVec.isPresent()) {
                double d3 = base.distanceTo(hitVec.get());

                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getPrimaryPassenger() == player.getPrimaryPassenger()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                            vec3d3 = hitVec.get();
                        }
                    } else {
                        pointedEntity = entity1;
                        vec3d3 = hitVec.get();
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && base.distanceTo(vec3d3) > 5) {
            pointedEntity = null;
            result = BlockHitResult.createMissed(vec3d3, Direction.NORTH, new BlockPos(vec3d3));
        }

        if (pointedEntity != null) {
            result = new EntityHitResult(pointedEntity, vec3d3);
        }

        return result;
    }

    /**
     * Processes the using of an entity from the server side.
     * @param player The player.
     * @param world The world of the calling tile entity.
     * @param entity The entity to interact with.
     * @param result The actual ray trace result, only necessary if using {@link PlayerInteractEntityC2SPacket.InteractionType#INTERACT_AT}
     * @param action The type of interaction to perform.
     * @return If the entity was used.
     */
    public static boolean processUseEntity(FakePlayer player, World world, Entity entity, HitResult result, PlayerInteractEntityC2SPacket.InteractionType action) {
        if (entity != null) {
            boolean flag = player.canSee(entity);
            double d0 = 36.0D;

            if (!flag) d0 = 9.0D;

            if (player.distanceTo(entity) < d0) {
                if (action == PlayerInteractEntityC2SPacket.InteractionType.INTERACT) {
                    return player.interact(entity, Hand.MAIN_HAND) == ActionResult.SUCCESS;
                } else if (action == PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT) {
                    //if (ForgeHooks.onInteractEntityAt(player, entity, result.getPos(), Hand.MAIN_HAND) != null) return false;
                    return entity.interactAt(player, result.getPos(), Hand.MAIN_HAND) == ActionResult.SUCCESS;
                } else if (action == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
                    if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ArrowEntity || entity == player) return false;
                    player.attack(entity);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A copy-paste of the SideOnly {@link Entity#rayTrace(double, float, boolean)}
     */
    public static HitResult rayTrace(FakePlayer player, World world, double reachDist, float partialTicks) {
        Vec3d vec3d = player.getCameraPosVec(partialTicks);
        Vec3d vec3d1 = player.getCameraPosVec(partialTicks);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * reachDist, vec3d1.y * reachDist, vec3d1.z * reachDist);
        return world.rayTrace(new RayTraceContext(vec3d, vec3d2, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, player));
    }
}
