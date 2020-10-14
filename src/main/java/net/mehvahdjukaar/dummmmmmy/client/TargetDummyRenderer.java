package net.mehvahdjukaar.dummmmmmy.client;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.dummmmmmy.Config;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class TargetDummyRenderer extends BipedModel<LivingEntity> {
    public ModelRenderer standPlate;
    public ModelRenderer newhead;
    public ModelRenderer newhead2;
    private float r = 0;
    private float r2 = 0;

    public TargetDummyRenderer() {
        this(0, 0f);
    }

    public TargetDummyRenderer(float size, float yOffsetIn) {
        this(size, yOffsetIn, 64, 64, 0);
    }

    public TargetDummyRenderer(float size, float yOffsetIn, int xw, int yw, float legOffset) {
        super(size, yOffsetIn, xw, yw);
        this.bipedRightArm = new ModelRenderer(this, 40, 16);
        this.bipedRightArm.addBox(-3.0F, 1.0F, -2.0F, 4, 8, 4.0F, size + 0.01f);
        this.bipedRightArm.setRotationPoint(-2.5F, -22.0F + yOffsetIn, 0.0F); // +2
        this.bipedLeftArm = new ModelRenderer(this, 40, 16);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, 1.0F, -2.0F, 4, 8, 4.0F, size + 0.01f);
        this.bipedLeftArm.setRotationPoint(2.5F, -22.0F + yOffsetIn, 0.0F);
        // left leg == stand
        this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
        this.bipedLeftLeg.addBox(-2.0F, -12.0F, -2.0F, 4, 12, 4, size + legOffset);
        this.bipedLeftLeg.setRotationPoint(0F, 24.0F + yOffsetIn, 0.0F);
        this.bipedRightLeg = new ModelRenderer(this, 0, 0);
        this.standPlate = new ModelRenderer(this, 0, 32);
        this.standPlate.addBox(-7.0F, 12F, -7.0F, 14, 1, 14, size);
        this.standPlate.setRotationPoint(0F, 11F + yOffsetIn, 0.0F);
        this.bipedBody = new ModelRenderer(this, 16, 16);
        // armor overlay size is slightly larger for leggins to prevent clipping armor
        this.bipedBody.addBox(-4.0F, -24.0F, -2.0F, 8, 12, 4, size); // -24
        this.bipedBody.setRotationPoint(0.0F, 24.0F + yOffsetIn, 0.0F);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        /*
         * this.bipedHead = new ModelRenderer(this, 0, 0); this.bipedHead.addBox(-4.0F,
         * -8.0F+ yOffsetIn, -4.0F, 8.0F, 8.0F, 8.0F, size);
         * this.bipedHead.setRotationPoint(0.0F, 0.0F , 0.0F);
         *
         */
        // don't know why but changing head rotation point does nothing. made newhead instead
        this.newhead = new ModelRenderer(this, 0, 0);
        this.newhead.setRotationPoint(0.0F, 24.0F + yOffsetIn, 0.0F);

        this.newhead2 = new ModelRenderer(this, 0, 0);
        this.newhead2.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, size);
        this.newhead2.setRotationPoint(0.0F, -24.0F + yOffsetIn, 0.0F);
        this.newhead.addChild(this.newhead2);
    }

    @Override
    public ModelRenderer getModelHead() {
        return this.getModelHeadWithOffset(-0.99f);
    }

    public ModelRenderer getModelHeadWithOffset(float offset) {
        // head parameters
        double hx = -4f;
        double hy = -8f;
        double hz = -4f;
        double hs = 8f;
        double hrx = 0f;
        double hry = -24f;
        double hrz = 0f;
        // can't find a bette solution for skull heads... hardcoding it is
        // same parameters and rotation as newhead2. hopefully won't get called often
        Vector3d v = new Vector3d(hx, hy + hry, hz).rotatePitch(-r / 2).add(0, -hry + offset, 0);
        Vector3d v2 = new Vector3d(hrx, hry, hrz).rotatePitch(-r / 2).add(0, -hry + offset, 0);
        ModelRenderer skullhead = new ModelRenderer(this, 0, 0);
        skullhead.addBox((float) v.getX(), (float) v.getY(), (float) v.getZ(), (float) hs, (float) hs, (float) hs, 1f);
        skullhead.setRotationPoint((float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
        skullhead.rotateAngleX = -r + r / 2;
        skullhead.rotateAngleZ = r2;
        return skullhead;
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green,
                       float blue, float alpha) {
        matrixStackIn.push();

        this.standPlate.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        matrixStackIn.translate(0, -0.0625, 0);
        this.bipedHead.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.newhead.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedLeftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedBody.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrixStackIn.pop();
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used
     * for animating the movement of arms and legs, where par1 represents the
     * time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    @Override
    public void setRotationAngles(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch) {
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleX = 0;
        this.bipedRightArm.rotateAngleX = 0;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedLeftArm.rotateAngleY = 0.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedHead.rotationPointY = 0.0F;
        // un-rotate the stand plate so it's aligned to the block grid
        this.standPlate.rotateAngleY = -(entityIn).rotationYaw / (180F / (float) Math.PI);
        this.bipedRightArm.rotateAngleZ = (float) Math.PI / 2f;
        this.bipedLeftArm.rotateAngleZ = -(float) Math.PI / 2f;

        float phase = ((TargetDummyEntity.CustomEntity) entityIn).shakeAnimation;
        float shake = Math.min((float) (((TargetDummyEntity.CustomEntity) entityIn).shake * Config.Configs.ANIMATION_INTENSITY.get()), 40f);
        this.r = 0;
        this.r2 = 0;
        //float r3=0;

        if (shake > 0) {
            r = (float) -(MathHelper.sin(phase) * Math.PI / 100f * shake);
            r2 = (float) (MathHelper.sin(phase) * Math.PI / 20f);
            //r3 = (float) -(MathHelper.sin(phase/2) * Math.PI / 100f * shake);

        }
        float n = 1.5f;
        this.bipedLeftArm.rotateAngleX = r * n;
        this.bipedRightArm.rotateAngleX = r * n;
        this.bipedLeftLeg.rotateAngleX = r / 2; // z instead of x because it's rotated 90�
        this.bipedBody.rotateAngleX = r / 2;
        // this.bipedHead.rotateAngleX = -r;
        // this.bipedHead.rotateAngleZ = r2;
        // this.newhead.setRotationPoint(0.0F, 0.0F , 0.0F);
        this.newhead2.rotateAngleX = -r; //-r
        //
        this.newhead2.rotateAngleZ = r2; //r2
        // this.newhead.setRotationPoint(0F, 24.0F + 0, 0.0F);
        this.newhead.rotateAngleX = r / 2;

        //I'm using this for the armor head cause for some reason in 1.16 I can't get it to animate at all
        this.bipedHead = this.getModelHeadWithOffset(0);
        this.bipedHead.showModel = false;
    }
}