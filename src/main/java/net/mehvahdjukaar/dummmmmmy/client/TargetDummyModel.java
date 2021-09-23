package net.mehvahdjukaar.dummmmmmy.client;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TargetDummyModel<T extends TargetDummyEntity> extends HumanoidModel<T> {
    public final ModelPart standPlate;

    private float r = 0;
    private float r2 = 0;

    public TargetDummyModel(ModelPart modelPart) {
        super(modelPart);
        standPlate = modelPart.getChild("stand");
    }


    public static LayerDefinition createLayer(float size) {
        CubeDeformation deformation = new CubeDeformation(size);
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("stand", CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-7.0F, 12F, -7.0F, 14F, 1F, 14F, deformation),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, 1.0F, -2.0F, 4.0F, 8F, 4.0F, deformation.extend(0.01f)),
                PartPose.offset(-2.5F, 2.0F, -0.005F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror()
                .addBox(-1.0F, 1.0F, -2.0F, 4.0F, 8F, 4.0F, deformation.extend(0.01f)),
                PartPose.offset(2.5F, 2.0F, -0.005F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createArmorLayer(float size) {
        CubeDeformation deformation = new CubeDeformation(size);
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("stand", CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-7.0F, 12F, -7.0F, 14F, 1F, 14F, deformation),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, 1.0F, -2.0F, 4.0F, 8F, 4.0F, deformation.extend(0.01f)),
                PartPose.offset(-2.5F, 2.0F, -0.005F));

        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(40, 16).mirror()
                        .addBox(-1.0F, 1.0F, -2.0F, 4.0F, 8F, 4.0F, deformation.extend(0.01f)),
                PartPose.offset(2.5F, 2.0F, -0.005F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }


    /*

    //armor layer constructor
    public TargetDummyModel(EquipmentSlot slot) {
        super(1);
        this.slot = slot;
        float size = 1;
        if(slot == EquipmentSlot.LEGS) size = 0.5f;
        constructor(size);
        this.standPlate.visible = false;
        this.rightLeg.visible = false;
    }
    //normal model constructor. had to make two cause it was causing crashes with mods.
    public TargetDummyModel() {
        super(0,0,64,64);
        this.constructor(0);
    }


    public void constructor(float size){

        float yOffsetIn =-1;
        //scale leg to prevent some clipping
        float legOffset = -0.01f;

        this.standPlate = new ModelPart(this, 0, 32);
        this.standPlate.addBox(-7.0F, 12F, -7.0F, 14, 1, 14, size);
        this.standPlate.setPos(0F, 11F , 0.0F);

        this.rightArm = new ModelPart(this, 40, 16);
        this.rightArm.addBox(-3.0F, 1.0F, -2.0F, 4, 8, 4.0F, size+0.01f);
        this.rightArm.setPos(-2.5F, 2.0F + yOffsetIn, -0.005F);
        this.leftArm = new ModelPart(this, 40, 16);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, 1.0F, -2.0F, 4, 8, 4.0F, size+0.01f);
        this.leftArm.setPos(2.5F, 2.0F + yOffsetIn, -0.005F);

        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, size);
        this.head.setPos(0.0F, 0.0F + yOffsetIn, 0.0F);

        //mod support. I'm not using this
        this.hat = new ModelPart(this, 32, 0);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, size + 0.5F);
        this.hat.setPos(0.0F, 0.0F + yOffsetIn, 0.0F);

        this.leftLeg = new ModelPart(this, 0, 16);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, size+legOffset);
        this.leftLeg.setPos(0F, 12.0F + yOffsetIn, 0.0F);
        this.rightLeg = new ModelPart(this, 0, 0);

        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, size );
        this.body.setPos(0.0F, 0.0F + yOffsetIn, 0.0F);

    }

*/
    public void rotateModelX(ModelPart model, float nrx, float nry, float nrz, float angle){
        Vec3 oldrot = new Vec3(model.x, model.y, model.z);
        Vec3 actualrot = new Vec3(nrx, nry, nrz);

        Vec3 newrot = actualrot.add(oldrot.subtract(actualrot).xRot(-angle));

        model.setPos((float) newrot.x(), (float) newrot.y(), (float) newrot.z());
        model.xRot = angle;
    }
    public void rotateModelY(ModelPart model, float nrx, float nry, float nrz, float angle, int mult){
        Vec3 oldrot = new Vec3(model.x, model.y, model.z);
        Vec3 actualrot = new Vec3(nrx, nry, nrz);

        Vec3 newrot = actualrot.add(oldrot.subtract(actualrot).xRot(-angle));

        model.setPos((float) newrot.x(), (float) newrot.y(), (float) newrot.z());
        model.yRot = angle*mult;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green,
                       float blue, float alpha) {
        matrixStackIn.pushPose();

        this.standPlate.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        this.head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.rightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.leftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        this.hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        matrixStackIn.popPose();
    }

    //TODO: this is horrible
    @Override
    public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
        float phase = Mth.lerp(partialTick,entityIn.prevShakeAmount,entityIn.shakeAmount);
        float swing = Mth.lerp(partialTick,entityIn.prevLimbSwing,entityIn.animationPosition);
        float shake = Math.min((float) (swing * Configs.cached.ANIMATION_INTENSITY), 40f);

        if (shake > 0) {
            this.r = (float) -(Mth.sin(phase) * Math.PI / 100f * shake);
            this.r2 = (float) (Mth.sin(phase) * Math.PI / 20f * Math.min(shake,1));
        }
        else{
            this.r = 0;
            this.r2 = 0;
        }

    }

    @Override
    public void setupAnim(TargetDummyEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch) {


        // un-rotate the stand plate, so it's aligned to the block grid
        this.standPlate.yRot = -(entityIn).getYRot() / (180F / (float) Math.PI);


        float n = 1.5f;

        //------new---------

        float yOffsetIn = -1;

        float xangle = r/2;


        this.leftLeg.setPos(0, 12.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.leftLeg, 0, 24 + yOffsetIn, 0, xangle);
        //for mod support
        this.rightLeg.setPos(0, 12.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.rightLeg, 0.01f, 24 + yOffsetIn+0.01f, 0.01f, xangle);

        this.body.setPos(0.0F, 0.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.body, 0, 24 + yOffsetIn, 0, xangle);


        this.rightArm.setPos(-2.5F, 2.0F + yOffsetIn, -0.005F);
        this.rotateModelY(this.rightArm, 0, 24 + yOffsetIn, 0, xangle, -1);

        this.leftArm.setPos(2.5F, 2.0F + yOffsetIn, -0.005F);
        this.rotateModelY(this.leftArm, 0, 24 + yOffsetIn, 0, xangle, 1);



        this.head.setPos(0.0F, 0.0F + yOffsetIn, 0.0F);
        this.rotateModelX(this.head, 0, 24 + yOffsetIn, 0, xangle);
        //mod support
        this.hat.copyFrom(this.head);



        this.head.xRot = -r; //-r
        this.head.zRot = r2; //r2


        //rotate arms up
        this.rightArm.zRot = (float) Math.PI / 2f;
        this.leftArm.zRot = -(float) Math.PI / 2f;
        //swing arm
        this.rightArm.xRot = r * n;
        this.leftArm.xRot = r * n;

    }


}