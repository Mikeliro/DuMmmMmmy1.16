
//Authors: bonusboni, mehvahdjukaar
package net.mehvahdjukaar.dummmmmmy;

import net.mehvahdjukaar.dummmmmmy.client.LayerDummyArmor;
import net.mehvahdjukaar.dummmmmmy.client.NumberRenderer;
import net.mehvahdjukaar.dummmmmmy.client.TargetDummyRenderer;
import net.mehvahdjukaar.dummmmmmy.dispenser.DispenserBehavior;
import net.mehvahdjukaar.dummmmmmy.entity.DummyNumberEntity;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.mehvahdjukaar.dummmmmmy.item.TargetDummyItem;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("dummmmmmy")
public class DummmmmmyMod {

	public DummmmmmyMod() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		//don't know what I'm doing here :/
		new DetectCriticalHit();
		//MinecraftForge.EVENT_BUS.register(DetectCriticalHit.class);
	}

	private void init(FMLCommonSetupEvent event) {
		GlobalEntityTypeAttributes.put(TargetDummyEntity.TARGET_DUMMY, TargetDummyEntity.DummyMob.setCustomAttributes().create());
		GlobalEntityTypeAttributes.put(DummyNumberEntity.DUMMY_NUMBER, DummyNumberEntity.NumberEntity.setCustomAttributes().create());
		DispenserBehavior.registerBehaviors();
		Network.Networking.registerMessages();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Configs.CLIENT_CONFIG);
	}


	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(TargetDummyEntity.TARGET_DUMMY, renderManager -> {
			BipedRenderer customRender = new BipedRenderer(renderManager, new TargetDummyRenderer(), 0f) {
				@Override
				public ResourceLocation getEntityTexture(Entity entity) {
					return new ResourceLocation(Config.Configs.getSkin(entity));
				}
			};
			customRender.addLayer(new LayerDummyArmor(customRender, new BipedModel(0.5f), new BipedModel(1)));
			return customRender;
		});

		RenderingRegistry.registerEntityRenderingHandler(DummyNumberEntity.DUMMY_NUMBER, renderManager -> new NumberRenderer(renderManager));

	}



	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new TargetDummyItem());
	}


	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		event.getRegistry().register(TargetDummyEntity.TARGET_DUMMY);
		event.getRegistry().register(DummyNumberEntity.DUMMY_NUMBER);
	}
}