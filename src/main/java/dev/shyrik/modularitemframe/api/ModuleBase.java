package dev.shyrik.modularitemframe.api;

import dev.shyrik.modularitemframe.api.util.RegistryHelper;
import dev.shyrik.modularitemframe.client.FrameRenderer;
import dev.shyrik.modularitemframe.common.block.ModularFrameBlock;
import dev.shyrik.modularitemframe.common.block.ModularFrameEntity;
import dev.shyrik.modularitemframe.init.Registrar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
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
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class ModuleBase {
    protected ModularFrameEntity blockEntity;
    ModuleItem parent;

    protected final String NBT_RELOADMODEL = "reloadmodel";

    public void setTile(ModularFrameEntity te) {
        blockEntity = te;
    }

    public ModuleItem getParent() { return parent; }

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
        return ModularFrameBlock.INNER_DEF_LOC;
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

    public boolean reloadModel = false;
    private BakedModel bakedModel = null;

    /**
     * Called by the {@link FrameRenderer} to bake the Frame model
     * by default {@link #frontTexture()} and {@link #backTexture()} will be asked to be replaced
     * override this with caution.
     *
     * @param model Contains the model of the frame
     * @return baked model ofc
     */
    @Environment(EnvType.CLIENT)
    public BakedModel bakeModel(ModelLoader loader, UnbakedModel model) {
        if (bakedModel == null || reloadModel) {
            bakedModel = model.bake(loader, mat -> {
                if (mat.getTextureId().toString().contains("default_front"))
                    return MinecraftClient.getInstance().getBakedModelManager().method_24153(mat.getAtlasId()).getSprite(frontTexture());
                if (mat.getTextureId().toString().contains("default_back"))
                    return MinecraftClient.getInstance().getBakedModelManager().method_24153(mat.getAtlasId()).getSprite(backTexture());
                if (mat.getTextureId().toString().contains("default_inner"))
                    return MinecraftClient.getInstance().getBakedModelManager().method_24153(mat.getAtlasId()).getSprite(innerTexture());
                return MinecraftClient.getInstance().getBakedModelManager().method_24153(mat.getAtlasId()).getSprite(mat.getTextureId());
            }, ModelRotation.X0_Y0, RegistryHelper.getBlockId(Registrar.MODULARFRAME));

            reloadModel = false;
        }
        return bakedModel;
    }

    /**
     * Called by the {@link FrameRenderer} after rendering the frame.
     * Extra rendering can be don here
     * like the {@link ModuleItem ModuleItem} does the item thing)
     *
     * @param renderer instance of the current {@link FrameRenderer}
     */
    @Environment(EnvType.CLIENT)
    public void specialRendering(FrameRenderer renderer, MatrixStack matrixStack, float partialTicks, VertexConsumerProvider buffer, int combinedLight, int combinedOverlay) {
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
    public NamedScreenHandlerFactory getContainer(BlockState state, World world, BlockPos pos) {
        return null;
    }

    /**
     * called when the blockEntity entity ticks
     */
    public void tick(World world, BlockPos pos) {
    }

    /**
     * Called when module is removed with the {@link dev.shyrik.modularitemframe.common.item.ScrewdriverItem screwdriver}
     * or destroyed.
     */
    public void onRemove(World world, BlockPos pos, Direction facing, PlayerEntity player) {
    }

    /**
     * Tag serialization in case there are some data to be saved!
     * this gets synced automatically
     */
    public CompoundTag toTag() {
        CompoundTag cmp = new CompoundTag();
        cmp.putBoolean(NBT_RELOADMODEL, reloadModel);
        return cmp;
    }

    /**
     * Tag deserialization in case there are some data to be saved!
     */
    public void fromTag(CompoundTag cmp) {
        if (cmp.contains(NBT_RELOADMODEL)) reloadModel = cmp.getBoolean(NBT_RELOADMODEL);
    }
}
