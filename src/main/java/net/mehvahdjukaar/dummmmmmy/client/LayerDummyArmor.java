package net.mehvahdjukaar.dummmmmmy.client;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;

public class LayerDummyArmor<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A >{
    /*public LayerDummyArmor(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50936_1_, BipedModel<LivingEntity> p_i50936_2_,
                           BipedModel<LivingEntity> p_i50936_3_) {
        super(p_i50936_1_, new TargetDummyRenderer(0.5F, 0, 64, 32, -0.01f), new TargetDummyRenderer(1.0F, 0, 64, 32, -0.01f));
    }*/
    public LayerDummyArmor(IEntityRenderer<T, M> renderer, A modelLegs, A modelChest) {

        //super(p_i50936_1_, (A) new TargetDummyModel<T>(0.5F, 0, 64, 32, -0.01f), (A) new TargetDummyModel<T>(1.0F, 0, 64, 32, -0.01f));
        super(renderer,modelLegs,modelChest);

    }


    @Override
    protected void setModelSlotVisible(A modelIn, EquipmentSlotType slotIn) {
        modelIn.setVisible(false);
        //boolean flag = modelIn instanceof  TargetDummyModel;
        modelIn.bipedRightLeg.showModel = false;
        switch (slotIn) {
            case HEAD:
                modelIn.bipedHead.showModel = true;
                break;
            case CHEST:
                modelIn.bipedBody.showModel = true;
                modelIn.bipedRightArm.showModel = true;
                modelIn.bipedLeftArm.showModel = true;
                break;
            case LEGS:
                modelIn.bipedBody.showModel = true;
                modelIn.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                modelIn.bipedLeftLeg.showModel = true;
                modelIn.bipedBody.showModel = false;
        }
    }

}