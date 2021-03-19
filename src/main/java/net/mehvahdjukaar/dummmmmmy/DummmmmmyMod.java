
//Authors: bonusboni, mehvahdjukaar
package net.mehvahdjukaar.dummmmmmy;

import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.setup.ClientSetup;
import net.mehvahdjukaar.dummmmmmy.setup.ModSetup;
import net.mehvahdjukaar.dummmmmmy.setup.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DummmmmmyMod.MOD_ID)
public class DummmmmmyMod {

	public static final String MOD_ID = "dummmmmmy";

	public static final Logger LOGGER = LogManager.getLogger();

	public DummmmmmyMod() {

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_CONFIG);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Configs::reloadConfigsEvent);

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();


		Registry.init(bus);

		bus.addListener(ModSetup::init);

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(ClientSetup::init));

	}

}