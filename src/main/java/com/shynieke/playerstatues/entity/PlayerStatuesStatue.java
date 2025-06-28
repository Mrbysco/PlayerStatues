package com.shynieke.playerstatues.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.shynieke.playerstatues.network.ModNetworking;
import com.shynieke.playerstatues.network.message.PlayerStatueScreenMessage;
import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import com.shynieke.playerstatues.registry.ModSerializers;
import com.shynieke.playerstatues.util.PlayerStatueData;
import com.shynieke.playerstatues.util.SkinUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class PlayerStatuesStatue extends LivingEntity {
	private static final Rotations DEFAULT_HEAD_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
	private static final Rotations DEFAULT_BODY_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
	private static final Rotations DEFAULT_LEFTARM_ROTATION = new Rotations(-10.0F, 0.0F, -10.0F);
	private static final Rotations DEFAULT_RIGHTARM_ROTATION = new Rotations(-15.0F, 0.0F, 10.0F);
	private static final Rotations DEFAULT_LEFTLEG_ROTATION = new Rotations(-1.0F, 0.0F, -1.0F);
	private static final Rotations DEFAULT_RIGHTLEG_ROTATION = new Rotations(1.0F, 0.0F, 1.0F);

	private static final String DEFAULT_MODEL = PlayerStatueData.MODEL_TYPE.AUTO.name();
	private static final EntityDataAccessor<Optional<GameProfile>> GAMEPROFILE = SynchedEntityData.defineId(PlayerStatuesStatue.class, ModSerializers.OPTIONAL_GAME_PROFILE.get());
	public static final EntityDataAccessor<Byte> STATUS = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Float> Y_OFFSET = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Rotations> HEAD_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<Rotations> BODY_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<Rotations> LEFT_ARM_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<Rotations> RIGHT_ARM_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<Rotations> LEFT_LEG_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<Rotations> RIGHT_LEG_ROTATION = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ROTATIONS);
	public static final EntityDataAccessor<String> MODEL_TYPE = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Optional<UUID>> LOCKED_BY_UUID = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.OPTIONAL_UUID);
	public static final EntityDataAccessor<ItemStack> USED_UPGRADE_ITEM = SynchedEntityData.defineId(PlayerStatuesStatue.class, EntityDataSerializers.ITEM_STACK);

	private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
	private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
	/**
	 * After punching the stand, the cooldown before you can punch it again without breaking it.
	 */
	public long punchCooldown;
	private int disabledSlots;
	private boolean isSlim = false;
	private Rotations headRotation = DEFAULT_HEAD_ROTATION;
	private Rotations bodyRotation = DEFAULT_BODY_ROTATION;
	private Rotations leftArmRotation = DEFAULT_LEFTARM_ROTATION;
	private Rotations rightArmRotation = DEFAULT_RIGHTARM_ROTATION;
	private Rotations leftLegRotation = DEFAULT_LEFTLEG_ROTATION;
	private Rotations rightLegRotation = DEFAULT_RIGHTLEG_ROTATION;

	public PlayerStatuesStatue(EntityType<? extends PlayerStatuesStatue> entityType, Level level) {
		super(entityType, level);
	}

	public PlayerStatuesStatue(Level level, double posX, double posY, double posZ) {
		this(ModEntities.PLAYER_STATUE_ENTITY.get(), level);
		this.setPos(posX, posY, posZ);
	}

	public int clientLock = 0;

	@Override
	public void setYRot(float yRot) {
		if (this.clientLock > 0) {
			return;
		}
		super.setYRot(yRot);
	}

	@Override
	public float getStepHeight() {
		return 0.0F;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void refreshDimensions() {
		double d0 = this.getX();
		double d1 = this.getY();
		double d2 = this.getZ();
		super.refreshDimensions();
		this.setPos(d0, d1, d2);
	}

	private boolean hasPhysics() {
		return !this.isNoGravity();
	}

	@Override
	public boolean isNoGravity() {
		return super.isNoGravity();
	}

	/**
	 * Returns whether the entity is in a server level
	 */
	@Override
	public boolean isEffectiveAi() {
		return super.isEffectiveAi() && this.hasPhysics();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(GAMEPROFILE, Optional.empty());
		this.entityData.define(STATUS, (byte) 0);
		this.entityData.define(Y_OFFSET, 0F);
		this.entityData.define(HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
		this.entityData.define(BODY_ROTATION, DEFAULT_BODY_ROTATION);
		this.entityData.define(LEFT_ARM_ROTATION, DEFAULT_LEFTARM_ROTATION);
		this.entityData.define(RIGHT_ARM_ROTATION, DEFAULT_RIGHTARM_ROTATION);
		this.entityData.define(LEFT_LEG_ROTATION, DEFAULT_LEFTLEG_ROTATION);
		this.entityData.define(RIGHT_LEG_ROTATION, DEFAULT_RIGHTLEG_ROTATION);
		this.entityData.define(LOCKED_BY_UUID, Optional.empty());
		this.entityData.define(MODEL_TYPE, DEFAULT_MODEL);
		this.entityData.define(USED_UPGRADE_ITEM, ItemStack.EMPTY);
	}

	public Optional<GameProfile> getGameProfile() {
		return entityData.get(GAMEPROFILE);
	}

	public void setGameProfile(GameProfile playerProfile) {
		SkinUtil.updateGameProfile(playerProfile, (profile) -> {
			entityData.set(GAMEPROFILE, Optional.of(profile));
			this.setSlim(profile != null && profile.getId() != null && SkinUtil.isSlimSkin(profile.getId()));
		});

		synchronized (this) {
			getGameProfile().ifPresent(profile -> {
				if (this.level() != null && this.level().isClientSide && profile != null && profile.isComplete()) {
					Minecraft.getInstance().getSkinManager().registerSkins(profile, (textureType, textureLocation, profileTexture) -> {
						if (textureType.equals(MinecraftProfileTexture.Type.SKIN)) {
							String metadata = profileTexture.getMetadata("model");
							this.setSlim(metadata != null && metadata.equals("slim"));
						}
					}, true);
				}
			});
		}
	}

	@Nullable
	public boolean isLocked() {
		return this.entityData.get(LOCKED_BY_UUID).isPresent();
	}

	@Nullable
	public UUID getLockedBy() {
		return this.entityData.get(LOCKED_BY_UUID).orElse((UUID) null);
	}

	public String getModel() {
		return this.entityData.get(MODEL_TYPE);
	}

	public void setModel(String model) {
		this.entityData.set(MODEL_TYPE, model);
	}

	public ItemStack getUsedUpgradeStack() {
		return this.entityData.get(USED_UPGRADE_ITEM);
	}

	public void setUsedUpgradeStack(ItemStack stack) {
		this.entityData.set(USED_UPGRADE_ITEM, stack);
	}

	public boolean canOpenUI(Player player) {
		final UUID lockedBy = this.getLockedBy();
		return lockedBy == null || (lockedBy != null && lockedBy.equals(player.getUUID()));
	}

	public void setLockedBy(@Nullable UUID uuid) {
		if (uuid == null) {
			this.setUnlocked();
		} else {
			this.entityData.set(LOCKED_BY_UUID, Optional.ofNullable(uuid));
		}
	}

	public void setUnlocked() {
		this.entityData.set(LOCKED_BY_UUID, Optional.empty());
	}

	public void setSlim(boolean slim) {
		this.isSlim = slim;
	}

	public boolean isSlim() {
		switch (getModel()) {
			case "SLIM":
				return true;
			case "DEFAULT":
				return false;
			default:
				return this.isSlim;
		}
	}

	public void setYOffset(float yOffset) {
		entityData.set(Y_OFFSET, Mth.clamp(yOffset, -1, 1));
	}

	public float getYOffsetData() {
		return entityData.get(Y_OFFSET);
	}

	@Override
	public Iterable<ItemStack> getHandSlots() {
		return this.handItems;
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return this.armorItems;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slotIn) {
		return switch (slotIn.getType()) {
			case HAND -> this.handItems.get(slotIn.getIndex());
			case ARMOR -> this.armorItems.get(slotIn.getIndex());
		};
	}

	@Override
	public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
		this.verifyEquippedItem(stack);
		switch (slotIn.getType()) {
			case HAND -> this.onEquipItem(slotIn, this.handItems.set(slotIn.getIndex(), stack), stack);
			case ARMOR -> this.onEquipItem(slotIn, this.armorItems.set(slotIn.getIndex(), stack), stack);
		}
	}

	@Override
	public boolean canTakeItem(ItemStack itemstackIn) {
		EquipmentSlot equipmentslottype = getEquipmentSlotForItem(itemstackIn);
		return this.getItemBySlot(equipmentslottype).isEmpty() && !this.isDisabled(equipmentslottype);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("gameProfileExists", entityData.get(GAMEPROFILE).isPresent());
		if (getGameProfile().isPresent()) {
			compound.put("gameProfile", NbtUtils.writeGameProfile(new CompoundTag(), entityData.get(GAMEPROFILE).get()));
		}

		compound.putFloat("yOffset", getYOffsetData());
		compound.putString("Model", getModel());

		ListTag listnbt = new ListTag();

		for (ItemStack itemstack : this.armorItems) {
			CompoundTag compoundnbt = new CompoundTag();
			if (!itemstack.isEmpty()) {
				itemstack.save(compoundnbt);
			}

			listnbt.add(compoundnbt);
		}

		compound.put("ArmorItems", listnbt);
		ListTag listnbt1 = new ListTag();

		for (ItemStack itemstack1 : this.handItems) {
			CompoundTag compoundnbt1 = new CompoundTag();
			if (!itemstack1.isEmpty()) {
				itemstack1.save(compoundnbt1);
			}

			listnbt1.add(compoundnbt1);
		}

		compound.putBoolean("Locked", this.isLocked());
		if (this.isLocked() && this.getLockedBy() != null) {
			compound.putUUID("LockedBy", this.getLockedBy());
		}

		compound.put("HandItems", listnbt1);
		compound.putBoolean("Small", this.isSmall());
		compound.putInt("DisabledSlots", this.disabledSlots);

		compound.put("Pose", this.writePose());

		ItemStack itemstack = this.getUsedUpgradeStack();
		if (!itemstack.isEmpty()) {
			compound.put("UpgradeItem", itemstack.save(new CompoundTag()));
		}
	}

	@Override
	public CompoundTag saveWithoutId(CompoundTag compound) {
		return super.saveWithoutId(compound);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		entityData.set(GAMEPROFILE, !compound.getBoolean("gameProfileExists") ? Optional.empty() :
				Optional.ofNullable(NbtUtils.readGameProfile(compound.getCompound("gameProfile"))));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		if (this.clientLock > 0) return;
		super.readAdditionalSaveData(compound);
		this.setYOffset(compound.getFloat("yOffset"));
		this.setModel(compound.getString("Model"));
		if (compound.contains("ArmorItems", 9)) {
			ListTag listnbt = compound.getList("ArmorItems", 10);

			for (int i = 0; i < this.armorItems.size(); ++i) {
				this.armorItems.set(i, ItemStack.of(listnbt.getCompound(i)));
			}
		}

		if (compound.getBoolean("Locked")) {
			UUID uuid;
			if (compound.hasUUID("LockedBy")) {
				uuid = compound.getUUID("LockedBy");
			} else {
				String s = compound.getString("LockedBy");
				uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
			}

			if (uuid != null) {
				this.setLockedBy(uuid);
			}
		}

		if (compound.contains("HandItems", 9)) {
			ListTag listnbt1 = compound.getList("HandItems", 10);

			for (int j = 0; j < this.handItems.size(); ++j) {
				this.handItems.set(j, ItemStack.of(listnbt1.getCompound(j)));
			}
		}

		this.setSmall(compound.getBoolean("Small"));
		this.disabledSlots = compound.getInt("DisabledSlots");
		this.noPhysics = !this.hasPhysics();
		CompoundTag compoundnbt = compound.getCompound("Pose");
		this.readPose(compoundnbt);

		if (compound.contains("UpgradeItem")) {
			ItemStack itemstack = ItemStack.of(compound.getCompound("UpgradeItem"));
			this.setUsedUpgradeStack(itemstack);
		}
	}

	private void readPose(CompoundTag tagCompound) {
		ListTag listnbt = tagCompound.getList("Head", 5);
		this.setHeadRotation(listnbt.isEmpty() ? DEFAULT_HEAD_ROTATION : new Rotations(listnbt));
		ListTag listnbt1 = tagCompound.getList("Body", 5);
		this.setBodyRotation(listnbt1.isEmpty() ? DEFAULT_BODY_ROTATION : new Rotations(listnbt1));
		ListTag listnbt2 = tagCompound.getList("LeftArm", 5);
		this.setLeftArmRotation(listnbt2.isEmpty() ? DEFAULT_LEFTARM_ROTATION : new Rotations(listnbt2));
		ListTag listnbt3 = tagCompound.getList("RightArm", 5);
		this.setRightArmRotation(listnbt3.isEmpty() ? DEFAULT_RIGHTARM_ROTATION : new Rotations(listnbt3));
		ListTag listnbt4 = tagCompound.getList("LeftLeg", 5);
		this.setLeftLegRotation(listnbt4.isEmpty() ? DEFAULT_LEFTLEG_ROTATION : new Rotations(listnbt4));
		ListTag listnbt5 = tagCompound.getList("RightLeg", 5);
		this.setRightLegRotation(listnbt5.isEmpty() ? DEFAULT_RIGHTLEG_ROTATION : new Rotations(listnbt5));
	}

	private CompoundTag writePose() {
		CompoundTag compoundnbt = new CompoundTag();
		if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
			compoundnbt.put("Head", this.headRotation.save());
		}

		if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
			compoundnbt.put("Body", this.bodyRotation.save());
		}

		if (!DEFAULT_LEFTARM_ROTATION.equals(this.leftArmRotation)) {
			compoundnbt.put("LeftArm", this.leftArmRotation.save());
		}

		if (!DEFAULT_RIGHTARM_ROTATION.equals(this.rightArmRotation)) {
			compoundnbt.put("RightArm", this.rightArmRotation.save());
		}

		if (!DEFAULT_LEFTLEG_ROTATION.equals(this.leftLegRotation)) {
			compoundnbt.put("LeftLeg", this.leftLegRotation.save());
		}

		if (!DEFAULT_RIGHTLEG_ROTATION.equals(this.rightLegRotation)) {
			compoundnbt.put("RightLeg", this.rightLegRotation.save());
		}

		return compoundnbt;
	}

	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entityIn) {

	}

	@Override
	public void setCustomName(@Nullable Component name) {
		if (name != null) {
			if (!isLocked()) {
				super.setCustomName(name);

				this.setGameProfile(new GameProfile((UUID) null, name.getString().toLowerCase(Locale.ROOT)));
			}
		}
	}

	/**
	 * Applies the given player interaction to this Entity.
	 */
	@Override
	public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			if (!this.level().isClientSide && player != null) {
				if (canOpenUI(player)) {
					ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new PlayerStatueScreenMessage(getId()));
				}
			}
		} else {
			if (itemstack.getItem() != Items.NAME_TAG) {
				if (player.isSpectator()) {
					return InteractionResult.SUCCESS;
				} else if (player.level().isClientSide) {
					return InteractionResult.CONSUME;
				} else {
					if (!isLocked()) {
						EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemstack);
						if (itemstack.isEmpty()) {
							EquipmentSlot equipmentslot1 = this.getClickedSlot(vec);
							EquipmentSlot equipmentslot2 = this.isDisabled(equipmentslot1) ? equipmentSlot : equipmentslot1;
							if (this.hasItemInSlot(equipmentslot2) && this.swapItem(player, equipmentslot2, itemstack, hand)) {
								return InteractionResult.SUCCESS;
							}
						} else {
							if (this.isDisabled(equipmentSlot)) {
								return InteractionResult.FAIL;
							}

							if (this.swapItem(player, equipmentSlot, itemstack, hand)) {
								return InteractionResult.SUCCESS;
							}
						}
					}

					return InteractionResult.PASS;
				}
			}
		}
		return InteractionResult.PASS;
	}

	private EquipmentSlot getClickedSlot(Vec3 clicked) {
		EquipmentSlot equipmentslottype = EquipmentSlot.MAINHAND;
		boolean flag = this.isSmall();
		double d0 = flag ? clicked.y * 2.0D : clicked.y;
		EquipmentSlot equipmentslottype1 = EquipmentSlot.FEET;
		if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(equipmentslottype1)) {
			equipmentslottype = EquipmentSlot.FEET;
		} else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D) && this.hasItemInSlot(EquipmentSlot.CHEST)) {
			equipmentslottype = EquipmentSlot.CHEST;
		} else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EquipmentSlot.LEGS)) {
			equipmentslottype = EquipmentSlot.LEGS;
		} else if (d0 >= 1.6D && this.hasItemInSlot(EquipmentSlot.HEAD)) {
			equipmentslottype = EquipmentSlot.HEAD;
		} else if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
			equipmentslottype = EquipmentSlot.OFFHAND;
		}

		return equipmentslottype;
	}

	private boolean isDisabled(EquipmentSlot slotIn) {
		return (this.disabledSlots & 1 << slotIn.getFilterFlag()) != 0;
	}

	private boolean swapItem(Player player, EquipmentSlot slot, ItemStack stack, InteractionHand hand) {
		ItemStack itemstack = this.getItemBySlot(slot);
		if (!itemstack.isEmpty() && (this.disabledSlots & 1 << slot.getFilterFlag() + 8) != 0) {
			return false;
		} else if (itemstack.isEmpty() && (this.disabledSlots & 1 << slot.getFilterFlag() + 16) != 0) {
			return false;
		} else if (player.getAbilities().instabuild && itemstack.isEmpty() && !stack.isEmpty()) {
			ItemStack itemstack2 = stack.copy();
			itemstack2.setCount(1);
			this.setItemSlot(slot, itemstack2);
			return true;
		} else if (!stack.isEmpty() && stack.getCount() > 1) {
			if (!itemstack.isEmpty()) {
				return false;
			} else {
				ItemStack itemstack1 = stack.copy();
				itemstack1.setCount(1);
				this.setItemSlot(slot, itemstack1);
				stack.shrink(1);
				return true;
			}
		} else {
			this.setItemSlot(slot, stack);
			player.setItemInHand(hand, itemstack);
			return true;
		}
	}

	@Override
	public boolean isInvulnerable() {
		return isLocked() || super.isInvulnerable();
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		if (isLocked()) {
			return true;
		}

		return super.isInvulnerableTo(source);
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!this.level().isClientSide && !this.isRemoved()) {
			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				this.remove(RemovalReason.DISCARDED);
				return false;
			} else if (!this.isInvulnerableTo(source)) {
				if (source.is(DamageTypeTags.IS_EXPLOSION)) {
					this.brokenByAnything(source);
					this.remove(RemovalReason.KILLED);
					return false;
				} else if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
					if (this.isOnFire()) {
						this.damagePlayerStatue(source, 0.15F);
					} else {
						this.setSecondsOnFire(5);
					}

					return false;
				} else if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
					this.damagePlayerStatue(source, 4.0F);
					return false;
				} else {
					boolean flag = source.getDirectEntity() instanceof AbstractArrow;
					boolean flag1 = flag && ((AbstractArrow) source.getDirectEntity()).getPierceLevel() > 0;
					boolean flag2 = "player".equals(source.getMsgId());
					if (!flag2 && !flag) {
						return false;
					} else if (source.getEntity() instanceof Player && !((Player) source.getEntity()).getAbilities().mayBuild) {
						return false;
					} else if (source.isCreativePlayer()) {
						this.playBrokenSound();
						this.playParticles();
						this.remove(RemovalReason.KILLED);
						return flag1;
					} else {
						long i = this.level().getGameTime();
						if (i - this.punchCooldown > 5L && !flag) {
							this.level().broadcastEntityEvent(this, (byte) 32);
							this.punchCooldown = i;
						} else {
							this.breakPlayerStatue(source);
							this.playParticles();
							this.remove(RemovalReason.KILLED);
						}

						return true;
					}
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleEntityEvent(byte id) {
		if (id == 32) {
			if (this.level().isClientSide) {
				this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
				this.punchCooldown = this.level().getGameTime();
			}
		} else {
			super.handleEntityEvent(id);
		}

	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0) || d0 == 0.0D) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	private void playParticles() {
		if (this.level() instanceof ServerLevel) {
			((ServerLevel) this.level()).sendParticles(
					new BlockParticleOption(ParticleTypes.BLOCK, ModRegistry.PLAYER_STATUE.get().defaultBlockState()),
					this.getX(), this.getY(0.6666666666666666D), this.getZ(), 10,
					(double) (this.getBbWidth() / 4.0F),
					(double) (this.getBbHeight() / 4.0F),
					(double) (this.getBbWidth() / 4.0F), 0.05D);
		}

	}

	private void damagePlayerStatue(DamageSource source, float amount) {
		float f = this.getHealth();
		f = f - amount;
		if (f <= 0.5F) {
			this.brokenByAnything(source);
			this.remove(RemovalReason.KILLED);
		} else {
			this.setHealth(f);
		}

	}

	private void breakPlayerStatue(DamageSource source) {
		ItemStack stack = new ItemStack(ModRegistry.PLAYER_STATUE.get());
		if (getGameProfile().isPresent()) {
			GameProfile profile = getGameProfile().get();
			if (profile != null) {
				CompoundTag stackTag = stack.getTag() != null ? stack.getTag() : new CompoundTag();
				CompoundTag nbttagcompound = new CompoundTag();
				NbtUtils.writeGameProfile(nbttagcompound, profile);
				stackTag.put("PlayerProfile", nbttagcompound);
				stack.setTag(stackTag);
				stack.setHoverName(Component.literal(profile.getName()));
			}
		}

		Block.popResource(this.level(), this.blockPosition(), stack);
		if (!this.getUsedUpgradeStack().isEmpty()) {
			Block.popResource(this.level(), this.blockPosition(), this.getUsedUpgradeStack());
		}
		this.brokenByAnything(source);
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack stack = new ItemStack(ModRegistry.PLAYER_STATUE.get());
		if (getGameProfile().isPresent()) {
			GameProfile profile = getGameProfile().get();
			if (profile != null) {
				CompoundTag stackTag = stack.getTag() != null ? stack.getTag() : new CompoundTag();
				CompoundTag nbttagcompound = new CompoundTag();
				NbtUtils.writeGameProfile(nbttagcompound, profile);
				stackTag.put("PlayerProfile", nbttagcompound);
				stack.setTag(stackTag);
				stack.setHoverName(Component.literal(profile.getName()));
			}
		}

		return stack;
	}

	private void brokenByAnything(DamageSource source) {
		this.playBrokenSound();
		this.dropAllDeathLoot(source);

		for (int i = 0; i < this.handItems.size(); ++i) {
			ItemStack itemstack = this.handItems.get(i);
			if (!itemstack.isEmpty()) {
				Block.popResource(this.level(), this.blockPosition().above(), itemstack);
				this.handItems.set(i, ItemStack.EMPTY);
			}
		}

		for (int j = 0; j < this.armorItems.size(); ++j) {
			ItemStack itemstack1 = this.armorItems.get(j);
			if (!itemstack1.isEmpty()) {
				Block.popResource(this.level(), this.blockPosition().above(), itemstack1);
				this.armorItems.set(j, ItemStack.EMPTY);
			}
		}

	}

	private void playBrokenSound() {
		this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
	}

	@Override
	protected float tickHeadTurn(float partialTick, float animStep) {
		this.yBodyRotO = this.yRotO;
		this.yBodyRot = this.getYRot();
		return 0.0F;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return sizeIn.height * (this.isBaby() ? 0.5F : 0.9F);
	}

	/**
	 * Returns the Y Offset of this entity.
	 */
	@Override
	public double getMyRidingOffset() {
		return (double) 0.1F + getYOffsetData(); //TODO: what does this do?
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.hasPhysics()) {
			super.travel(travelVector);
		}
	}

	/**
	 * Set the render yaw offset
	 */
	@Override
	public void setYBodyRot(float offset) {
		this.yBodyRotO = this.yRotO = offset;
		this.yHeadRotO = this.yHeadRot = offset;
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	/**
	 * Sets the head's yaw rotation of the entity.
	 */
	@Override
	public void setYHeadRot(float rotation) {
		this.yBodyRotO = this.yRotO = rotation;
		this.yHeadRotO = this.yHeadRot = rotation;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void tick() {
		super.tick();

		if (this.clientLock > 0) {
			this.clientLock--;
			return;
		}
		Rotations rotations = this.entityData.get(HEAD_ROTATION);
		if (!this.headRotation.equals(rotations)) {
			this.setHeadRotation(rotations);
		}

		Rotations rotations1 = this.entityData.get(BODY_ROTATION);
		if (!this.bodyRotation.equals(rotations1)) {
			this.setBodyRotation(rotations1);
		}

		Rotations rotations2 = this.entityData.get(LEFT_ARM_ROTATION);
		if (!this.leftArmRotation.equals(rotations2)) {
			this.setLeftArmRotation(rotations2);
		}

		Rotations rotations3 = this.entityData.get(RIGHT_ARM_ROTATION);
		if (!this.rightArmRotation.equals(rotations3)) {
			this.setRightArmRotation(rotations3);
		}

		Rotations rotations4 = this.entityData.get(LEFT_LEG_ROTATION);
		if (!this.leftLegRotation.equals(rotations4)) {
			this.setLeftLegRotation(rotations4);
		}

		Rotations rotations5 = this.entityData.get(RIGHT_LEG_ROTATION);
		if (!this.rightLegRotation.equals(rotations5)) {
			this.setRightLegRotation(rotations5);
		}
	}

	/**
	 * If Animal, checks if the age timer is negative
	 */
	@Override
	public boolean isBaby() {
		return this.isSmall();
	}

	/**
	 * Called by the /kill command.
	 */
	@Override
	public void kill() {
		this.remove(RemovalReason.KILLED);
	}

	private void setSmall(boolean small) {
		this.entityData.set(STATUS, this.setBit(this.entityData.get(STATUS), 1, small));
	}

	public boolean isSmall() {
		return (this.entityData.get(STATUS) & 1) != 0;
	}

	private byte setBit(byte oldBit, int offset, boolean value) {
		if (value) {
			oldBit = (byte) (oldBit | offset);
		} else {
			oldBit = (byte) (oldBit & ~offset);
		}

		return oldBit;
	}

	public void setHeadRotation(Rotations vec) {
		this.headRotation = vec;
		this.entityData.set(HEAD_ROTATION, vec);
	}

	public void setBodyRotation(Rotations vec) {
		this.bodyRotation = vec;
		this.entityData.set(BODY_ROTATION, vec);
	}

	public void setLeftArmRotation(Rotations vec) {
		this.leftArmRotation = vec;
		this.entityData.set(LEFT_ARM_ROTATION, vec);
	}

	public void setRightArmRotation(Rotations vec) {
		this.rightArmRotation = vec;
		this.entityData.set(RIGHT_ARM_ROTATION, vec);
	}

	public void setLeftLegRotation(Rotations vec) {
		this.leftLegRotation = vec;
		this.entityData.set(LEFT_LEG_ROTATION, vec);
	}

	public void setRightLegRotation(Rotations vec) {
		this.rightLegRotation = vec;
		this.entityData.set(RIGHT_LEG_ROTATION, vec);
	}

	public Rotations getHeadRotation() {
		return this.headRotation;
	}

	public Rotations getBodyRotation() {
		return this.bodyRotation;
	}

	@OnlyIn(Dist.CLIENT)
	public Rotations getLeftArmRotation() {
		return this.leftArmRotation;
	}

	@OnlyIn(Dist.CLIENT)
	public Rotations getRightArmRotation() {
		return this.rightArmRotation;
	}

	@OnlyIn(Dist.CLIENT)
	public Rotations getLeftLegRotation() {
		return this.leftLegRotation;
	}

	@OnlyIn(Dist.CLIENT)
	public Rotations getRightLegRotation() {
		return this.rightLegRotation;
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean isPickable() {
		return true;
	}

	/**
	 * Called when a player attacks an entity. If this returns true the attack will not happen.
	 */
	@Override
	public boolean skipAttackInteraction(Entity entityIn) {
		return entityIn instanceof Player && !this.level().mayInteract((Player) entityIn, this.blockPosition());
	}

	@Override
	public LivingEntity.Fallsounds getFallSounds() {
		return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ARMOR_STAND_HIT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ARMOR_STAND_BREAK;
	}

	@Override
	public void thunderHit(ServerLevel serverLevel, LightningBolt bolt) {
	}

	/**
	 * Returns false if the entity is an armor stand. Returns true for all other entity living bases.
	 */
	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (STATUS.equals(key)) {
			this.refreshDimensions();
			this.blocksBuilding = !this.isRemoved();
		}
		if (GAMEPROFILE.equals(key)) {
			synchronized (this) {
				getGameProfile().ifPresent(profile -> {
					if (this.level() != null && this.level().isClientSide && profile != null && profile.isComplete()) {
						Minecraft.getInstance().getSkinManager().registerSkins(profile, (textureType, textureLocation, profileTexture) -> {
							if (textureType.equals(MinecraftProfileTexture.Type.SKIN)) {
								String metadata = profileTexture.getMetadata("model");
								this.setSlim(metadata != null && metadata.equals("slim"));
							}
						}, true);
					}
				});
			}
		}

		super.onSyncedDataUpdated(key);
	}

	@Override
	public boolean attackable() {
		return false;
	}
}
