package dev.shyrik.modularitemframe.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;


import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class FakePlayerHelper {
    private static final Map<World, Map<GameProfile, UsefulFakePlayer>> PLAYERS = new WeakHashMap<>();

    public static class UsefulFakePlayer extends F
// there is no such thing as a fake player in fabric... exploring atmakePlayer {

        public UsefulFakePlayer(World world, GameProfile name) {
            super((ServerWorld) world, name);
        }

        @Override
        public float getEyeHeight(EntityPose pose) {
            return 0; //Allows for the position of the player to be the exact source when raytracing.
        }

        @Override
        public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
            //Prevent crashing when objects with containers are clicked on.
        }

        @Override
        public float getCooledAttackStrength(float adjustTicks) {
            return 1; //Prevent the attack strength from always being 0.03 due to not ticking.
        }
    }

    /**
     * Only store this as a WeakReference, or you'll cause memory leaks.
     */
    public static UsefulFakePlayer getPlayer(World world, GameProfile profile) {
        return PLAYERS.computeIfAbsent(world, p -> new HashMap<>()).computeIfAbsent(profile, p -> {
            UsefulFakePlayer player = new UsefulFakePlayer(world, profile);
            player.connection = new NetHandlerSpaghettiServer(player);
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
    public static void setupFakePlayerForUse(UsefulFakePlayer player, BlockPos pos, Direction direction, ItemStack toHold, boolean sneaking) {
        player.inventory.mainInventory.set(player.inventory.currentItem, toHold);
        float pitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;
        float yaw = direction == Direction.SOUTH ? 0 : direction == Direction.WEST ? 90 : direction == Direction.NORTH ? 180 : -90;
        Vec3i sideVec = direction.getDirectionVec();
        Direction.Axis a = direction.getAxis();
        Direction.AxisDirection ad = direction.getAxisDirection();
        double x = a == Direction.Axis.X && ad == Direction.AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getX() / 1.9D;
        double y = 0.5 + sideVec.getY() / 1.9D;
        double z = a == Direction.Axis.Z && ad == Direction.AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getZ() / 1.9D;
        player.setLocationAndAngles(pos.getX() + x, pos.getY() + y, pos.getZ() + z, yaw, pitch);
        if (!toHold.isEmpty()) player.getAttributes().applyAttributeModifiers(toHold.getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.setSneaking(sneaking);
    }

    /**
     * Cleans up the fake player after use.
     * @param player The player.
     * @param resultStack The stack that was returned from right/leftClickInDirection.
     * @param oldStack The previous stack, from before use.
     */
    public static void cleanupFakePlayerFromUse(UsefulFakePlayer player, ItemStack resultStack, ItemStack oldStack, Consumer<ItemStack> stackCallback) {
        if (!oldStack.isEmpty()) player.getAttributes().removeAttributeModifiers(oldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
        stackCallback.accept(resultStack);
        if (!player.inventory.isEmpty()) player.inventory.dropAllItems();
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
    public static ItemStack rightClickInDirection(UsefulFakePlayer player, World world, BlockPos pos, Direction side, BlockState sourceState, int range) {
        Vec3d base = new Vec3d(player.getPosX(), player.getPosY(), player.getPosZ());
        Vec3d look = player.getLookVec();
        Vec3d target = base.add(look.x * range, look.y * range, look.z * range);
        RayTraceResult trace = world.rayTraceBlocks(new RayTraceContext(base, target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
        RayTraceResult traceEntity = traceEntities(player, base, target, world);
        RayTraceResult toUse = trace == null ? traceEntity : trace;

        if (trace != null && traceEntity != null) {
            double d1 = trace.getHitVec().distanceTo(base);
            double d2 = traceEntity.getHitVec().distanceTo(base);
            toUse = traceEntity.getType() == RayTraceResult.Type.ENTITY && d1 > d2 ? traceEntity : trace;
        }

        if (toUse == null) return player.getHeldItemMainhand();

        ItemStack itemstack = player.getHeldItemMainhand();
        if (toUse.getType() == RayTraceResult.Type.ENTITY) {
            if (processUseEntity(player, world, ((EntityRayTraceResult)toUse).getEntity(), toUse, CUseEntityPacket.Action.INTERACT_AT)) return player.getHeldItemMainhand();
            else if (processUseEntity(player, world, ((EntityRayTraceResult)toUse).getEntity(), null, CUseEntityPacket.Action.INTERACT)) return player.getHeldItemMainhand();
        } else if (toUse.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult)toUse).getPos();
            BlockState state = world.getBlockState(blockpos);
            if (state != sourceState && state.getMaterial() != Material.AIR) {
                ActionResultType resultType = player.interactionManager.func_219441_a(player, world, itemstack, Hand.MAIN_HAND, (BlockRayTraceResult)toUse);
                if (resultType == ActionResultType.SUCCESS) return player.getHeldItemMainhand();
            }
        }

        if(toUse == null || toUse.getType() == RayTraceResult.Type.MISS) {
            for(int i = 1; i <= range; i++) {
                BlockState state = world.getBlockState(pos.offset(side, i));
                if (state != sourceState && state.getMaterial() != Material.AIR) {
                    player.interactionManager.func_219441_a(player, world, itemstack, Hand.MAIN_HAND, (BlockRayTraceResult) toUse);
                    return player.getHeldItemMainhand();
                }
            }
        }

        if (itemstack.isEmpty() && (toUse.getType() == RayTraceResult.Type.MISS)) ForgeHooks.onEmptyClick(player, Hand.MAIN_HAND);
        if (!itemstack.isEmpty()) player.interactionManager.processRightClick(player, world, itemstack, Hand.MAIN_HAND);
        return player.getHeldItemMainhand();
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
    public static ItemStack leftClickInDirection(UsefulFakePlayer player, World world, BlockPos pos, Direction side, BlockState sourceState, int range) {
        Vec3d base = new Vec3d(player.getPosX(), player.getPosY(), player.getPosZ());
        Vec3d look = player.getLookVec();
        Vec3d target = base.add(look.x * range, look.y * range, look.z * range);
        RayTraceResult trace = world.rayTraceBlocks(new RayTraceContext(base, target, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
        RayTraceResult traceEntity = traceEntities(player, base, target, world);
        RayTraceResult toUse = trace == null ? traceEntity : trace;

        if (trace != null && traceEntity != null) {
            double d1 = trace.getHitVec().distanceTo(base);
            double d2 = traceEntity.getHitVec().distanceTo(base);
            toUse = traceEntity.getType() == RayTraceResult.Type.ENTITY && d1 > d2 ? traceEntity : trace;
        }

        if (toUse == null) return player.getHeldItemMainhand();

        ItemStack itemstack = player.getHeldItemMainhand();
        if (toUse.getType() == RayTraceResult.Type.ENTITY) {
            if (processUseEntity(player, world, ((EntityRayTraceResult)toUse).getEntity(), null, CUseEntityPacket.Action.ATTACK)) return player.getHeldItemMainhand();
        } else if (toUse.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult)toUse).getPos();
            BlockState state = world.getBlockState(blockpos);
            if (state != sourceState && state.getMaterial() != Material.AIR) {
                player.interactionManager.func_225416_a(blockpos, CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, ((BlockRayTraceResult)toUse).getFace(), player.server.getBuildLimit());
                return player.getHeldItemMainhand();
            }
        }

        if(toUse.getType() == RayTraceResult.Type.MISS) {
            for(int i = 1; i <= 5; i++) {
                BlockState state = world.getBlockState(pos.offset(side, i));
                if (state != sourceState && state.getMaterial() != Material.AIR) {
                    player.interactionManager.func_225416_a(pos.offset(side, i), CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, side.getOpposite(), player.server.getBuildLimit());
                    return player.getHeldItemMainhand();
                }
            }
        }

        if (itemstack.isEmpty() && (toUse == null || toUse.getType() == RayTraceResult.Type.MISS)) ForgeHooks.onEmptyLeftClick(player);
        return player.getHeldItemMainhand();
    }

    /**
     * Traces for an entity.
     * @param player The player.
     * @param world The world of the calling tile entity.
     * @return A ray trace result that will likely be of type entity, but may be type block, or null.
     */
    public static RayTraceResult traceEntities(UsefulFakePlayer player, Vec3d base, Vec3d target, World world) {
        Entity pointedEntity = null;
        RayTraceResult result = null;
        Vec3d vec3d3 = null;
        AxisAlignedBB search = new AxisAlignedBB(base.x, base.y, base.z, target.x, target.y, target.z).grow(.5, .5, .5);
        List<Entity> list = world.getEntitiesInAABBexcluding(player, search, entity -> EntityPredicates.NOT_SPECTATING.test(entity) && entity != null && entity.canBeCollidedWith());
        double d2 = 5;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);

            AxisAlignedBB aabb = entity1.getBoundingBox().grow(entity1.getCollisionBorderSize());
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
                    if (entity1.getLowestRidingEntity() == player.getLowestRidingEntity() && !entity1.canRiderInteract()) {
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
            result = BlockRayTraceResult.createMiss(vec3d3, Direction.NORTH, new BlockPos(vec3d3));
        }

        if (pointedEntity != null) {
            result = new EntityRayTraceResult(pointedEntity, vec3d3);
        }

        return result;
    }

    /**
     * Processes the using of an entity from the server side.
     * @param player The player.
     * @param world The world of the calling tile entity.
     * @param entity The entity to interact with.
     * @param result The actual ray trace result, only necessary if using {@link CUseEntityPacket.Action#INTERACT_AT}
     * @param action The type of interaction to perform.
     * @return If the entity was used.
     */
    public static boolean processUseEntity(UsefulFakePlayer player, World world, Entity entity, @Nullable RayTraceResult result, CUseEntityPacket.Action action) {
        if (entity != null) {
            boolean flag = player.canEntityBeSeen(entity);
            double d0 = 36.0D;

            if (!flag) d0 = 9.0D;

            if (player.getDistanceSq(entity) < d0) {
                if (action == CUseEntityPacket.Action.INTERACT) {
                    return player.interactOn(entity, Hand.MAIN_HAND) == ActionResultType.SUCCESS;
                } else if (action == CUseEntityPacket.Action.INTERACT_AT) {
                    if (ForgeHooks.onInteractEntityAt(player, entity, result.getHitVec(), Hand.MAIN_HAND) != null) return false;
                    return entity.applyPlayerInteraction(player, result.getHitVec(), Hand.MAIN_HAND) == ActionResultType.SUCCESS;
                } else if (action == CUseEntityPacket.Action.ATTACK) {
                    if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ArrowEntity || entity == player) return false;
                    player.attackTargetEntityWithCurrentItem(entity);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A copy-paste of the SideOnly {@link Entity#rayTrace(double, float)}
     */
    public static RayTraceResult rayTrace(UsefulFakePlayer player, World world, double reachDist, float partialTicks) {
        Vec3d vec3d = player.getEyePosition(partialTicks);
        Vec3d vec3d1 = player.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * reachDist, vec3d1.y * reachDist, vec3d1.z * reachDist);
        return world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
    }

    public static class NetHandlerSpaghettiServer extends ServerPlayNetHandler {

        public NetHandlerSpaghettiServer(UsefulFakePlayer player) {
            super(null, new NetworkManager(PacketDirection.CLIENTBOUND), player);
        }

        @Override
        public void disconnect(ITextComponent textComponent) {
        }

        @Override
        public void onDisconnect(ITextComponent reason) {
        }

        @Override
        public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
        }

        @Override
        public void func_217261_a(CLockDifficultyPacket p_217261_1_) {
        }

        @Override
        public void func_217263_a(CSetDifficultyPacket p_217263_1_) {
        }

        @Override
        public void func_217262_a(CUpdateJigsawBlockPacket p_217262_1_) {
        }

        @Override
        public void handleAnimation(CAnimateHandPacket packetIn) {
        }

        @Override
        public void processClientStatus(CClientStatusPacket packetIn) {
        }

        @Override
        public void processPlayer(CPlayerPacket packetIn) {
        }

        @Override
        public void processEnchantItem(CEnchantItemPacket packetIn) {
        }

        @Override
        public void processCloseWindow(CCloseWindowPacket packetIn) {
        }

        @Override
        public void handleSeenAdvancements(CSeenAdvancementsPacket packetIn) {
        }

        @Override
        public void processVehicleMove(CMoveVehiclePacket packetIn) {
        }

        @Override
        public void handleRecipeBookUpdate(CRecipeInfoPacket packetIn) {
        }

        @Override
        public void handleResourcePackStatus(CResourcePackStatusPacket packetIn) {
        }

        @Override
        public void processChatMessage(CChatMessagePacket packetIn) {
        }

        @Override
        public void handleSpectate(CSpectatePacket packetIn) {
        }

        @Override
        public void processClientSettings(CClientSettingsPacket packetIn) {
        }

        @Override
        public void processClickWindow(CClickWindowPacket packetIn) {
        }

        @Override
        public void processCustomPayload(CCustomPayloadPacket packetIn) {
        }

        @Override
        public void processCreativeInventoryAction(CCreativeInventoryActionPacket packetIn) {
        }

        @Override
        public void processConfirmTeleport(CConfirmTeleportPacket packetIn) {
        }

        @Override
        public void processEntityAction(CEntityActionPacket packetIn) {
        }

        @Override
        public void processConfirmTransaction(CConfirmTransactionPacket packetIn) {
        }

        @Override
        public void processInput(CInputPacket packetIn) {
        }

        @Override
        public void processEditBook(CEditBookPacket packetIn) {
        }

        @Override
        public void processNBTQueryBlockEntity(CQueryTileEntityNBTPacket packetIn) {
        }

        @Override
        public void processHeldItemChange(CHeldItemChangePacket packetIn) {
        }

        @Override
        public void processPlaceRecipe(CPlaceRecipePacket packetIn) {
        }

        @Override
        public void processKeepAlive(CKeepAlivePacket packetIn) {
        }

        @Override
        public void processPlayerDigging(CPlayerDiggingPacket packetIn) {
        }

        @Override
        public void processNBTQueryEntity(CQueryEntityNBTPacket packetIn) {
        }

        @Override
        public void processSelectTrade(CSelectTradePacket packetIn) {
        }

        @Override
        public void processPickItem(CPickItemPacket packetIn) {
        }

        @Override
        public void processTabComplete(CTabCompletePacket packetIn) {
        }

        @Override
        public void processPlayerAbilities(CPlayerAbilitiesPacket packetIn) {
        }

        @Override
        public void processTryUseItemOnBlock(CPlayerTryUseItemOnBlockPacket packetIn) {
        }

        @Override
        public void processSteerBoat(CSteerBoatPacket packetIn) {
        }

        @Override
        public void processUpdateCommandBlock(CUpdateCommandBlockPacket packetIn) {
        }

        @Override
        public void processRenameItem(CRenameItemPacket packetIn) {
        }

        @Override
        public void processUpdateSign(CUpdateSignPacket packetIn) {
        }

        @Override
        public void processTryUseItem(CPlayerTryUseItemPacket packetIn) {
        }

        @Override
        public void processUseEntity(CUseEntityPacket packetIn) {
        }

        @Override
        public void processUpdateCommandMinecart(CUpdateMinecartCommandBlockPacket packetIn) {
        }

        @Override
        public void processUpdateStructureBlock(CUpdateStructureBlockPacket packetIn) {
        }

        @Override
        public void processUpdateBeacon(CUpdateBeaconPacket packetIn) {
        }

        @Override
        public void sendPacket(IPacket<?> packetIn, @Nullable GenericFutureListener<? extends Future<? super Void>> futureListeners) {
        }

        @Override
        public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPlayerPositionLookPacket.Flags> relativeSet) {
        }

        @Override
        public void sendPacket(IPacket<?> packetIn) {
        }

        @Override
        public void tick() {
        }

    }
}
