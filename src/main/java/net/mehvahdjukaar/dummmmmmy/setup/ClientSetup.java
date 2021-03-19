package net.mehvahdjukaar.dummmmmmy.setup;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.client.LayerDummyArmor;
import net.mehvahdjukaar.dummmmmmy.client.NumberRenderer;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyModel;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyRenderer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {



        RenderingRegistry.registerEntityRenderingHandler(Registry.TARGET_DUMMY.get(), TargetDummyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(Registry.DUMMY_NUMBER.get(), NumberRenderer::new);

    }
}
