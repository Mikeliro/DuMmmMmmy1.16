
package net.mehvahdjukaar.dummmmmmy.common;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Collections;
import java.util.List;

public class Configs {

	public static void reloadConfigsEvent(ModConfig.ModConfigEvent event) {
		if(event.getConfig().getSpec() == CLIENT_CONFIG)
			cached.refresh();
	}


	public static ForgeConfigSpec CLIENT_CONFIG;

	public static ForgeConfigSpec.DoubleValue ANIMATION_INTENSITY;
	public static ForgeConfigSpec.BooleanValue SHOW_HEARTHS;
	public static ForgeConfigSpec.BooleanValue DYNAMIC_DPS;
	public static ForgeConfigSpec.IntValue SKIN;

	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_GENERIC;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_CRIT;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_DRAGON;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_WITHER;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_EXPLOSION;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_IND_MAGIC;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_TRIDENT;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_MAGIC;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_FIRE;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_LIGHTNING;
	public static ForgeConfigSpec.ConfigValue<String> DAMAGE_CACTUS;

	public static ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.comment("lots of cosmetic stuff in here").push("visuals");
		ANIMATION_INTENSITY = builder.comment("How much the dummy swings in degrees with respect to the damage dealt. default=0.75").defineInRange("animationIntensity", 0.75, 0.0, 2.0);
		SHOW_HEARTHS = builder.comment("Show hearths instead of damage dealt? (1 hearth = two damage)").define("showHearths", false);
		DYNAMIC_DPS = builder.comment("Does dps message update dynamically or will it only appear after each parse? ").define("dynamicDPS", true);

		//TODO: add color configs
		builder.push("skins");

		SKIN = builder.comment("possible skins: 0 = default, 1 = original, 2 = my attempt").defineInRange("texture", 0, 0, 2);

		builder.push("damage_number_colors").comment("hex color for various damage sources");
		DAMAGE_GENERIC = builder.define("genetic","0xffffff");
		DAMAGE_CRIT = builder.define("crit","0xff0000");
		DAMAGE_DRAGON = builder.define("dragon_breath","0xFF00FF");
		DAMAGE_WITHER = builder.define("wither","0x666666");
		DAMAGE_EXPLOSION = builder.define("explosion","0xFFCC33");
		DAMAGE_IND_MAGIC = builder.define("indirect_magic","0x990033");
		DAMAGE_MAGIC = builder.define("magic","0x3399FF");
		DAMAGE_TRIDENT = builder.define("trident","0x00FFCC");
		DAMAGE_FIRE = builder.define("fire","0xFF9900");
		DAMAGE_LIGHTNING = builder.define("lightning","0xFFFF00");
		DAMAGE_CACTUS = builder.define("cactus","0x006600");

		builder.pop();

		builder.push("scarecrow").comment("equip a dummy with a pumpkin to make hit act as a scarecrow");

		WHITELIST = builder.comment("all animal entities will be scared. add here additional ones that are not included").defineList("mobs_whitelist", Collections.singletonList(""), s->true);
		BLACKLIST = builder.comment("animal entities that will not be scared").defineList("mobs_blacklist", Collections.singletonList(""), s->true);

		builder.pop();

		builder.pop();

		CLIENT_CONFIG = builder.build();
	}

	private static int parseHex(String s){
		int hex = 0xffffff;
		try{
			hex = Integer.parseInt(s.replace("0x",""), 16);
		}catch(Exception e){
			DummmmmmyMod.LOGGER.warn("failed to parse damage source color from config");
		}
		return hex;
	}


	public static class cached{
		public static double ANIMATION_INTENSITY;
		public static boolean SHOW_HEARTHS;
		public static boolean DYNAMIC_DPS;
		public static int SKIN;

		public static int DAMAGE_GENERIC;
		public static int DAMAGE_CRIT;
		public static int DAMAGE_DRAGON;
		public static int DAMAGE_WITHER;
		public static int DAMAGE_EXPLOSION;
		public static int DAMAGE_IND_MAGIC;
		public static int DAMAGE_TRIDENT;
		public static int DAMAGE_MAGIC;
		public static int DAMAGE_FIRE;
		public static int DAMAGE_LIGHTNING;
		public static int DAMAGE_CACTUS;

		public static List<? extends String> WHITELIST;
		public static List<? extends String> BLACKLIST;

		public static void refresh(){
			ANIMATION_INTENSITY = Configs.ANIMATION_INTENSITY.get();
			SHOW_HEARTHS = Configs.SHOW_HEARTHS.get();
			DYNAMIC_DPS = Configs.DYNAMIC_DPS.get();
			SKIN = Configs.SKIN.get();

			DAMAGE_GENERIC = parseHex(Configs.DAMAGE_GENERIC.get());
			DAMAGE_CRIT = parseHex(Configs.DAMAGE_CRIT.get());
			DAMAGE_DRAGON = parseHex(Configs.DAMAGE_DRAGON.get());
			DAMAGE_WITHER = parseHex(Configs.DAMAGE_WITHER.get());
			DAMAGE_EXPLOSION = parseHex(Configs.DAMAGE_EXPLOSION.get());
			DAMAGE_IND_MAGIC = parseHex(Configs.DAMAGE_IND_MAGIC.get());
			DAMAGE_TRIDENT = parseHex(Configs.DAMAGE_TRIDENT.get());
			DAMAGE_MAGIC = parseHex(Configs.DAMAGE_MAGIC.get());
			DAMAGE_FIRE = parseHex(Configs.DAMAGE_FIRE.get());
			DAMAGE_LIGHTNING = parseHex(Configs.DAMAGE_LIGHTNING.get());
			DAMAGE_CACTUS = parseHex(Configs.DAMAGE_CACTUS.get());

			WHITELIST = Configs.WHITELIST.get();
			BLACKLIST = Configs.BLACKLIST.get();

		}
	}

}
