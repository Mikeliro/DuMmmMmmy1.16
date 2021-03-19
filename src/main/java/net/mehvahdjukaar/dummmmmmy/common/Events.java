package net.mehvahdjukaar.dummmmmmy.common;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.mehvahdjukaar.dummmmmmy.setup.Registry;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus= Mod.EventBusSubscriber.Bus.FORGE)
public class Events {

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

    public static boolean isScared(Entity entity){
        return entity instanceof AnimalEntity;
    }

    public static boolean isScarecrowInRange(Entity entity, World world){
        return !world.getEntitiesWithinAABB(Registry.TARGET_DUMMY.get(), entity.getBoundingBox().grow(10),
                TargetDummyEntity::isScarecrow).isEmpty();
    }

    //prevents them from spawning
    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if(!(event.getWorld()instanceof World))return;
        World world = event.getEntity().world;

        Entity entity = event.getEntity();
        if(isScared(entity)){
            if(isScarecrowInRange(entity, world)) event.setResult(Event.Result.DENY);
        }
    }

    //add goal
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if(event.getWorld() == null)return;
        Entity e = event.getEntity();
        if (e instanceof CreatureEntity && isScared(e)) {

            CreatureEntity mob = (CreatureEntity) e;
            mob.goalSelector.addGoal(3, new AvoidEntityGoal<>(mob, TargetDummyEntity.class,
                    12.0F, 1.0D, 1.3D, d -> ((TargetDummyEntity) d).isScarecrow()));

        }
    }
}
