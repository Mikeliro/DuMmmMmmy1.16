
package net.mehvahdjukaar.dummmmmmy;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class Network {



	public static class myMessage {}

	public static class PacketDamageNumber extends myMessage {
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
				Entity entity = Minecraft.getInstance().world.getEntityByID(this.entityID);
				if (entity != null && entity instanceof TargetDummyEntity.DummyMob) {
					TargetDummyEntity.DummyMob dummy = (TargetDummyEntity.DummyMob) entity;
					dummy.setShake(this.shake);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}



	public static class PacketSyncEquip extends myMessage {
		private final int entityID;
		private final int slotId;
		private final ItemStack itemstack;
		public PacketSyncEquip(PacketBuffer buf) {
			this.entityID = buf.readInt();
		    this.slotId = buf.readInt();

		    this.itemstack = buf.readItemStack();
		}

		public PacketSyncEquip(int entityId, int slotId, @Nonnull ItemStack itemstack) {
			this.entityID = entityId;
		    this.slotId = slotId;
		    this.itemstack = itemstack.copy();
		}

		public void toBytes(PacketBuffer buf) {
			buf.writeInt(this.entityID);
		    buf.writeInt(slotId);  
		    buf.writeItemStack(itemstack);
		}

		public void handle(Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Entity entity = Minecraft.getInstance().world.getEntityByID(this.entityID);
				if (entity != null && entity instanceof TargetDummyEntity.DummyMob) {
					TargetDummyEntity.DummyMob dummy = (TargetDummyEntity.DummyMob) entity;
					dummy.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, this.slotId), this.itemstack);
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}


	public static class PacketChangeSkin extends myMessage {
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
				Entity entity = Minecraft.getInstance().world.getEntityByID(this.entityID);
				if (entity != null && entity instanceof TargetDummyEntity.DummyMob) {
					TargetDummyEntity.DummyMob dummy = (TargetDummyEntity.DummyMob) entity;
					dummy.sheared=this.skin;
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}





	public static class Networking {
		public static SimpleChannel INSTANCE;
		private static int ID = 0;
		public static int nextID() {
			return ID++;
		}

		public static void registerMessages() {
			INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation("dummmmmmy:mychannel"), () -> "1.0", s -> true, s -> true);
			
			INSTANCE.registerMessage(nextID(), PacketDamageNumber.class, PacketDamageNumber::toBytes, PacketDamageNumber::new,
					PacketDamageNumber::handle);


			INSTANCE.registerMessage(nextID(), PacketSyncEquip.class, PacketSyncEquip::toBytes, PacketSyncEquip::new,
					PacketSyncEquip::handle);

			
			INSTANCE.registerMessage(nextID(), PacketChangeSkin.class, PacketChangeSkin::toBytes, PacketChangeSkin::new,
					PacketChangeSkin::handle);

		}
	}

	public static void sendToAllTracking(World world, Entity entityIn, myMessage message){
		if (world instanceof ServerWorld){
		((ServerWorld)world).getChunkProvider().sendToAllTracking(entityIn, Networking.INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT));
		}

	}
	
}
