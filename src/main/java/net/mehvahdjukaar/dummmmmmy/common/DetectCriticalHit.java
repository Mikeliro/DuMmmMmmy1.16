package net.mehvahdjukaar.dummmmmmy.common;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus= Mod.EventBusSubscriber.Bus.FORGE)
public class DetectCriticalHit {

    @SubscribeEvent
    public static void onEntityCriticalHit(CriticalHitEvent event) {
        if (event != null && event.getEntity() != null) {
            Entity target = event.getTarget();
            if (event.getDamageModifier() == 1.5 && target instanceof TargetDummyEntity) {
                TargetDummyEntity dummy = (TargetDummyEntity) target;
                dummy.critical = true;
            }
        }
    }
}
