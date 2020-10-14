package net.mehvahdjukaar.dummmmmmy.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.dummmmmmy.Config;
import net.mehvahdjukaar.dummmmmmy.entity.DummyNumberEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.text.DecimalFormat;

public class NumberRenderer extends EntityRenderer<DummyNumberEntity.CustomEntity> {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    public NumberRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(DummyNumberEntity.CustomEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                       int packedLightIn) {
        FontRenderer fontrenderer = this.renderManager.getFontRenderer();
        matrixStackIn.push();
        // translate towards player
        PlayerEntity player = Minecraft.getInstance().player;


        Vector3d v = (player.getPositionVec().subtract(entityIn.getPositionVec())).normalize();
        matrixStackIn.translate(v.getX(), v.getY(), v.getZ());
        // animation
        matrixStackIn.translate(0, MathHelper.lerp(partialTicks, entityIn.prevDy, entityIn.dy), 0);
        // rotate towards camera
        double d = Math.sqrt(this.renderManager.getDistanceToCamera(entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ()));


        float fadeout =entityIn.fadeout;

        float defscale = 0.006f;
        float scale = (float) (defscale * d);
        matrixStackIn.rotate(this.renderManager.getCameraOrientation());
        // matrixStackIn.translate(0, 0, -1);
        // animation
        matrixStackIn.translate(MathHelper.lerp(partialTicks, entityIn.prevDx, entityIn.dx),0, 0);
        // scale depending on distance so size remains the same
        matrixStackIn.scale(-scale, -scale, scale);
        matrixStackIn.translate(0, (4d*(1-fadeout)) , 0);
        matrixStackIn.scale(fadeout, fadeout, fadeout);
        matrixStackIn.translate(0,  -d / 10d, 0);

        float number = Config.Configs.SHOW_HEARTHS.get()? entityIn.getNumber()/2f : entityIn.getNumber();
        String s = df.format(number);
        // center string
        matrixStackIn.translate((-fontrenderer.getStringWidth(s) / 2f) + 0.5f, 0, 0);
        fontrenderer.renderString(s, 0, 0, entityIn.color, true, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, packedLightIn);
        // matrixStackIn.translate(fontrenderer.getStringWidth(s) / 2, 0, 0);
        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(DummyNumberEntity.CustomEntity entity) {
        return null;
    }
}
