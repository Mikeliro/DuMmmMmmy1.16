package net.mehvahdjukaar.dummmmmmy.setup;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.client.NumberRenderer;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyModel;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {

    private static ModelLayerLocation loc(String name){
        return new ModelLayerLocation(DummmmmmyMod.res(name), name);
    }
    public static ModelLayerLocation DUMMY_BODY = loc("dummy");
    public static ModelLayerLocation DUMMY_ARMOR_OUTER = loc("dummy_armor_outer");
    public static ModelLayerLocation DUMMY_ARMOR_INNER = loc("dummy_armor_inner");


    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DUMMY_BODY, ()->TargetDummyModel.createLayer(0));
        event.registerLayerDefinition(DUMMY_ARMOR_OUTER, ()->TargetDummyModel.createArmorLayer(1));
        event.registerLayerDefinition(DUMMY_ARMOR_INNER, ()->TargetDummyModel.createArmorLayer(0.5f));
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Registry.TARGET_DUMMY.get(), TargetDummyRenderer::new);
        event.registerEntityRenderer(Registry.DUMMY_NUMBER.get(), NumberRenderer::new);
    }
}