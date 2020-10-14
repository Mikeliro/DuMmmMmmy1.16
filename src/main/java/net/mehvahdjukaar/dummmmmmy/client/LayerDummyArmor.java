package net.mehvahdjukaar.dummmmmmy.client;

import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;

public class LayerDummyArmor extends BipedArmorLayer<LivingEntity, BipedModel<LivingEntity>, BipedModel<LivingEntity>> {
    public LayerDummyArmor(IEntityRenderer<LivingEntity, BipedModel<LivingEntity>> p_i50936_1_, BipedModel<LivingEntity> p_i50936_2_,
                           BipedModel<LivingEntity> p_i50936_3_) {
        super(p_i50936_1_, new TargetDummyRenderer(0.5F, 0, 64, 32, -0.01f), new TargetDummyRenderer(1.0F, 0, 64, 32, -0.01f));
    }


    @Override
    protected void setModelSlotVisible(BipedModel<LivingEntity> modelIn, EquipmentSlotType slotIn) {
        modelIn.setVisible(false);
        ((TargetDummyRenderer) modelIn).standPlate.showModel = false;
        ((TargetDummyRenderer) modelIn).newhead.showModel = false;
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
                modelIn.bipedRightLeg.showModel = true;
                modelIn.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                modelIn.bipedRightLeg.showModel = false;
                modelIn.bipedLeftLeg.showModel = true;
                modelIn.bipedBody.showModel = false;
        }
    }
}