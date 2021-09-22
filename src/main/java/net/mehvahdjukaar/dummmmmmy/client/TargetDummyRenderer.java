package net.mehvahdjukaar.dummmmmmy.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class TargetDummyRenderer extends BipedRenderer<TargetDummyEntity, TargetDummyModel<TargetDummyEntity>> {

    public TargetDummyRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TargetDummyModel<>(), 0);
        this.addLayer(new LayerDummyArmor<>(this, new TargetDummyModel<>(EquipmentSlotType.LEGS), new TargetDummyModel<>(EquipmentSlotType.CHEST)));
    }


    @Override
    public ResourceLocation getTextureLocation(TargetDummyEntity entity) {
        return Configs.cached.SKIN.getSkin(entity.sheared);
    }



    public void render2(TargetDummyEntity entity, float p_225623_2_, float p_225623_3_, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn) {
        super.render(entity, p_225623_2_, p_225623_3_, matrixStackIn, bufferIn, combinedLightIn);
        ItemStack stack= new ItemStack(Items.SAND);
        Framebuffer fb = new Framebuffer(16, 16, true, true);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        //itemRenderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);

        IBakedModel ibakedmodel = itemRenderer.getModel(stack, entity.level, null);

        Framebuffer mcFb = Minecraft.getInstance().getMainRenderTarget();
        fb.bindWrite(false);

        fb.blitToScreen(500,500,false);
        RenderSystem.pushMatrix();
        itemRenderer.renderAndDecorateItem(stack, 0, 0);
        RenderSystem.popMatrix();
        //fb.destroyBuffers();
        fb.destroyBuffers();

        mcFb.bindWrite(false);
        /*

        fb.bindRead();



        //itemRenderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);

        //itemRenderer.renderAndDecorateItem(stack, 0, 0);

        //fb.destroyBuffers();
        fb.unbindWrite();
        fb.unbindRead();


         */





        //matrixStackIn.translate(1,1,1);
        //matrixStackIn.scale(1F, 1F, 0.01F);


        //RenderHelper.turnBackOn();

        //Following 9 lines lifted from Storage Drawers. Spent ages trying to figure out lighting...
        //int ambLight = getWorld().getCombinedLight(te.getPos().offset(barrelFacing), 0);
        //int lu = ambLight % 65536;
        //int lv = ambLight / 65536;
        //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lu / 1f, lv / 1f);

        /*
        RenderSystem.enableRescaleNormal();  //This hack Storage Drawers uses is crazy!!!

        RenderSystem.disableRescaleNormal(); //I guess the purpose is to make the lighting

        matrixStackIn.pushPose();
        //still work when the item is flattened
        RenderSystem.enableRescaleNormal();
        matrixStackIn.popPose();
        */





        //RenderHelper.turnOff();

        //drawItem(matrixStackIn,bufferIn,stack,combinedLightIn);

    }

    //private static Framebuffer fb = new Framebuffer(16, 16, true, true);

    public static void drawItem(MatrixStack matrices, IRenderTypeBuffer buffer, ItemStack stack, int light) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        matrices.pushPose();




        Minecraft mc = Minecraft.getInstance();
        GraphicsFanciness cache = mc.options.graphicsMode;
        mc.options.graphicsMode = GraphicsFanciness.FANCY;







        //renderFastItem(renderStacks[i], tile, state, i, matrix, buffer, combinedLight, combinedOverlay, side, partialTickTime);


        //BlockDrawers block = (BlockDrawers)state.getBlock();
        //AxisAlignedBB labelGeometry = block.labelGeometry[slot];

        float scaleX = (float)8 / 16;
        float scaleY = (float)8 / 16;
        float moveX = (float)0 + (8 * scaleX);
        float moveY = 16f - (float)5 + (8 * scaleY);
        float moveZ = (float)0 * .0625f;


        matrices.pushPose();

        //alignRendering(matrix, side);
        moveRendering(matrices, scaleX, scaleY, moveX, moveY, moveZ);

        //List<IRenderLabel> renderHandlers = StorageDrawers.renderRegistry.getRenderHandlers();
        //for (IRenderLabel renderHandler : renderHandlers) {
        //    renderHandler.render(tile, tile.getGroup(), slot, 0, partialTickTime);
        //}

        Consumer<IRenderTypeBuffer> finish = (IRenderTypeBuffer buf) -> {
            if (buf instanceof IRenderTypeBuffer.Impl)
                ((IRenderTypeBuffer.Impl) buf).endBatch();
        };

        try {
            matrices.translate(0, 0, 100f);
            matrices.scale(1, -1, 1);
            matrices.scale(16, 16, 16);

            //IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            IBakedModel itemModel = itemRenderer.getModel(stack, null, null);
            boolean render3D = itemModel.isGui3d(); // itemModel.func_230044_c_();
            finish.accept(buffer);

            if (render3D)
                RenderHelper.setupFor3DItems();
            else
                RenderHelper.setupForFlatItems();

            //matrices.last().normal().set(Matrix3f.createScaleMatrix(1, -1, 1));
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.GUI, false, matrices, buffer, light, OverlayTexture.NO_OVERLAY, itemModel);
            finish.accept(buffer);
        }
        catch (Exception e) {
            // Shrug
        }


        mc.options.graphicsMode = cache;
        matrices.popPose();
        RenderHelper.setupLevel(matrices.last().pose());
        matrices.popPose();
    }


    private static void moveRendering (MatrixStack matrix, float scaleX, float scaleY, float offsetX, float offsetY, float offsetZ) {
        // NOTE: RenderItem expects to be called in a context where Y increases toward the bottom of the screen
        // However, for in-world rendering the opposite is true. So we translate up by 1 along Y, and then flip
        // along Y. Since the item is drawn at the back of the drawer, we also translate by `1-offsetZ` to move
        // it to the front.

        // The 0.00001 for the Z-scale both flattens the item and negates the 32.0 Z-scale done by RenderItem.

        matrix.translate(0, 1, 1-offsetZ);
        matrix.scale(1 / 16f, -1 / 16f, 0.00005f);

        matrix.translate(offsetX, offsetY, 0);
        matrix.scale(scaleX, scaleY, 1);
    }

}
