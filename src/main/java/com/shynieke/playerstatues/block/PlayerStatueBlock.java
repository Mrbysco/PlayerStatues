package com.shynieke.playerstatues.block;

import com.mojang.authlib.GameProfile;
import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.blockentity.PlayerBlockEntity;
import com.shynieke.playerstatues.entity.PlayerStatuesStatue;
import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerStatueBlock extends AbstractBaseBlock {

	private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

	public PlayerStatueBlock(Properties builder) {
		super(builder.sound(SoundType.STONE));
		this.registerDefaultState(this.defaultBlockState()
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, Boolean.FALSE));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlayerBlockEntity(pos, state);
	}

	private PlayerBlockEntity getBE(Level level, BlockPos pos) {
		return (PlayerBlockEntity) level.getBlockEntity(pos);
	}

	@NotNull
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos,
	                          @NotNull BlockState state, BlockEntity be, @NotNull ItemStack stack) {
		if (be instanceof PlayerBlockEntity blockEntity && ((Nameable) be).hasCustomName()) {
			player.causeFoodExhaustion(0.005F);

			if (level.isClientSide)
				return;

			if (this == Blocks.AIR)
				return;

			ItemStack itemstack = new ItemStack(this);
			itemstack.setHoverName(((Nameable) blockEntity).getName());

			if (blockEntity.getPlayerProfile() != null) {
				CompoundTag stackTag = itemstack.getTag() != null ? itemstack.getTag() : new CompoundTag();
				CompoundTag tag = new CompoundTag();
				NbtUtils.writeGameProfile(tag, blockEntity.getPlayerProfile());
				stackTag.put("PlayerProfile", tag);
				itemstack.setTag(stackTag);
				itemstack.setHoverName(((Nameable) blockEntity).getName());
			}

			popResource(level, pos, itemstack);
		} else {
			super.playerDestroy(level, player, pos, state, null, stack);
		}
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
		if (state.hasBlockEntity() && newState.getBlock() != ModRegistry.PLAYER_STATUE.get()) {
			level.removeBlockEntity(pos);
		}
	}

	@NotNull
	@Override
	public ItemStack pickupBlock(LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof PlayerBlockEntity) {
			return getStatueWithName(level, pos, state);
		} else {
			return new ItemStack(state.getBlock());
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof PlayerBlockEntity) {
			return getStatueWithName(level, pos, state);
		} else {
			return super.getCloneItemStack(state, target, level, pos, player);
		}
	}

	private ItemStack getStatueWithName(BlockGetter level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof PlayerBlockEntity playerBlockEntity) {
			ItemStack stack = new ItemStack(state.getBlock());

			GameProfile profile = playerBlockEntity.getPlayerProfile();
			if (profile != null) {
				CompoundTag tag = new CompoundTag();
				tag.put("PlayerProfile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
				stack.setTag(tag);
			}

			return stack.setHoverName(playerBlockEntity.getName());
		} else {
			return new ItemStack(state.getBlock());
		}
	}

	@Override
	public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
	                        @Nullable LivingEntity placer, @NotNull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);

		if (!level.isClientSide && getBE(level, pos) != null) {
			PlayerBlockEntity playerBlockEntity = getBE(level, pos);
			CompoundTag tag = stack.getTag();
			if (tag != null) {
				if (tag.contains("PlayerProfile", 10)) {
					GameProfile gameprofile = NbtUtils.readGameProfile(tag.getCompound("PlayerProfile"));
					playerBlockEntity.setPlayerProfile(gameprofile);
				} else if (tag.contains("PlayerProfile", 8) && !Util.isBlank(tag.getString("PlayerProfile"))) {
					GameProfile gameprofile = new GameProfile((UUID) null, tag.getString("PlayerProfile"));
					tag.remove("PlayerProfile");
					SkullBlockEntity.updateGameprofile(gameprofile, (profile) -> {
						tag.put("PlayerProfile", NbtUtils.writeGameProfile(new CompoundTag(), profile));
						playerBlockEntity.setPlayerProfile(profile);
					});
				}
			} else {
				if (stack.hasCustomHoverName()) {
					String stackName = stack.getHoverName().getString();
					boolean spaceFlag = stackName.contains(" ");
					boolean emptyFlag = stackName.isEmpty();

					if (!spaceFlag && !emptyFlag) {
						playerBlockEntity.setPlayerProfile(new GameProfile((UUID) null, stackName));
					}
				} else {
					if (placer instanceof Player player) {
						playerBlockEntity.setPlayerProfile(player.getGameProfile());
					} else {
						playerBlockEntity.setPlayerProfile(new GameProfile((UUID) null, "steve"));
					}
				}
			}

		}
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level,
	                            @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				MutableComponent userComponent = Component.literal("Username: ").withStyle(ChatFormatting.GOLD);
				userComponent.append(stack.getHoverName().plainCopy().withStyle(ChatFormatting.WHITE));
				tooltip.add(userComponent);

				if (tag != null && tag.contains("PlayerProfile")) {
					CompoundTag profileTag = (CompoundTag) tag.get("PlayerProfile");
					if (profileTag != null) {
						GameProfile gameprofile = NbtUtils.readGameProfile(profileTag);

						if (gameprofile != null && gameprofile.isComplete()) {
							MutableComponent UUIDComponent = Component.literal("UUID: ").withStyle(ChatFormatting.GOLD);
							UUIDComponent.append(Component.literal(gameprofile.getId().toString()).withStyle(ChatFormatting.WHITE));
							tooltip.add(UUIDComponent);
						}
					}
				}
			}
		}
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
		return false;
	}

	@NotNull
	@Override
	public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
	                             Player playerIn, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
		ItemStack stack = playerIn.getItemInHand(hand);
		GameProfile gameProfile = getBE(level, pos).getPlayerProfile();
		PlayerBlockEntity playerBlockEntity = getBE(level, pos);
		if (!level.isClientSide && playerBlockEntity != null && gameProfile != null) {
			if (playerIn.isShiftKeyDown()) {
				return InteractionResult.SUCCESS;
			} else {
				if (stack.is(PlayerStatuesMod.UPGRADE_ITEM)) {
					if (level instanceof ServerLevel serverLevel) {
						Consumer<PlayerStatuesStatue> consumer = EntityType.appendCustomEntityStackConfig((statue) -> {
						}, serverLevel, stack, playerIn);
						PlayerStatuesStatue playerStatueEntity = ModEntities.PLAYER_STATUE_ENTITY.get().create(serverLevel, stack.getTag(), consumer, pos, MobSpawnType.SPAWN_EGG, true, true);
						if (playerStatueEntity == null) {
							return InteractionResult.FAIL;
						}

						serverLevel.addFreshEntityWithPassengers(playerStatueEntity);
						float f = (float) Mth.floor((Mth.wrapDegrees(playerIn.getYRot() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
						playerStatueEntity.setGameProfile(playerBlockEntity.getPlayerProfile());
						playerStatueEntity.moveTo(playerStatueEntity.getX(), playerStatueEntity.getY(), playerStatueEntity.getZ(), f, 0.0F);
						level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
						level.addFreshEntity(playerStatueEntity);
						level.playSound((Player) null, playerStatueEntity.getX(), playerStatueEntity.getY(), playerStatueEntity.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);

						if (!playerIn.getAbilities().instabuild) {
							ItemStack usedItem = stack.split(1);
							playerStatueEntity.setUsedUpgradeStack(usedItem);
						}
					}
				}
			}
		}
		return InteractionResult.PASS;
	}

	@NotNull
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@NotNull
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
	                           @NotNull CollisionContext context) {
		return SHAPE;
	}

	@NotNull
	@Override
	public VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
		return Shapes.empty();
	}

	@NotNull
	@Override
	public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
	                              @NotNull PathComputationType computationType) {
		return false;
	}
}