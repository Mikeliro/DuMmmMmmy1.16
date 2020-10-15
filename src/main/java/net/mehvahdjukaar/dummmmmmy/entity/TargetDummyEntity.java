
package net.mehvahdjukaar.dummmmmmy.entity;

import net.mehvahdjukaar.dummmmmmy.Config;
import net.mehvahdjukaar.dummmmmmy.Network;
import net.mehvahdjukaar.dummmmmmy.item.TargetDummyItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TargetDummyEntity{
	public static EntityType TARGET_DUMMY = (EntityType.Builder.<DummyMob>create(DummyMob::new, EntityClassification.MISC).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(DummyMob::new).size(0.6f, 2f)).build("target_dummy")
			.setRegistryName("target_dummy");



	public static class DummyMob extends MobEntity implements IEntityAdditionalSpawnData {


		public float shake;
		public float shakeAnimation; // used to have an independent start for the animation, otherwise the phase of
										// the animation depends ont he damage dealt
										// used to calculate the whole damage in one tick, in case there are multiple
										// sources
		public float lastDamage;
		public int lastDamageTick;
		public int firstDamageTick; // indicates when we started taking damage and if != 0 it also means that we are
									// currently recording damage taken
		public float damageTaken;
		public boolean critical = false; //has just been hit by critical?
		public int mobType =0; //0=undefined, 1=undead, 2=water, 3= illager
		private final List<ServerPlayerEntity> currentlyAttacking = new ArrayList<>();//players to which display dps message
		private int mynumberpos =0;
		public boolean sheared;
		//public DummyNumberEntity.CustomEntity myLittleNumber;
		private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);

		public DummyMob(FMLPlayMessages.SpawnEntity packet, World world) {
			this(TARGET_DUMMY, world);
		}

		public DummyMob(EntityType<DummyMob> type, World world) {
			super(type, world);
			experienceValue = 0;
			setNoAI(true);
			Arrays.fill(this.inventoryArmorDropChances, 1.1f);
            this.sheared=false;
	
		}

		public DummyMob(World world) {
			this(TARGET_DUMMY, world);
		}

		public void setShake(float s) {
			this.shake = s;

		}
	
		public void hitByCritical(){
			this.critical=true;	
		}

		public void updateOnLoadClient() {
			float r = this.rotationYaw;
			this.prevRotationYawHead = this.rotationYawHead = r;
			this.prevRotationYaw = r;
			this.prevRenderYawOffset = this.renderYawOffset = r;
					
		}
	    public void updateOnLoadServer(){
	    	this.applyEquipmentModifiers();	
	    }

		// dress it up! :D

		@Override
		public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
			boolean invchanged = false;
			if (player.isSpectator()) {
				return ActionResultType.PASS;
			} else {
				ItemStack itemstack = player.getHeldItem(hand);
				EquipmentSlotType equipmentslottype = getSlotForItemStack(itemstack);
				// empty hand -> unequip
				if (itemstack.isEmpty() && hand == Hand.MAIN_HAND) {
					equipmentslottype = this.getClickedSlot(vec);
					if (this.hasItemInSlot(equipmentslottype)) {
						this.unequipArmor(player, equipmentslottype, itemstack, hand);
						invchanged=true;

					}
				}
				//equip banner
				else if(itemstack.getItem() instanceof BannerItem){	
					equipmentslottype = EquipmentSlotType.HEAD;		
					this.equipArmor(player, equipmentslottype, itemstack, hand);
					invchanged=true;


				}
				// armor item in hand -> equip/swap
				else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR) {
					this.equipArmor(player, equipmentslottype, itemstack, hand);
					invchanged=true;
					
				}
				//remove sack
				else if (itemstack.getItem() instanceof ShearsItem){
					if(!this.sheared) {
						this.sheared = true;
						if (!this.world.isRemote) {
							Network.sendToAllTracking(this.world, this,
									new Network.PacketChangeSkin(this.getEntityId(), true));
						}
						return ActionResultType.SUCCESS;
					}

					
				}


				if(invchanged){
					Network.sendToAllTracking(this.world,this, new Network.PacketSyncEquip(this.getEntityId(), equipmentslottype.getIndex(), this.getItemStackFromSlot(equipmentslottype)));
					//this.applyEquipmentModifiers();
					return ActionResultType.SUCCESS;
				}

				
				return ActionResultType.PASS;
			}
		}

		private void unequipArmor(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Hand hand) {
			// set slot to stack which is empty stack
			ItemStack itemstack = this.getItemStackFromSlot(slot);
			ItemStack itemstack2 = itemstack.copy();
			player.setHeldItem(hand, itemstack2);
			this.setItemStackToSlot(slot, stack);

			//this.applyEquipmentModifiers();
			//now done here^
			this.getAttributeManager().removeModifiers(itemstack2.getAttributeModifiers(slot));
			//clear mob type
			if(slot==EquipmentSlotType.HEAD)this.mobType=0;
			
		}

		private void equipArmor(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Hand hand) {
			ItemStack itemstack = this.getItemStackFromSlot(slot);
			ItemStack itemstack2 = stack.copy();
			itemstack2.setCount(1);
			if (!player.isCreative()) {
				stack.shrink(1);
				if (!itemstack.isEmpty()) {
					// give item to player hand, inventory or drop it
					if (stack.isEmpty()) {
						player.setHeldItem(hand, itemstack);
					} else if (!player.inventory.addItemStackToInventory(itemstack)) {
						if (!getEntityWorld().isRemote) {
							this.entityDropItem(itemstack, 1.0f);
						}
					}
				}
			}
			this.playEquipSound(itemstack2);
			this.setItemStackToSlot(slot, itemstack2);
			
			//this.applyEquipmentModifiers();
			//now done here^
			this.getAttributeManager().reapplyModifiers(itemstack2.getAttributeModifiers(slot));
			//add mob type
			if(this.isUndeadSkull(itemstack2)){ 
				this.mobType=1;
			}
			else if(this.isTurtleHelmet(itemstack2)){
				this.mobType=2;
			}
			else if(ItemStack.areItemStacksEqual(itemstack2, Raid.createIllagerBanner())){
				this.mobType=3;
			}
			else this.mobType=0;
		}
  

		private boolean isTurtleHelmet(ItemStack itemstack){
			return (itemstack.getItem() == new ItemStack(Items.TURTLE_HELMET).getItem());
		}

		private boolean isUndeadSkull(ItemStack itemstack){
			Item i =itemstack.getItem();
			return i == new ItemStack(Blocks.ZOMBIE_HEAD).getItem() ||
					i == new ItemStack(Blocks.SKELETON_SKULL).getItem() ||
					i == new ItemStack(Blocks.WITHER_SKELETON_SKULL).getItem();
		}

		private EquipmentSlotType getClickedSlot(Vector3d p_190772_1_) {
			EquipmentSlotType equipmentslottype = EquipmentSlotType.MAINHAND;
			double d0 = p_190772_1_.y;
			EquipmentSlotType equipmentslottype1 = EquipmentSlotType.FEET;
			if (d0 >= 0.1D && d0 < 0.1D + ( 0.45D) && this.hasItemInSlot(equipmentslottype1)) {
				equipmentslottype = EquipmentSlotType.FEET;
			} else if (d0 >= 0.9D + ( 0.0D) && d0 < 0.9D + ( 0.7D) && this.hasItemInSlot(EquipmentSlotType.CHEST)) {
				equipmentslottype = EquipmentSlotType.CHEST;
			} else if (d0 >= 0.4D && d0 < 0.4D + ( 0.8D) && this.hasItemInSlot(EquipmentSlotType.LEGS)) {
				equipmentslottype = EquipmentSlotType.LEGS;
			} else if (d0 >= 1.6D && this.hasItemInSlot(EquipmentSlotType.HEAD)) {
				equipmentslottype = EquipmentSlotType.HEAD;
			}
			return equipmentslottype;
		}

		private void playBrokenSound() {
			this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK,
					this.getSoundCategory(), 1.0F, 1.0F);
		}

		private void playParticles() {
			if (this.world instanceof ServerWorld) {
				((ServerWorld) getEntityWorld()).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()),
						this.getPosX(), this.getPosYHeight(0.6666666666666666D), this.getPosZ(), 10,  (this.getWidth() / 4.0F),
						 (this.getHeight() / 4.0F),  (this.getWidth() / 4.0F), 0.05D);
			}
		}

		public void dropInventory() {
			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				if (slot.getSlotType() != EquipmentSlotType.Group.ARMOR) {
					continue;
				}
				ItemStack armor = getItemStackFromSlot(slot);
				if (!armor.isEmpty()) {
					this.entityDropItem(armor, 1.0f);
				}
			}
		}

		public void dismantle(boolean drops) {
			if (!getEntityWorld().isRemote) {
				if (drops) {
					dropInventory();
					this.entityDropItem(TargetDummyItem.DUMMY_ITEM, 1);
				}
				this.playBrokenSound();
				this.playParticles();
			}
			this.remove();
		}
			
		
		@Override
	   public void onKillCommand() {
	      this.dismantle(true);
	   }
	   
	   @Override
	   public boolean canBreatheUnderwater() {
	      return true;
	   }

		public int getColorFromDamageSource(DamageSource source){

			if(this.critical) return 0xff0000;
			if (source == DamageSource.DRAGON_BREATH) return 0xFF00FF;
			if (source ==DamageSource.WITHER)return  0x666666;
			if(source.damageType.equals("explosion")||source.damageType.equals("explosion.player"))return 0xFFCC33;
			if(source.damageType.equals("indirectMagic"))return 0x990033;
			if(source.damageType.equals("trident"))return 0x00FFCC;
			if(source==DamageSource.GENERIC)return 0xffffff;
			
			if(source == DamageSource.MAGIC){
				//would really like to detect poison damage but i don't think there's simple way

				return 0x3399FF;
			}
			if (source ==DamageSource.HOT_FLOOR||source ==DamageSource.LAVA||source ==DamageSource.ON_FIRE||source ==DamageSource.IN_FIRE) return 0xFF9900;
			if (source == DamageSource.LIGHTNING_BOLT) return 0xFFFF00;


			if (source ==DamageSource.CACTUS || source ==DamageSource.SWEET_BERRY_BUSH)return  0x006600;
			
			return 0xffffff;
		}


	   
		@Override
		public boolean attackEntityFrom(DamageSource source, float damage) {

      		if (this.isInvulnerableTo(source)) return false;


			//super.attackEntityFrom(source, damage);

			//not immune to void damage, immune to drown, wall
            if(source == DamageSource.OUT_OF_WORLD){ this.remove();return true;}
            if(source == DamageSource.DROWN || source == DamageSource.IN_WALL) return false;
            //workaround for wither boss, otherwise it would keep targeting the dummy forever
            if(source.getImmediateSource() instanceof WitherEntity|| source.getTrueSource() instanceof WitherEntity){
            	this.dismantle(true);
            	return true;
            
            }

            //lots of living entity code here
            if (source.isFireDamage() && this.isPotionActive(Effects.FIRE_RESISTANCE)) return false;

	       if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
            this.getItemStackFromSlot(EquipmentSlotType.HEAD).damageItem((int)(damage * 4.0F + this.rand.nextFloat() * damage * 2.0F), this, (p_213341_0_) -> {
               p_213341_0_.sendBreakAnimation(EquipmentSlotType.HEAD);
            });
            damage *= 0.75F;
         	}
	     
            

			// dismantling + adding players to dps message list
			if (source.damageType.equals("player") || source.getTrueSource() instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) source.getTrueSource();
				if (!world.isRemote) {
					ServerPlayerEntity sp = (ServerPlayerEntity) player;
					if (!this.currentlyAttacking.contains(sp)) {
						this.currentlyAttacking.add(sp);
					}

				}
				// shift-leftclick with empty hand dismantles
				if (player.isSneaking() && player.getHeldItemMainhand().isEmpty()) {
					dismantle(!player.isCreative());
					return false;
				}
			}


			//vanilla livingentity code down here

			//check if i'm on invulnerability frame
			if ((float)this.hurtResistantTime > 10.0F) {
							
	           	//check if i received damage greater that previous. if not do nothing cause bigger damage overrides smaller
	            
	            //currently instant damage tipped arrows do not work cause of this vanilla code. (pot damage overrides arrow since thay happen in same tick...)
	            //Awesome game design right here mojang!
	            if (damage <= this.lastDamage) {
	               return false;
	               
	            }
				//if true deal that damage minus the one i just inflicted.
				
				float ld = this.lastDamage;
				this.lastDamage = damage;
	            damage = damage - ld;
	            
	         } else {
	         	//if i'm not on invulnerability frame deal normal damage and reset cooldowns
	 
	            this.lastDamage = damage;
	            this.hurtResistantTime = 20;
	            this.maxHurtTime = 10;

	            //don't know what this does. probably sends a packet of some sort. seems to be related to red overlay so I disabled
	            //this.world.setEntityState(this, (byte)2);
	         }

	        //set to 0 to disable red glow that happens when hurt
	        this.hurtTime = 0;//this.maxHurtTime;


			
			// calculate the ACTUAL damage done after armor n stuff
			
			if (!world.isRemote) {
				damage = ForgeHooks.onLivingHurt(this, source, damage);
				if (damage > 0) {
					damage = this.applyArmorCalculations(source, damage);
					damage = this.applyPotionDamageCalculations(source, damage);
					float f1 = damage;
					damage = Math.max(damage - this.getAbsorptionAmount(), 0.0F);
					this.setAbsorptionAmount(this.getAbsorptionAmount() - (f1 - damage));				
				}
			}
			// magic code ^


			// damage in the same tick, add it
			if (lastDamageTick == this.ticksExisted) {
				lastDamage += damage;
				shake += damage ;
				shake = Math.min(shake, 60f);
			} else {
				// OUCH :(
				shake =  Math.min(damage, 60f);
				lastDamage = damage;
				lastDamageTick = this.ticksExisted;
			}

			
			if (!this.world.isRemote) {
				//custom update packet

				Network.sendToAllTracking(this.world, this, new Network.PacketDamageNumber(this.getEntityId(), damage, shake));
				
				// damage numebrssss
				int color = getColorFromDamageSource(source);
				DummyNumberEntity.NumberEntity number = new DummyNumberEntity.NumberEntity(damage, color, this.mynumberpos++, this.world);
				number.setLocationAndAngles(this.getPosX(), this.getPosY()+1, this.getPosZ(), 0.0F, 0.0F);
				this.world.addEntity(number);

				this.critical=false;

						

				this.damageTaken += damage;
				if (firstDamageTick == 0) {
					firstDamageTick = this.ticksExisted;
				}
			}
			return true;
		}


		public void applyEquipmentModifiers() {
			//living entity code here. apparently every entity does this check every tick.
			//trying instead to run it only when needed instead
			if (!this.world.isRemote) {
				for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
					ItemStack itemstack;
					if (equipmentslottype.getSlotType()== EquipmentSlotType.Group.ARMOR) {
						itemstack = this.inventoryArmor.get(equipmentslottype.getIndex());

						ItemStack itemstack1 = this.getItemStackFromSlot(equipmentslottype);
						if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
							if (!itemstack1.equals(itemstack, true))
								//send packet
								//Network.sendToAllTracking(this.world,this, new Network.PacketSyncEquip(this.getEntityId(), equipmentslottype.getIndex(), itemstack));

								net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent(this, equipmentslottype, itemstack, itemstack1));
							if (!itemstack.isEmpty()) {
								this.getAttributeManager().removeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
							}
							if (!itemstack1.isEmpty()) {
								this.getAttributeManager().reapplyModifiers(itemstack1.getAttributeModifiers(equipmentslottype));
							}
						}
					}
				}
			}
		}
		 
		@Override
		public void tick() {

			//check if on stable ground. used for automation
			if (this.world != null && this.world.getGameTime() % 20L == 0L) {
				if (world.isAirBlock(this.getOnPosition())) {
					this.dismantle(true);
				}
			}

			
			//used for fire damage, poison damage etc.
			//so you can damage it like any mob
			//for some reason instant damage 2 arrows show their damage twice
			this.baseTick();

	        //set to 0 to disable red glow that happens when hurt
	        this.hurtTime = 0;//this.maxHurtTime;  
			
			if (shake > 0) {

				shakeAnimation++;
				shake -= 0.8f;
				if (shake <= 0) {
					shakeAnimation = 0;
					shake = 0;
					
				}
			}
			//used only for dragon head mouth
			this.prevLimbSwingAmount=0;
			this.limbSwingAmount=0; 
			this.limbSwing=shake; 


			// DPS!
			//&& this.ticksExisted - lastDamageTick >60 for static

			//am i being attacked?
			if (!getEntityWorld().isRemote && this.damageTaken > 0) { 

				boolean isdynamic = Config.Configs.DYNAMIC_DPS.get();
				boolean flag = isdynamic? (this.ticksExisted == lastDamageTick+1) : (this.ticksExisted - lastDamageTick) >60;
				

				//only show damage after second damage tick
				if (flag && firstDamageTick < lastDamageTick) {

					// it's not actual DPS but "damage per tick scaled to seconds".. but meh.
					float seconds = (lastDamageTick - firstDamageTick) / 20f + 1;
					float dps = damageTaken / seconds;
					for (ServerPlayerEntity p : this.currentlyAttacking) {
						p.sendStatusMessage(new StringTextComponent("Target Dummy: " + new DecimalFormat("#.##").format(dps)+" DPS"), true);
				
					}
					

				}
				//out of combat. reset variables
 				if(this.ticksExisted - lastDamageTick >60){
 					this.currentlyAttacking.clear();
					this.damageTaken = 0;
					this.firstDamageTick = 0;
 				}
			}
		}


		@Override
		protected boolean isMovementBlocked() {
			return true;
		}

		@Override
		public boolean canBePushed() {
			return false;
		}

		@Override
		public boolean canBeCollidedWith() {
			return true;
		}

		//called when entity is first spawned/loaded
		@Override
		public void writeSpawnData(PacketBuffer buffer) {
			buffer.writeFloat(this.shake);
			buffer.writeBoolean(this.sheared);
			//hijacking this method to do some other server calculations. there's probably an event just for this but I haven't found it
			this.updateOnLoadServer();

		}

		//called when entity is first spawned/loaded
		@Override
		public void readSpawnData(PacketBuffer additionalData) {
			this.shake = additionalData.readFloat();
			this.sheared = additionalData.readBoolean();	
			//and this as well to do some other client calculations
			this.updateOnLoadClient();
			
		}


		@Override
		public void writeAdditional(CompoundNBT tag) {
			super.writeAdditional(tag);
			tag.putFloat("shake", this.shake);
			tag.putInt("type", this.mobType);
			tag.putInt("damage number pos", this.mynumberpos);
			tag.putBoolean("sheared", this.sheared);
		}

		@Override
		public void readAdditional(CompoundNBT tag) {
			super.readAdditional(tag);
			this.shake = tag.getFloat("shake");
			this.mobType = tag.getInt("type");
			this.mynumberpos = tag.getInt("damage number pos");
			this.sheared = tag.getBoolean("sheared");
		}


		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}

		@Override
		public CreatureAttribute getCreatureAttribute() {
			switch (this.mobType){
				default:
				case 0:
					return CreatureAttribute.UNDEFINED;
				case 1:
					return CreatureAttribute.UNDEAD;
				case 2:
					return CreatureAttribute.WATER;
				case 3:
					return CreatureAttribute.ILLAGER;
			}
		}

		@Override
		public boolean canDespawn(double distanceToClosestPlayer) {
			return false;
		}

		protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
			super.dropSpecialItems(source, looting, recentlyHitIn);
		}

		@Override
		public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
			return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.armor_stand.hit"));
		}

		@Override
		public net.minecraft.util.SoundEvent getDeathSound() {
			return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.armor_stand.break"));
		}

		@Override
		public boolean onLivingFall(float l, float d) {
			return false;
		}

		public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
			return MobEntity.func_233666_p_()
					.createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D)
					.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0D)
					.createMutableAttribute(Attributes.MAX_HEALTH, 20D)
					.createMutableAttribute(Attributes.ARMOR, 0D)
					.createMutableAttribute(Attributes.ATTACK_DAMAGE, 0D)
					.createMutableAttribute(Attributes.FLYING_SPEED, 0D);
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
