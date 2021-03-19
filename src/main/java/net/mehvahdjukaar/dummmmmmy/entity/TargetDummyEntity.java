
package net.mehvahdjukaar.dummmmmmy.entity;

import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.common.NetworkHandler;
import net.mehvahdjukaar.dummmmmmy.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.CreatureAttribute;
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
import net.minecraft.util.text.TranslationTextComponent;
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


public class TargetDummyEntity extends MobEntity implements IEntityAdditionalSpawnData{

	public float prevLimbswing=0;
	public float prevShakeAmount=0;

	public float shakeAmount=0; // used to have an independent start for the animation, otherwise the phase of
									// the animation depends ont he damage dealt
									// used to calculate the whole damage in one tick, in case there are multiple
									// sources
	public float lastDamage;
	public int lastDamageTick;
	public int firstDamageTick; // indicates when we started taking damage and if != 0 it also means that we are
								// currently recording damage taken
	public float damageTaken;
	public boolean critical = false; //has just been hit by critical?
	public MobAttribute mobType = MobAttribute.UNDEFINED; //0=undefined, 1=undead, 2=water, 3= illager
	private final List<ServerPlayerEntity> currentlyAttacking = new ArrayList<>();//players to which display dps message
	private int mynumberpos = 0;
	public boolean sheared = false;
	//public DummyNumberEntity.CustomEntity myLittleNumber;
	private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);

	public TargetDummyEntity(FMLPlayMessages.SpawnEntity packet, World world) {
		this(Registry.TARGET_DUMMY.get(), world);
	}

	public TargetDummyEntity(EntityType<TargetDummyEntity> type, World world) {
		super(type, world);
		experienceValue = 0;
		//so can take all sorts of damage
		//setNoAI(true);
		Arrays.fill(this.inventoryArmorDropChances, 1.1f);

	}

	public TargetDummyEntity(World world) {
		this(Registry.TARGET_DUMMY.get(), world);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	//stuff needed by client only?
	//called when entity is first spawned/loaded
	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeBoolean(this.sheared);
		//hijacking this method to do some other server calculations. there's probably an event just for this but I haven't found it
		this.updateOnLoadServer();

	}

	//called when entity is first spawned/loaded
	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.sheared = additionalData.readBoolean();
		//and this as well to do some other client calculations
		this.updateOnLoadClient();
	}

	@Override
	public void writeAdditional(CompoundNBT tag) {
		super.writeAdditional(tag);
		tag.putInt("Type", this.mobType.ordinal());
		tag.putInt("NumberPos", this.mynumberpos);
		tag.putBoolean("Sheared", this.sheared);
	}

	@Override
	public void readAdditional(CompoundNBT tag) {
		super.readAdditional(tag);
		this.mobType = MobAttribute.values()[tag.getInt("Type")];
		this.mynumberpos = tag.getInt("NumberPos");
		this.sheared = tag.getBoolean("Sheared");
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
		if (!player.isSpectator() && player.abilities.allowEdit) {
			ItemStack itemstack = player.getHeldItem(hand);
			EquipmentSlotType equipmentslottype = getSlotForItemStack(itemstack);

			Item item = itemstack.getItem();

			//special items
			if(item instanceof BannerItem || this.isPumpkin(item)){
				equipmentslottype = EquipmentSlotType.HEAD;
			}


			// empty hand -> unequip
			if (itemstack.isEmpty() && hand == Hand.MAIN_HAND) {
				equipmentslottype = this.getClickedSlot(vec);
				if (this.hasItemInSlot(equipmentslottype)) {
					if(player.world.isRemote)return ActionResultType.CONSUME;
					this.unequipArmor(player, equipmentslottype, itemstack, hand);
					invchanged = true;

				}
			}
			// armor item in hand -> equip/swap
			else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR) {
				if(player.world.isRemote)return ActionResultType.CONSUME;
				this.equipArmor(player, equipmentslottype, itemstack, hand);
				invchanged = true;

			}
			//remove sack
			else if (item instanceof ShearsItem) {
				if (!this.sheared) {
					if(player.world.isRemote)return ActionResultType.CONSUME;
					this.sheared = true;
					if (!this.world.isRemote) {
						NetworkHandler.sendToAllTracking(this, (ServerWorld) this.world,
								new NetworkHandler.PacketChangeSkin(this.getEntityId(), true));
					}
					return ActionResultType.SUCCESS;
				}
			}


			if (invchanged) {
				if (!this.world.isRemote) {
					NetworkHandler.sendToAllTracking(this, (ServerWorld) this.world, new NetworkHandler.PacketSyncEquip(this.getEntityId(), equipmentslottype.getIndex(), this.getItemStackFromSlot(equipmentslottype)));
				}
				//this.applyEquipmentModifiers();
				return ActionResultType.SUCCESS;
			}


		}
		return ActionResultType.PASS;
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
		if(slot==EquipmentSlotType.HEAD)this.mobType = MobAttribute.UNDEFINED;

	}

	private void equipArmor(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Hand hand) {
		ItemStack currentItem = this.getItemStackFromSlot(slot);
		ItemStack newItem = stack.copy();
		newItem.setCount(1);

		player.setHeldItem(hand, DrinkHelper.fill(stack.copy(), player, currentItem, player.isCreative()));

		this.playEquipSound(newItem);
		this.setItemStackToSlot(slot, newItem);

		//this.applyEquipmentModifiers();
		//now done here^
		this.getAttributeManager().reapplyModifiers(newItem.getAttributeModifiers(slot));
		if(slot==EquipmentSlotType.HEAD) {
			//add mob type
			if (this.isUndeadSkull(newItem)) this.mobType = MobAttribute.UNDEAD;
			else if (newItem.getItem() == Items.TURTLE_HELMET) this.mobType = MobAttribute.WATER;
			else if (newItem.getItem() == Items.DRAGON_HEAD) this.mobType = MobAttribute.ARTHROPOD;
			else if (ItemStack.areItemStacksEqual(newItem, Raid.createIllagerBanner())) this.mobType = MobAttribute.ILLAGER;
			else if (this.isPumpkin(newItem.getItem())) this.mobType = MobAttribute.SCARECROW;
			else this.mobType = MobAttribute.UNDEFINED;
		}
	}

	private boolean isPumpkin(Item item){
		return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof CarvedPumpkinBlock;
	}

	private boolean isUndeadSkull(ItemStack itemstack){
		Item i =itemstack.getItem();
		return i == Items.WITHER_SKELETON_SKULL ||
				i == Items.SKELETON_SKULL ||
				i == Items.ZOMBIE_HEAD;
	}

	public boolean isScarecrow(){
		return this.mobType==MobAttribute.SCARECROW;
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
		if (!this.world.isRemote) {
			if (drops) {
				this.dropInventory();
				this.entityDropItem(Registry.DUMMY_ITEM.get(), 1);
			}
			this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), this.getDeathSound(),
					this.getSoundCategory(), 1.0F, 1.0F);

			((ServerWorld) this.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()),
					this.getPosX(), this.getPosYHeight(0.6666666666666666D), this.getPosZ(), 10,  (this.getWidth() / 4.0F),
					(this.getHeight() / 4.0F),  (this.getWidth() / 4.0F), 0.05D);
			this.remove();
		}
	}

	@Override
	public void onKillCommand() {
	  this.dismantle(true);
	}

	public int getColorFromDamageSource(DamageSource source){

		if(this.critical) return Configs.cached.DAMAGE_CRIT;
		if(source == DamageSource.DRAGON_BREATH) return Configs.cached.DAMAGE_DRAGON;
		if(source == DamageSource.WITHER)return  Configs.cached.DAMAGE_WITHER;
		if(source.damageType.equals("explosion") || source.damageType.equals("explosion.player"))
			return Configs.cached.DAMAGE_EXPLOSION;
		if(source.damageType.equals("indirectMagic"))return Configs.cached.DAMAGE_IND_MAGIC;
		if(source.damageType.equals("trident"))return Configs.cached.DAMAGE_TRIDENT;
		if(source == DamageSource.GENERIC)return Configs.cached.DAMAGE_GENERIC;

		if(source == DamageSource.MAGIC){
			//would really like to detect poison damage but i don't think there's simple way

			return Configs.cached.DAMAGE_MAGIC;
		}
		if (source == DamageSource.HOT_FLOOR || source == DamageSource.LAVA || source == DamageSource.ON_FIRE
				|| source ==DamageSource.IN_FIRE) return Configs.cached.DAMAGE_FIRE;
		if (source == DamageSource.LIGHTNING_BOLT) return Configs.cached.DAMAGE_LIGHTNING;

		if (source == DamageSource.CACTUS || source ==DamageSource.SWEET_BERRY_BUSH)return Configs.cached.DAMAGE_CACTUS;

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
		//this.getItemStackFromSlot(EquipmentSlotType.HEAD).damageItem((int)(damage * 4.0F + this.rand.nextFloat() * damage * 2.0F), this, (p_213341_0_) -> p_213341_0_.sendBreakAnimation(EquipmentSlotType.HEAD));
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
			this.limbSwing += damage ;
			this.limbSwing = Math.min(this.limbSwing, 60f);
		} else {
			// OUCH :(
			this.limbSwing =  Math.min(damage, 60f);
			lastDamage = damage;
			lastDamageTick = this.ticksExisted;
		}


		if (!this.world.isRemote) {
			//custom update packet

			NetworkHandler.sendToAllTracking(this, (ServerWorld) this.world, new NetworkHandler.PacketDamageNumber(this.getEntityId(), damage, this.limbSwing));

			// damage numebrssss
			int color = getColorFromDamageSource(source);
			DummyNumberEntity number = new DummyNumberEntity(damage, color, this.mynumberpos++, this.world);
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


	@Override
	public void tick() {

		BlockPos onPos = this.getOnPosition();

		//check if on stable ground. used for automation
		if (this.world != null && this.world.getGameTime() % 20L == 0L && !this.world.isRemote) {
			if (world.isAirBlock(onPos)) {
				this.dismantle(true);
				return;
			}
		}

		this.setNoGravity(true);
		this.world.getBlockState(onPos).getBlock().onEntityWalk(this.world,onPos,this);


		//used for fire damage, poison damage etc.
		//so you can damage it like any mob

		this.baseTick();


		//living tick stuff. dont remove
		Vector3d vector3d = this.getMotion();
		double d1 = vector3d.x;
		double d3 = vector3d.y;
		double d5 = vector3d.z;
		if (Math.abs(vector3d.x) < 0.003D) {
			d1 = 0.0D;
		}
		if (Math.abs(vector3d.y) < 0.003D) {
			d3 = 0.0D;
		}
		if (Math.abs(vector3d.z) < 0.003D) {
			d5 = 0.0D;
		}

		this.setMotion(d1, d3, d5);

		this.world.getProfiler().startSection("travel");
		this.travel(new Vector3d(this.moveStrafing, this.moveVertical, this.moveForward));
		this.world.getProfiler().endSection();


		this.world.getProfiler().startSection("push");
		this.collideWithNearbyEntities();
		this.world.getProfiler().endSection();
		//end living tick stuff



		if(this.world.isRemote) {
			//set to 0 to disable red glow that happens when hurt
			this.hurtTime = 0;//this.maxHurtTime;
			this.prevShakeAmount = this.shakeAmount;
			this.prevLimbswing = this.limbSwing;
			//client animation
			if (this.limbSwing > 0) {

				this.shakeAmount++;
				this.limbSwing -= 0.8f;
				if (this.limbSwing <= 0) {
					this.shakeAmount = 0;
					this.limbSwing = 0;
				}
			}

		}
		else {
			this.setFlag(6, this.isGlowing());
			if (!this.glowing) {
				boolean flag = this.isPotionActive(Effects.GLOWING);
				if (this.getFlag(6) != flag) {
					this.setFlag(6, flag);
				}
			}

			if(this.damageTaken > 0){
				// DPS!
				//&& this.ticksExisted - lastDamageTick >60 for static

				//am i being attacked?

				boolean isdynamic = Configs.cached.DYNAMIC_DPS;
				boolean flag = isdynamic? (this.ticksExisted == lastDamageTick+1) : (this.ticksExisted - lastDamageTick) >60;


				//only show damage after second damage tick
				if (flag && firstDamageTick < lastDamageTick) {

					// it's not actual DPS but "damage per tick scaled to seconds".. but meh.
					float seconds = (lastDamageTick - firstDamageTick) / 20f + 1;
					float dps = damageTaken / seconds;
					for (ServerPlayerEntity p : this.currentlyAttacking) {
						if(p.getDistance(this)<64)
							p.sendStatusMessage(new TranslationTextComponent("message.dummmmmmy.dps", new DecimalFormat("#.##").format(dps)), true);
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



	}

	@Override
	public boolean canBreatheUnderwater() {
	  return true;
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
		return super.canBeCollidedWith();
	}

	@Override
	public boolean onLivingFall(float l, float d) {
		return false;
	}

	@Override
	protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos) {}

	@Override
	public void setNoGravity(boolean ignored) {
		super.setNoGravity(true);
	}

	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.armor_stand.hit"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.armor_stand.break"));
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return this.mobType.get();
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



	private enum MobAttribute{
		UNDEFINED,
		UNDEAD,
		WATER,
		ILLAGER,
		ARTHROPOD,
		SCARECROW;

		public CreatureAttribute get(){
			switch (this){
				default:
				case UNDEFINED:
					return CreatureAttribute.UNDEFINED;
				case UNDEAD:
					return CreatureAttribute.UNDEAD;
				case WATER:
					return CreatureAttribute.WATER;
				case ILLAGER:
					return CreatureAttribute.ILLAGER;
				case ARTHROPOD:
					return CreatureAttribute.ARTHROPOD;
			}
		}
	}


}