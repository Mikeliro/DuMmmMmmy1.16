package net.mehvahdjukaar.dummmmmmy.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;

public class TargetDummyRenderer extends BipedRenderer<TargetDummyEntity, TargetDummyModel<TargetDummyEntity>> {

    public TargetDummyRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new TargetDummyModel<>(), 0);
        this.addLayer(new LayerDummyArmor<>(this, new TargetDummyModel<>(EquipmentSlotType.LEGS), new TargetDummyModel<>(EquipmentSlotType.CHEST)));
    }

    private static final ResourceLocation TEXT_0 = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy.png");
    private static final ResourceLocation TEXT_1 = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy_1.png");
    private static final ResourceLocation TEXT_2 = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy_2.png");
    private static final ResourceLocation TEXT_0_S = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy_h.png");
    private static final ResourceLocation TEXT_1_S = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy_1_h.png");
    private static final ResourceLocation TEXT_2_S = new ResourceLocation(DummmmmmyMod.MOD_ID+":textures/dummy_2_h.png");
    @Override
    public ResourceLocation getEntityTexture(TargetDummyEntity entity) {
        boolean s = entity.sheared;
        switch (Configs.cached.SKIN){
            default:
            case 0:
                return s?TEXT_0_S:TEXT_0;
            case 1:
                return s?TEXT_1_S:TEXT_1;
            case 2:
                return s?TEXT_2_S:TEXT_2;
        }
    }
}
