package net.mehvahdjukaar.dummmmmmy.client;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.dummmmmmy.Config;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class TargetDummyModel<T extends LivingEntity> extends BipedModel<T> {
    public ModelRenderer standPlate;

    private float r = 0;
    private float r2 = 0;
    protected EquipmentSlotType slot = EquipmentSlotType.CHEST;

    //armor layer constructor
    public TargetDummyModel(EquipmentSlotType slot) {
        super(1);
        this.slot = slot;
        float size = 1;
        if(slot == EquipmentSlotType.LEGS) size = 0.5f;
        constructor(size);
        if(this instanceof  TargetDummyModel){
            ((TargetDummyModel) this).standPlate.showModel = false;
        }
    }
    //normal model constructor. had to make two cause it was causing crashed with mods.
    public TargetDummyModel() {
        super(0,0,64,64);
        this.constructor(0);
    }


    public void constructor(float size){

        float yOffsetIn =-1;
        //scale leg to prevent some clipping
        float legOffset = -0.01f;

        this.standPlate = new ModelRenderer(this, 0, 32);
        this.standPlate.addBox(-7.0F, 12F, -7.0F, 14, 1, 14, size);
        this.standPlate.setRotationPoint(0F, 11F , 0.0F);

        this.bipedRightArm = new ModelRenderer(this, 40, 16);
        this.bipedRightArm.addBox(-3.0F, 1.0F, -2.0F, 4, 8, 4.0F, size+0.01f);
        this.bipedRightArm.setRotationPoint(-2.5F, 2.0F + yOffsetIn, -0.005F);
        this.bipedLeftArm = new ModelRenderer(this, 40, 16);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, 1.0F, -2.0F, 4, 8, 4.0F, size+0.01f);
        this.bipedLeftArm.setRotationPoint(2.5F, 2.0F + yOffsetIn, -0.005F);

        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, size);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + yOffsetIn, 0.0F);

        this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, size+legOffset);
        this.bipedLeftLeg.setRotationPoint(0F, 12.0F + yOffsetIn, 0.0F);
        this.bipedRightLeg = new ModelRenderer(this, 0, 0);

        this.bipedBody = new ModelRenderer(this, 16, 16);
        this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, size );
        this.bipedBody.setRotationPoint(0.0F, 0.0F + yOffsetIn, 0.0F);

    }

    /*
    @Override
    public ModelRenderer getModelHead() {
        //return this.getModelHeadWithOffset(-0.99f);
        return this.getModelHeadWithOffset(0);
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
        //this has the benefit of making it work with mods!
        Vector3d v = new Vector3d(hx, hy + hry, hz).rotatePitch(-r / 2).add(0, -hry + offset, 0);
        Vector3d v2 = new Vector3d(hrx, hry, hrz).rotatePitch(-r / 2).add(0, -hry + offset, 0);
        ModelRenderer skullhead = new ModelRenderer(this, 0, 0);
        skullhead.addBox((float) v.getX(), (float) v.getY(), (float) v.getZ(), (float) hs, (float) hs, (float) hs, 1f);
        skullhead.setRotationPoint((float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
        skullhead.rotateAngleX = -r + r / 2;
        skullhead.rotateAngleZ = r2;
        return skullhead;
    }

*/
    public void rotateModelX(ModelRenderer model, float nrx, float nry, float nrz, float angle){
        Vector3d oldrot = new Vector3d(model.rotationPointX, model.rotationPointY, model.rotationPointZ);
        Vector3d actualrot = new Vector3d(nrx, nry, nrz);

        Vector3d newrot = actualrot.add(oldrot.subtract(actualrot).rotatePitch(-angle));

        model.setRotationPoint((float) newrot.getX(), (float) newrot.getY(), (float) newrot.getZ());
        model.rotateAngleX = angle;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green,
                       float blue, float alpha) {
        matrixStackIn.push();

        this.standPlate.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        this.bipedHead.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedRightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedLeftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedBody.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.bipedLeftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        matrixStackIn.pop();
    }


    @Override
    public void setRotationAngles(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch) {


        // un-rotate the stand plate so it's aligned to the block grid
        this.standPlate.rotateAngleY = -(entityIn).rotationYaw / (180F / (float) Math.PI);

        //rotate arms
        this.bipedRightArm.rotateAngleZ = (float) Math.PI / 2f;
        this.bipedLeftArm.rotateAngleZ = -(float) Math.PI / 2f;

        float phase = ((TargetDummyEntity.DummyMob) entityIn).shakeAnimation;
        float shake = Math.min((float) (((TargetDummyEntity.DummyMob) entityIn).shake * Config.Configs.ANIMATION_INTENSITY.get()), 40f);

        if (shake > 0) {
            r = (float) -(MathHelper.sin(phase) * Math.PI / 100f * shake);
            r2 = (float) (MathHelper.sin(phase) * Math.PI / 20f);
        }
        else{
            r = 0;
            r2 = 0;
        }
        float n = 1.5f;

        //------new---------

        float yOffsetIn = -1;

        float xangle = r/2;

        this.bipedLeftLeg.setRotationPoint(0, 12.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.bipedLeftLeg, 0, 24 + yOffsetIn, 0, xangle);

        this.bipedBody.setRotationPoint(0.0F, 0.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.bipedBody, 0, 24 + yOffsetIn, 0, xangle);

        this.bipedRightArm.setRotationPoint(-2.5F, 2.0F + yOffsetIn, -0.005F);
        this.rotateModelX(this.bipedRightArm, 0, 24 + yOffsetIn, 0, xangle);

        this.bipedLeftArm.setRotationPoint(2.5F, 2.0F + yOffsetIn, -0.005F);
        this.rotateModelX(this.bipedLeftArm, 0, 24 + yOffsetIn, 0, xangle);

        this.bipedHead.setRotationPoint(0.0F, 0.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.bipedHead, 0, 24 + yOffsetIn, 0, xangle);

        this.bipedRightArm.rotateAngleX = r * n;
        this.bipedLeftArm.rotateAngleX = r * n;

        this.bipedHead.rotateAngleX = -r; //-r
        this.bipedHead.rotateAngleZ = r2; //r2

    }
}