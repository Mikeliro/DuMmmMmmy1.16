package net.mehvahdjukaar.dummmmmmy;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DetectCriticalHit {
    public DetectCriticalHit(){
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
