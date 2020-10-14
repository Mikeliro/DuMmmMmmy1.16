package net.mehvahdjukaar.dummmmmmy.procedures;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import net.minecraft.entity.Entity;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraftforge.common.MinecraftForge;

@DummmmmmyModElements.ModElement.Tag
public class DetectCriticalHitProcedure extends DummmmmmyModElements.ModElement {
	public DetectCriticalHitProcedure(DummmmmmyModElements instance) {
		super(instance, 4);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onEntityCriticalHit(CriticalHitEvent event) {
		if (event != null && event.getEntity() != null) {
			Entity target = event.getTarget();
			if (event.getDamageModifier() == 1.5 && target instanceof TargetDummyEntity.DummyMob) {
				TargetDummyEntity.DummyMob dummy = (TargetDummyEntity.DummyMob) target;

				dummy.hitByCritical();
			}
		}
	}
}
