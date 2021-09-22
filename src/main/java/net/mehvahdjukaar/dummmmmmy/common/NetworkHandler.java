
package net.mehvahdjukaar.dummmmmmy.common;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyMod;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class NetworkHandler {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";

    public static int nextID() {
        return ID++;
    }


    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DummmmmmyMod.MOD_ID, "dummychannel"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), PacketDamageNumber.class, PacketDamageNumber::toBytes, PacketDamageNumber::new,
                PacketDamageNumber::handle);

        INSTANCE.registerMessage(nextID(), PacketSyncEquip.class, PacketSyncEquip::toBytes, PacketSyncEquip::new,
                PacketSyncEquip::handle);


        INSTANCE.registerMessage(nextID(), PacketChangeSkin.class, PacketChangeSkin::toBytes, PacketChangeSkin::new,
                PacketChangeSkin::handle);
    }


    private interface Message {
    }

    public static void sendToAllTracking(Entity entity, ServerWorld world, Message message) {
        world.getChunkSource().broadcast(entity, INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
    }

    public static class PacketDamageNumber implements Message {
        private final int entityID;
        private final float damage;
        private final float shake;

        public PacketDamageNumber(PacketBuffer buf) {
            this.entityID = buf.readInt();
            this.damage = buf.readFloat();
            this.shake = buf.readFloat();
        }

        public PacketDamageNumber(int id, float damage, float shakeAmount) {
            this.entityID = id;
            this.damage = damage;
            this.shake = shakeAmount;
        }

        public void toBytes(PacketBuffer buf) {
            buf.writeInt(this.entityID);
            buf.writeFloat(this.damage);
            buf.writeFloat(this.shake);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityID);
                if (entity instanceof TargetDummyEntity) {
                    TargetDummyEntity dummy = (TargetDummyEntity) entity;
                    dummy.animationPosition = shake;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }


    public static class PacketSyncEquip implements Message {
        private final int entityID;
        private final int slotId;
        private final ItemStack itemstack;

        public PacketSyncEquip(PacketBuffer buf) {
            this.entityID = buf.readInt();
            this.slotId = buf.readInt();

            this.itemstack = buf.readItem();
        }

        public PacketSyncEquip(int entityId, int slotId, @Nonnull ItemStack itemstack) {
            this.entityID = entityId;
            this.slotId = slotId;
            this.itemstack = itemstack.copy();
        }

        public void toBytes(PacketBuffer buf) {
            buf.writeInt(this.entityID);
            buf.writeInt(slotId);
            buf.writeItem(itemstack);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityID);
                if (entity instanceof TargetDummyEntity) {
                    TargetDummyEntity dummy = (TargetDummyEntity) entity;
                    dummy.setItemSlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, this.slotId), this.itemstack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }


    public static class PacketChangeSkin implements Message {
        private final int entityID;
        private final boolean skin;

        public PacketChangeSkin(PacketBuffer buf) {
            this.entityID = buf.readInt();
            this.skin = buf.readBoolean();
        }

        public PacketChangeSkin(int entityId, boolean skin) {
            this.entityID = entityId;
            this.skin = skin;
        }

        public void toBytes(PacketBuffer buf) {
            buf.writeInt(this.entityID);
            buf.writeBoolean(this.skin);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Entity entity = Minecraft.getInstance().level.getEntity(this.entityID);
                if (entity instanceof TargetDummyEntity) {
                    TargetDummyEntity dummy = (TargetDummyEntity) entity;
                    dummy.sheared = this.skin;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }


}
