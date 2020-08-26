package dev.shyrik.modularitemframe.api;

import com.google.common.collect.ImmutableList;
import dev.shyrik.modularitemframe.ModularItemFrame;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public abstract class ModuleBase {
    protected ModularFrameEntity frame;
    ModuleItem item;

    public void setTile(ModularFrameEntity te) {
        frame = te;
    }

    public ModuleItem getItem() { return item; }

    public abstract Identifier getId();

    /**
     * Is called when the {@link FrameRenderer} wants to render the module for the first time.
     *
     * @return [Nonnull] {@link Identifier} to the Texture
     */
    @Environment(EnvType.CLIENT)
    public abstract Identifier frontTexture();

    /**
     * Is called when the {@link FrameRenderer} wants to render the module for the first time.
     *
     * @return [Nonnull] {@link Identifier} to the Texture
     */
    @Environment(EnvType.CLIENT)
    public Identifier innerTexture() {
        return ModularFrameBlock.INNER_DEF;
    }

    /**
     * Is called when the {@link FrameRenderer} wants to render the module for the first time.
     *
     * @return [Nonnull] {@link Identifier} to the Texture
     */
    @Environment(EnvType.CLIENT)
    public Identifier backTexture() {
        return new Identifier("minecraft", "block/stripped_birch_log_top");
    }

    /**
     * TOP and WAILA are using this for display
     * Please use translation holders - raw strings are bad!
     *
     * @return the name of the module :O
     */
    @Environment(EnvType.CLIENT)
    public abstract String getModuleName();

    public List<Identifier> getVariantFronts() {
        return ImmutableList.of(frontTexture());
    }

    /**
     * Called by the {@link FrameRenderer} after rendering the frame.
     * Extra rendering can be don here
     * like the {@link ModuleItem ModuleItem} does the item thing)
     *
     * @param renderer instance of the current {@link FrameRenderer}
     */
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float ticks, VertexConsumerProvider buffer, int light, int overlay) {
    }

    /**
     * Called when the frame got left clicked
     */
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
    }

    /**
     * Called when a {@link dev.shyrik.modularitemframe.common.item.ScrewdriverItem screwdriver} in interaction mode clicks a frame
     * Implement behavior for {@link dev.shyrik.modularitemframe.common.item.ScrewdriverItem screwdriver} interaction here
     *
     * @param driver the driver who was used
     */
    public void screw(World world, BlockPos pos, PlayerEntity player, ItemStack driver) {
    }

    public void onFrameUpgradesChanged() {
    }

    /**
     * Called when a frame is simply right clicked
     */
    public abstract ActionResult onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, BlockHitResult trace);

    /**
     * in case your module has a gui
     */
    public NamedScreenHandlerFactory getScreenHandler(BlockState state, World world, BlockPos pos) {
        return null;
    }

    /**
     * called when the blockEntity entity ticks
     */
    public void tick(World world, BlockPos pos) {
    }

    /**
     * Called when module is removed with the {@link dev.shyrik.modularitemframe.common.item.ScrewdriverItem screwdriver}
    public void onInsert(World world, BlockPos pos, Direction facing, ItemStack moduleStack) {
    }
     * or destroyed.
     */
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player, ItemStack moduleStack) {
    }

    /**
     * Helper method which safe checks ticks
     */
    public boolean canTick(World world, int base, int mod) {
        return world.getTime() % Math.max(base - mod * frame.getSpeedUpCount(), 10) == 0;
    }

    /**
     * Tag serialization in case there are some data to be saved!
     * this gets synced automatically
     */
    public CompoundTag toTag() {
        return new CompoundTag();
    }

    /**
     * Tag deserialization in case there are some data to be saved!
     */
    public void fromTag(CompoundTag tag) {
    }

    //region <helper>
    /**
     * Forwarded from blockEntity
     */
    public void markDirty() {
        frame.markDirty();
    }

    protected Box getScanBox() {
        BlockPos pos = frame.getPos();
        int range = frame.getRangeUpCount() + ModularItemFrame.getConfig().scanZoneRadius;
        switch (frame.getFacing()) {
            case DOWN:
                return new Box(pos.add(-range, 0, -range), pos.add(range, -range, range));
            case UP:
                return new Box(pos.add(-range, 0, -range), pos.add(range, range, range));
            case NORTH:
                return new Box(pos.add(-range, -range, 0), pos.add(range, range, -range));
            case SOUTH:
                return new Box(pos.add(-range, -range, 0), pos.add(range, range, range));
            case WEST:
                return new Box(pos.add(0, -range, -range), pos.add(-range, range, range));
            case EAST:
                return new Box(pos.add(0, -range, -range), pos.add(range, range, range));
        }
        return new Box(pos, pos.add(1, 1, 1));
    }
    //endregion
}
