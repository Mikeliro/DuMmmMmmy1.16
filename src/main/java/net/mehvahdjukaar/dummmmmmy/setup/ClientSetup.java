package net.mehvahdjukaar.dummmmmmy.setup;

import net.mehvahdjukaar.dummmmmmy.client.NumberRenderer;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {



        RenderingRegistry.registerEntityRenderingHandler(Registry.TARGET_DUMMY.get(), TargetDummyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(Registry.DUMMY_NUMBER.get(), NumberRenderer::new);

    }
}
