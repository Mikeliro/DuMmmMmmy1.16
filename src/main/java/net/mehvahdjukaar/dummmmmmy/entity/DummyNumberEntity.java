
package net.mehvahdjukaar.dummmmmmy.entity;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.IPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.block.BlockState;

import net.mehvahdjukaar.dummmmmmy.DummmmmmyModElements;

import net.mehvahdjukaar.dummmmmmy.Config;

import java.util.Random;

import java.text.DecimalFormat;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class DummyNumberEntity{
	public static EntityType DUMMY_NUMBER = (EntityType.Builder.<CustomEntity>create(CustomEntity::new, EntityClassification.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(CustomEntity::new).size(0.6f, 1.8f)).build("dummy_number")
			.setRegistryName("dummy_number");
	public DummyNumberEntity(DummmmmmyModElements instance) {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
	}



	public static class CustomEntity extends Entity implements IEntityAdditionalSpawnData {
		protected static final int MAXAGE=40;
		public int age;
		public float number = 69420;
		protected float speed = 1;
		public float dy = 0;
		public float prevDy = 0;
		public int color = 0xffffffff;
		public float dx = 0;
		public float prevDx = 0;
		public float speedx = 0;
		public float fadeout=-1;
		private int type =-1; //used for location in array
		protected final Random rand = new Random();
		public  List<Float> list = new ArrayList<>(Arrays.asList(0f,-0.25f,0.12f,-0.12f,0.25f));
		public CustomEntity(FMLPlayMessages.SpawnEntity packet, World world) {
			this(DUMMY_NUMBER, world);
		}

		public CustomEntity(EntityType<CustomEntity> type, World world) {
			super(type, world);
		}

		public CustomEntity(float number, int color, World world) {
			super(DUMMY_NUMBER, world);
			this.number = number;
			this.color = color;
		}
		public CustomEntity(float number, int color, int type, World world) {
			this(number,  color,  world);
			this.type=type;
		}


		@Override
		public AxisAlignedBB getBoundingBox() {
			return new AxisAlignedBB(new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ()));
		}

		@Override
		public void writeSpawnData(PacketBuffer buffer) {
			buffer.writeFloat(this.number);
			buffer.writeInt(this.color);
			buffer.writeInt(this.type);
		}

		@Override
		public void readSpawnData(PacketBuffer additionalData) {
			this.number = additionalData.readFloat();
			this.color = additionalData.readInt();
			int i = additionalData.readInt();
            if(i!=-1){
            	this.speedx=list.get(i%list.size());
            }
            else{
				//this.speedx = (this.rand.nextFloat() - 0.5f) / 2f;
				this.speedx = list.get(this.rand.nextInt(list.size()));
            }
		}

		public void readAdditional(CompoundNBT compound) {
			// super.readAdditional(compound);
			this.number = compound.getFloat("number");
			this.color = compound.getInt("color");
			this.age = compound.getInt("age");
		}

		public void writeAdditional(CompoundNBT compound) {
			// super.writeAdditional(compound);
			compound.putFloat("number", this.number);
			compound.putInt("color", this.color);
			compound.putInt("age", this.age);
		}

		protected void registerData() {
			// this.getDataManager().register(ITEM, ItemStack.EMPTY);
		}

		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}

		@Override
		public void tick() {
			if (this.age++ > MAXAGE || this.getPosY() < -64.0D) {
				this.remove();
			}

			float lenght=6;
			this.fadeout = this.age>(MAXAGE-lenght)? ((float)MAXAGE-this.age)/lenght : 1;

			
			// this.forceSetPosition(this.getPosX(), this.getPosY() + (this.speed / 2),
			// this.getPosZ());
			this.prevDy = this.dy;
			this.dy += this.speed;
			this.prevDx = this.dx;
			this.dx += this.speedx;
			// this.speed / 500d;
			//spawn numbers in a sort of elliple centered on his torso
			if (Math.sqrt(Math.pow(this.dx*1.5,2) + Math.pow(this.dy-1,2)) < 1.9-1) {
 
				speed = speed / 2;
			} else {
				speed = 0;
				speedx = 0;
			}
		}

		public void reSet(float number) {
			this.number = number;
			this.age = 0;
		}

		public float getNumber() {
			return this.number;
		}


		@Override
		public boolean onLivingFall(float l, float d) {
			return false;
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			return false;
		}

		@Override
		public boolean isPushedByWater() {
			return false;
		}

		@Override
		public boolean canBeCollidedWith() {
			return false;
		}

		@Override
		protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
		}

		@Override
		public void setNoGravity(boolean ignored) {
			super.setNoGravity(true);
		}
	}
}
