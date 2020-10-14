
package net.mehvahdjukaar.dummmmmmy;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public class Config {

	@EventBusSubscriber
	public  static class Configs{
		public static ForgeConfigSpec CLIENT_CONFIG;
		public static ForgeConfigSpec.DoubleValue ANIMATION_INTENSITY;
		public static ForgeConfigSpec.BooleanValue SHOW_HEARTHS;
		public static ForgeConfigSpec.BooleanValue DYNAMIC_DPS;
		public static ForgeConfigSpec.IntValue SKIN;
		static {
	
	        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	
	        CLIENT_BUILDER.comment("lots of cosmetic stuff in here").push("visuals");
	        ANIMATION_INTENSITY = CLIENT_BUILDER.comment("How much the dummy swings in degrees with respect to the damage dealt. default=0.75").defineInRange("animationIntensity", 0.75, 0.0, 2.0);
	       	SHOW_HEARTHS = CLIENT_BUILDER.comment("Show hearths instead of damage dealt? (1 hearth = two damage)").define("showHearths", false);
	        DYNAMIC_DPS = CLIENT_BUILDER.comment("Does dps message update dynamically or will it only appear after each parse? ").define("dynamicDPS", true);
	        CLIENT_BUILDER.push("skins");

	        SKIN = CLIENT_BUILDER.comment("possible skins: 0 = default, 1 = original, 2 = my attempt").defineInRange("texture", 0, 0, 2);
	        
	        CLIENT_BUILDER.pop();

	        CLIENT_CONFIG = CLIENT_BUILDER.build();
    	}

		public static String getSkin(Entity entity){
			boolean flag = false;
			if(entity instanceof TargetDummyEntity.DummyMob){
				flag = ((TargetDummyEntity.DummyMob)entity).sheared;
			}
			if(!flag){
				switch (SKIN.get()){
					default:
					case 0:
					return "dummmmmmy:textures/dummy.png";
					case 1:
					return "dummmmmmy:textures/dummy_1.png";
					case 2:
					return "dummmmmmy:textures/dummy_2.png";
				}
			}
			else{
				switch (SKIN.get()){
					default:
					case 0:
					return "dummmmmmy:textures/dummy_h.png";
					case 1:
					return "dummmmmmy:textures/dummy_1_h.png";
					case 2:
					return "dummmmmmy:textures/dummy_2_h.png";
				}
			}
		}
	}

}
