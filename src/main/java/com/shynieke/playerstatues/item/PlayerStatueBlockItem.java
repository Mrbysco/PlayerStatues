package com.shynieke.playerstatues.item;

import com.mojang.authlib.GameProfile;
import com.shynieke.playerstatues.client.renderer.PlayerBEWLR;
import com.shynieke.playerstatues.util.SkinUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerStatueBlockItem extends BlockItem {

	public PlayerStatueBlockItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}

	@Nullable
	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}

	@Override
	public void verifyTagAfterLoad(CompoundTag tag) {
		super.verifyTagAfterLoad(tag);
		if (tag.contains("PlayerProfile", 8) && !Util.isBlank(tag.getString("PlayerProfile"))) {
			GameProfile gameprofile = new GameProfile((UUID) null, tag.getString("PlayerProfile"));
			SkinUtil.updateGameProfile(gameprofile, (profile) ->
					tag.put("PlayerProfile", NbtUtils.writeGameProfile(new CompoundTag(), profile)));
		}
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		if (ModList.get().isLoaded("curios")) {
			return com.shynieke.playerstatues.compat.curios.CuriosCompat.getCapability(stack);
		}
		return super.initCapabilities(stack, nbt);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new PlayerBEWLR(new BlockEntityRendererProvider.Context(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getBlockRenderer(),
						Minecraft.getInstance().getItemRenderer(),
						Minecraft.getInstance().getEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels(),
						Minecraft.getInstance().font
				));
			}
		});
	}
}
