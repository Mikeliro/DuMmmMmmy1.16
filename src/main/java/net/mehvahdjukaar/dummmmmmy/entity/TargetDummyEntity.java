
package net.mehvahdjukaar.dummmmmmy.entity;

import net.mehvahdjukaar.dummmmmmy.common.Configs;
import net.mehvahdjukaar.dummmmmmy.common.NetworkHandler;
import net.mehvahdjukaar.dummmmmmy.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TargetDummyEntity extends MobEntity implements IEntityAdditionalSpawnData, IForgeEntity {

    public float prevLimbSwing = 0;
    public float prevShakeAmount = 0;

    public float shakeAmount = 0;   // used to have an independent start for the animation, otherwise the phase of
    // the animation depends ont he damage dealt
    // used to calculate the whole damage in one tick, in case there are multiple
    // sources
    public float lastDamage;
    public int lastDamageTick;
    public int firstDamageTick; // indicates when we started taking damage and if != 0 it also means that we are
    // currently, recording damage taken
    public float damageTaken;
    public boolean critical = false; //has just been hit by critical?
    public MobAttribute mobType = MobAttribute.UNDEFINED; //0=undefined, 1=undead, 2=water, 3= illager
    private final List<ServerPlayerEntity> currentlyAttacking = new ArrayList<>();//players to which display dps message
    private int damageNumberPos = 0; //position of damage number in the semicircle
    public boolean sheared = false;

    private final NonNullList<ItemStack> lastArmorItem = NonNullList.withSize(4, ItemStack.EMPTY);

    public TargetDummyEntity(FMLPlayMessages.SpawnEntity packet, World world) {
        this(Registry.TARGET_DUMMY.get(), world);
    }

    public TargetDummyEntity(EntityType<TargetDummyEntity> type, World world) {
        super(type, world);
    }

    public TargetDummyEntity(World world) {
        this(Registry.TARGET_DUMMY.get(), world);
        xpReward = 0;
        //so can take all sorts of damage
        //setNoAI(true);
        Arrays.fill(this.armorDropChances, 1.1f);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
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
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Type", this.mobType.ordinal());
        tag.putInt("NumberPos", this.damageNumberPos);
        tag.putBoolean("Sheared", this.sheared);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        this.mobType = MobAttribute.values()[tag.getInt("Type")];
        this.damageNumberPos = tag.getInt("NumberPos");
        this.sheared = tag.getBoolean("Sheared");
    }

    public void updateOnLoadClient() {
        float r = this.yRot;
        this.yHeadRotO = this.yHeadRot = r;
        this.yRotO = r;
        this.yBodyRotO = this.yBodyRot = r;

    }

    public void updateOnLoadServer() {
        this.applyEquipmentModifiers();
    }

    // dress it up! :D
    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d vec, Hand hand) {
        boolean invchanged = false;
        if (!player.isSpectator() && player.abilities.mayBuild) {
            ItemStack itemstack = player.getItemInHand(hand);
            EquipmentSlotType equipmentslottype = getEquipmentSlotForItem(itemstack);

            Item item = itemstack.getItem();

            //special items
            if (item instanceof BannerItem || this.isPumpkin(item) || item.canEquip(itemstack, EquipmentSlotType.HEAD, this)) {
                equipmentslottype = EquipmentSlotType.HEAD;
            }


            // empty hand -> unequip
            if (itemstack.isEmpty() && hand == Hand.MAIN_HAND) {
                equipmentslottype = this.getClickedSlot(vec);
                if (this.hasItemInSlot(equipmentslottype)) {
                    if (player.level.isClientSide) return ActionResultType.CONSUME;
                    this.unequipArmor(player, equipmentslottype, itemstack, hand);
                    invchanged = true;

                }
            }
            // armor item in hand -> equip/swap
            else if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR) {
                if (player.level.isClientSide) return ActionResultType.CONSUME;
                this.equipArmor(player, equipmentslottype, itemstack, hand);
                invchanged = true;

            }
            //remove sack
            else if (item instanceof ShearsItem) {
                if (!this.sheared) {
                    if (player.level.isClientSide) return ActionResultType.CONSUME;
                    this.sheared = true;
                    if (!this.level.isClientSide) {
                        NetworkHandler.sendToAllTracking(this, (ServerWorld) this.level,
                                new NetworkHandler.PacketChangeSkin(this.getId(), true));
                    }
                    return ActionResultType.SUCCESS;
                }
            }


            if (invchanged) {
                this.setLastArmorItem(equipmentslottype, itemstack);
                if (!this.level.isClientSide) {
                    NetworkHandler.sendToAllTracking(this, (ServerWorld) this.level, new NetworkHandler.PacketSyncEquip(this.getId(), equipmentslottype.getIndex(), this.getItemBySlot(equipmentslottype)));
                }
                //this.applyEquipmentModifiers();
                return ActionResultType.SUCCESS;
            }


        }
        return ActionResultType.PASS;
    }

    private void unequipArmor(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Hand hand) {
        // set slot to stack which is empty stack
        ItemStack itemstack = this.getItemBySlot(slot);
        ItemStack itemstack2 = itemstack.copy();

        player.setItemInHand(hand, itemstack2);
        this.setItemSlot(slot, stack);

        //this.applyEquipmentModifiers();
        //now done here^
        this.getAttributes().removeAttributeModifiers(itemstack2.getAttributeModifiers(slot));
        //clear mob type
        if (slot == EquipmentSlotType.HEAD) this.mobType = MobAttribute.UNDEFINED;

    }

    private void equipArmor(PlayerEntity player, EquipmentSlotType slot, ItemStack stack, Hand hand) {
        ItemStack currentItem = this.getItemBySlot(slot);
        ItemStack newItem = stack.copy();
        newItem.setCount(1);

        player.setItemInHand(hand, DrinkHelper.createFilledResult(stack.copy(), player, currentItem, player.isCreative()));

        this.playEquipSound(newItem);
        this.setItemSlot(slot, newItem);

        //this.applyEquipmentModifiers();
        //now done here^
        this.getAttributes().addTransientAttributeModifiers(newItem.getAttributeModifiers(slot));
        if (slot == EquipmentSlotType.HEAD) {
            //add mob type
            if (this.isUndeadSkull(newItem)) this.mobType = MobAttribute.UNDEAD;
            else if (newItem.getItem() == Items.TURTLE_HELMET) this.mobType = MobAttribute.WATER;
            else if (newItem.getItem() == Items.DRAGON_HEAD) this.mobType = MobAttribute.ARTHROPOD;
            else if (ItemStack.matches(newItem, Raid.getLeaderBannerInstance())) this.mobType = MobAttribute.ILLAGER;
            else if (this.isPumpkin(newItem.getItem())) this.mobType = MobAttribute.SCARECROW;
            else this.mobType = MobAttribute.UNDEFINED;
        }
    }

    private boolean isPumpkin(Item item) {
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            String name = item.getRegistryName().getPath();
            return block instanceof CarvedPumpkinBlock || name.contains("pumpkin") || name.contains("jack_o");
        }
        return false;
    }

    private boolean isUndeadSkull(ItemStack itemstack) {
        Item i = itemstack.getItem();
        return i == Items.WITHER_SKELETON_SKULL ||
                i == Items.SKELETON_SKULL ||
                i == Items.ZOMBIE_HEAD;
    }

    public boolean isScarecrow() {
        return this.mobType == MobAttribute.SCARECROW;
    }

    private EquipmentSlotType getClickedSlot(Vector3d p_190772_1_) {
        EquipmentSlotType equipmentslottype = EquipmentSlotType.MAINHAND;
        double d0 = p_190772_1_.y;
        EquipmentSlotType equipmentslottype1 = EquipmentSlotType.FEET;
        if (d0 >= 0.1D && d0 < 0.1D + (0.45D) && this.hasItemInSlot(equipmentslottype1)) {
            equipmentslottype = EquipmentSlotType.FEET;
        } else if (d0 >= 0.9D + (0.0D) && d0 < 0.9D + (0.7D) && this.hasItemInSlot(EquipmentSlotType.CHEST)) {
            equipmentslottype = EquipmentSlotType.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (0.8D) && this.hasItemInSlot(EquipmentSlotType.LEGS)) {
            equipmentslottype = EquipmentSlotType.LEGS;
        } else if (d0 >= 1.6D && this.hasItemInSlot(EquipmentSlotType.HEAD)) {
            equipmentslottype = EquipmentSlotType.HEAD;
        }
        return equipmentslottype;
    }


    private void setLastArmorItem(EquipmentSlotType type, ItemStack stack) {
        this.lastArmorItem.set(type.getIndex(), stack);
    }

    public void applyEquipmentModifiers() {
        //living entity code here. apparently every entity does this check every tick.
        //trying instead to run it only when needed instead
        if (!this.level.isClientSide) {
            for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
                ItemStack itemstack;
                if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR) {
                    itemstack = this.lastArmorItem.get(equipmentslottype.getIndex());

                    ItemStack itemstack1 = this.getItemBySlot(equipmentslottype);
                    if (!ItemStack.matches(itemstack1, itemstack)) {
                        if (!itemstack1.equals(itemstack, true))
                            //send packet
                            //Network.sendToAllTracking(this.world,this, new Network.PacketSyncEquip(this.getEntityId(), equipmentslottype.getIndex(), itemstack));

                            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent(this, equipmentslottype, itemstack, itemstack1));
                        if (!itemstack.isEmpty()) {
                            this.getAttributes().removeAttributeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
                        }
                        if (!itemstack1.isEmpty()) {
                            this.getAttributes().addTransientAttributeModifiers(itemstack1.getAttributeModifiers(equipmentslottype));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dropEquipment() {
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot.getType() != EquipmentSlotType.Group.ARMOR) {
                continue;
            }
            ItemStack armor = getItemBySlot(slot);
            if (!armor.isEmpty()) {
                this.spawnAtLocation(armor, 1.0f);
            }
        }
    }

    public void dismantle(boolean drops) {
        if (!this.level.isClientSide) {
            if (drops) {
                this.dropEquipment();
                this.spawnAtLocation(Registry.DUMMY_ITEM.get(), 1);
            }
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getDeathSound(),
                    this.getSoundSource(), 1.0F, 1.0F);

            ((ServerWorld) this.level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
                    this.getX(), this.getY(0.6666666666666666D), this.getZ(), 10, (this.getBbWidth() / 4.0F),
                    (this.getBbHeight() / 4.0F), (this.getBbWidth() / 4.0F), 0.05D);
            this.remove();
        }
    }

    @Override
    public void kill() {
        this.dismantle(true);
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(Registry.DUMMY_ITEM.get());
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        //call forge event
        if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, damage)) return false;

        if (this.isInvulnerableTo(source)) return false;

        //super.attackEntityFrom(source, damage);

        //not immune to void damage, immune to drown, wall
        if (source == DamageSource.OUT_OF_WORLD) {
            this.remove();
            return true;
        }
        if (source == DamageSource.DROWN || source == DamageSource.IN_WALL) return false;
        //workaround for wither boss, otherwise it would keep targeting the dummy forever
        if (source.getDirectEntity() instanceof WitherEntity || source.getEntity() instanceof WitherEntity) {
            this.dismantle(true);
            return true;

        }

        //lots of living entity code here
        if (source.isFire() && this.hasEffect(Effects.FIRE_RESISTANCE)) return false;

        if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemBySlot(EquipmentSlotType.HEAD).isEmpty()) {
            //this.getItemStackFromSlot(EquipmentSlotType.HEAD).damageItem((int)(damage * 4.0F + this.rand.nextFloat() * damage * 2.0F), this, (p_213341_0_) -> p_213341_0_.sendBreakAnimation(EquipmentSlotType.HEAD));
            damage *= 0.75F;
        }


        // dismantling + adding players to dps message list
        if (source.msgId.equals("player") || source.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getEntity();
            if (!level.isClientSide) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                if (!this.currentlyAttacking.contains(sp)) {
                    this.currentlyAttacking.add(sp);
                }

            }
            // shift-leftclick with empty hand dismantles
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                dismantle(!player.isCreative());
                return false;
            }
        }


        //vanilla livingentity code down here

        //check if i'm on invulnerability frame
        if ((float) this.invulnerableTime > 10.0F) {

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

            this.playHurtSound(source);
            //if i'm not on invulnerability frame deal normal damage and reset cooldowns

            this.lastDamage = damage;
            this.invulnerableTime = 20;
            this.hurtDuration = 10;

            //don't know what this does. probably sends a packet of some sort. seems to be related to red overlay so I disabled
            //this.world.setEntityState(this, (byte)2);
        }

        //set to 0 to disable red glow that happens when hurt
        this.hurtTime = 0;//this.maxHurtTime;


        // calculate the ACTUAL damage done after armor n stuff

        if (!level.isClientSide) {
            damage = ForgeHooks.onLivingHurt(this, source, damage);
            if (damage > 0) {
                damage = this.getDamageAfterArmorAbsorb(source, damage);
                damage = this.getDamageAfterMagicAbsorb(source, damage);
                float f1 = damage;
                damage = Math.max(damage - this.getAbsorptionAmount(), 0.0F);
                this.setAbsorptionAmount(this.getAbsorptionAmount() - (f1 - damage));
            }
        }
        // magic code ^


        // damage in the same tick, add it
        if (lastDamageTick == this.tickCount) {
            lastDamage += damage;
            this.animationPosition += damage;
            this.animationPosition = Math.min(this.animationPosition, 60f);
        } else {
            // OUCH :(
            this.animationPosition = Math.min(damage, 60f);
            lastDamage = damage;
            lastDamageTick = this.tickCount;
        }


        if (!this.level.isClientSide) {
            this.showDamageDealt(damage, getDamageType(source));
            this.critical = false;
        }
        return true;
    }


    private void showDamageDealt(float damage, DamageType type) {
        //custom update packet

        //I could have used synced entity attributes here
        //TODO: replace with entity attributes
        NetworkHandler.sendToAllTracking(this, (ServerWorld) this.level, new NetworkHandler.PacketDamageNumber(this.getId(), damage, this.animationPosition));

        // damage numebrssss
        DummyNumberEntity number = new DummyNumberEntity(damage, type, this.damageNumberPos++, this.level);
        number.moveTo(this.getX(), this.getY() + 1, this.getZ(), 0.0F, 0.0F);
        this.level.addFreshEntity(number);

        this.damageTaken += damage;
        if (firstDamageTick == 0) {
            firstDamageTick = this.tickCount;
        }
    }


    @Override
    public void tick() {
        //show true damage that has bypassed hurt method
        if (lastDamageTick + 1 == this.tickCount && !this.level.isClientSide) {
            float trueDamage = this.getMaxHealth() - this.getHealth();
            if (trueDamage > 0) {
                this.heal(trueDamage);
                this.showDamageDealt(trueDamage, DamageType.TRUE);
            }
        }

        BlockPos onPos = this.getOnPos();

        //check if on stable ground. used for automation
        if (this.level != null && this.level.getGameTime() % 20L == 0L && !this.level.isClientSide) {
            if (level.isEmptyBlock(onPos)) {
                this.dismantle(true);
                return;
            }
        }

        this.setNoGravity(true);
        this.level.getBlockState(onPos).getBlock().stepOn(this.level, onPos, this);


        //used for fire damage, poison damage etc.
        //so you can damage it like any mob

        this.baseTick();


        this.level.getProfiler().push("travel");
        this.travel(new Vector3d(this.xxa, this.yya, this.zza));
        this.level.getProfiler().pop();


        this.level.getProfiler().push("push");
        this.pushEntities();
        this.level.getProfiler().pop();
        //end living tick stuff

        this.level.getProfiler().push("rest");
        try {
            this.checkInsideBlocks();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
        this.level.getProfiler().pop();

        if (this.level.isClientSide) {
            //set to 0 to disable red glow that happens when hurt
            this.hurtTime = 0;//this.maxHurtTime;
            this.prevShakeAmount = this.shakeAmount;
            this.prevLimbSwing = this.animationPosition;
            //client animation
            if (this.animationPosition > 0) {

                this.shakeAmount++;
                this.animationPosition -= 0.8f;
                if (this.animationPosition <= 0) {
                    this.shakeAmount = 0;
                    this.animationPosition = 0;
                }
            }

        } else {
            this.setSharedFlag(6, this.isGlowing());
            if (!this.glowing) {
                boolean flag = this.hasEffect(Effects.GLOWING);
                if (this.getSharedFlag(6) != flag) {
                    this.setSharedFlag(6, flag);
                }
            }

            //here is for visially show dps on status mesasge
            if (this.damageTaken > 0) {
                // DPS!
                //&& this.ticksExisted - lastDamageTick >60 for static

                //am i being attacked?

                boolean isdynamic = Configs.cached.DYNAMIC_DPS;
                boolean flag = isdynamic ? (this.tickCount == lastDamageTick + 1) : (this.tickCount - lastDamageTick) > 60;


                //only show damage after second damage tick
                if (flag && firstDamageTick < lastDamageTick) {

                    // it's not actual DPS but "damage per tick scaled to seconds".. but meh.
                    float seconds = (lastDamageTick - firstDamageTick) / 20f + 1;
                    float dps = damageTaken / seconds;
                    for (ServerPlayerEntity p : this.currentlyAttacking) {
                        if (p.distanceTo(this) < 64)
                            p.displayClientMessage(new TranslationTextComponent("message.dummmmmmy.dps", new DecimalFormat("#.##").format(dps)), true);
                    }

                }
                //out of combat. reset variables
                if (this.tickCount - lastDamageTick > 60) {
                    this.currentlyAttacking.clear();
                    this.damageTaken = 0;
                    this.firstDamageTick = 0;
                }
            }
        }


    }


    @Override
    public void setDeltaMovement(Vector3d motionIn) {
    }

    @Override
    public void knockback(float strength, double ratioX, double ratioZ) {
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected boolean isImmobile() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable();
    }

    @Override
    public boolean causeFallDamage(float l, float d) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public CreatureAttribute getMobType() {
        return this.mobType.get();
    }

    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0D)
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.ARMOR, 0D)
                .add(Attributes.ATTACK_DAMAGE, 0D)
                .add(Attributes.FLYING_SPEED, 0D);
    }


    private enum MobAttribute {
        UNDEFINED,
        UNDEAD,
        WATER,
        ILLAGER,
        ARTHROPOD,
        SCARECROW;

        public CreatureAttribute get() {
            switch (this) {
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


    public enum DamageType {
        GENERIC,
        CRIT,
        DRAGON,
        WITHER,
        EXPLOSION,
        MAGIC,
        IND_MAGIC,
        TRIDENT,
        FIRE,
        LIGHTNING,
        CACTUS,
        TRUE;

        //only client
        public int getColor() {
            switch (this) {
                case CRIT:
                    return Configs.cached.DAMAGE_CRIT;
                case FIRE:
                    return Configs.cached.DAMAGE_FIRE;
                case MAGIC:
                    return Configs.cached.DAMAGE_MAGIC;
                case CACTUS:
                    return Configs.cached.DAMAGE_CACTUS;
                case DRAGON:
                    return Configs.cached.DAMAGE_DRAGON;
                case WITHER:
                    return Configs.cached.DAMAGE_WITHER;
                default:
                case GENERIC:
                    return Configs.cached.DAMAGE_GENERIC;
                case TRIDENT:
                    return Configs.cached.DAMAGE_TRIDENT;
                case EXPLOSION:
                    return Configs.cached.DAMAGE_EXPLOSION;
                case IND_MAGIC:
                    return Configs.cached.DAMAGE_IND_MAGIC;
                case LIGHTNING:
                    return Configs.cached.DAMAGE_LIGHTNING;
                case TRUE:
                    return Configs.cached.DAMAGE_TRUE;
            }
        }
    }


    public DamageType getDamageType(DamageSource source) {
        if (this.critical) return DamageType.CRIT;
        if (source == DamageSource.DRAGON_BREATH) return DamageType.DRAGON;
        if (source == DamageSource.WITHER) return DamageType.WITHER;
        if (source.msgId.equals("explosion") || source.msgId.equals("explosion.player") || source.isExplosion())
            return DamageType.EXPLOSION;
        if (source.msgId.equals("indirectMagic")) return DamageType.IND_MAGIC;
        if (source.msgId.equals("trident")) return DamageType.TRIDENT;
        if (source == DamageSource.HOT_FLOOR || source == DamageSource.LAVA || source == DamageSource.ON_FIRE
                || source == DamageSource.IN_FIRE || source.isFire()) return DamageType.FIRE;
        if (source == DamageSource.MAGIC || source.isMagic()) {
            //would really like to detect poison damage but i don't think there's simple way

            return DamageType.MAGIC;
        }
        if (source == DamageSource.LIGHTNING_BOLT) return DamageType.LIGHTNING;

        if (source == DamageSource.CACTUS || source == DamageSource.SWEET_BERRY_BUSH) return DamageType.CACTUS;
        return DamageType.GENERIC;
    }


}