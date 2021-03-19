package net.mehvahdjukaar.dummmmmmy.setup;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.entity.DummyNumberEntity;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.mehvahdjukaar.dummmmmmy.item.TargetDummyItem;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class Registry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DummmmmmyMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, DummmmmmyMod.MOD_ID);

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }


    public static final String TARGET_DUMMY_NAME = "target_dummy";
    public static final RegistryObject<EntityType<TargetDummyEntity>> TARGET_DUMMY = ENTITIES.register(TARGET_DUMMY_NAME, ()->(
            EntityType.Builder.<TargetDummyEntity>create(TargetDummyEntity::new, EntityClassification.MISC)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setCustomClientFactory(TargetDummyEntity::new)
                    //.setUpdateInterval(3)
                    .size(0.6f, 2f))
            .build(TARGET_DUMMY_NAME));

    public static final String DUMMY_NUMBER_NAME = "dummy_number";
    public static final RegistryObject<EntityType<DummyNumberEntity>> DUMMY_NUMBER = ENTITIES.register(DUMMY_NUMBER_NAME, ()->(
            EntityType.Builder.<DummyNumberEntity>create(DummyNumberEntity::new, EntityClassification.MISC)
                    .setShouldReceiveVelocityUpdates(true)
                    .setTrackingRange(64)
                    .setCustomClientFactory(DummyNumberEntity::new)
                    //.setUpdateInterval(3)
                    .size(0.6f, 1.8f))
            .build(DUMMY_NUMBER_NAME));

    public static final String DUMMY_ITEM_NAME = "target_dummy_placer";
    public static final RegistryObject<Item> DUMMY_ITEM = ITEMS.register(DUMMY_ITEM_NAME, ()-> new TargetDummyItem(
            new Item.Properties().group(ItemGroup.COMBAT).maxStackSize(16)));


}