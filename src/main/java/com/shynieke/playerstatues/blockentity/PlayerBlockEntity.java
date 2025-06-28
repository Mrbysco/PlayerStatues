package com.shynieke.playerstatues.blockentity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.shynieke.playerstatues.registry.ModRegistry;
import com.shynieke.playerstatues.util.SkinUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PlayerBlockEntity extends BlockEntity implements Nameable {
	private GameProfile playerProfile;
	private boolean isSlim = false;

	public PlayerBlockEntity(BlockPos pos, BlockState state) {
		super(ModRegistry.PLAYER.get(), pos, state);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		if (compound.contains("PlayerProfile", 10)) {
			this.setPlayerProfile(NbtUtils.readGameProfile(compound.getCompound("PlayerProfile")));
		}
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		if (this.playerProfile != null) {
			CompoundTag tag = new CompoundTag();
			NbtUtils.writeGameProfile(tag, this.playerProfile);
			compound.put("PlayerProfile", tag);
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag compoundNBT = pkt.getTag();
		handleUpdateTag(compoundNBT);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt);
		return nbt;
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt);
		return nbt;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public boolean hasCustomName() {
		return this.playerProfile != null && !this.playerProfile.getName().isEmpty();
	}

	@Nullable
	public GameProfile getPlayerProfile() {
		return this.playerProfile;
	}

	public boolean isSlim() {
		return this.isSlim;
	}

	public void setPlayerProfile(@Nullable GameProfile profile) {
		synchronized (this) {
			this.playerProfile = profile;
			if (this.level != null && this.level.isClientSide && this.playerProfile != null && this.playerProfile.isComplete()) {
				Minecraft.getInstance().getSkinManager().registerSkins(this.playerProfile, (textureType, textureLocation, profileTexture) -> {
					if (textureType.equals(MinecraftProfileTexture.Type.SKIN)) {
						String metadata = profileTexture.getMetadata("model");
						this.isSlim = metadata != null && metadata.equals("slim");
					}
				}, true);
			}
		}

		this.updateOwnerProfile();
	}

	private void updateOwnerProfile() {
		SkinUtil.updateGameProfile(this.playerProfile, (profile) -> {
			this.playerProfile = profile;
			this.setChanged();
		});
	}

	@Override
	public Component getName() {
		return this.hasCustomName() ? Component.literal(this.playerProfile != null ? playerProfile.getName() : "") : Component.translatable("entity.statues.player_statue");
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return null;
	}
}
