package com.shynieke.playerstatues.compat.curios;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

@SuppressWarnings("removal")
public class CuriosCompat {

	public static ICapabilityProvider getCapability(ItemStack stack) {
		ICurio curio = new ICurio() {
			@Override
			public ItemStack getStack() {
				return stack;
			}

			@Override
			public boolean canEquipFromUse(SlotContext ctx) {
				return true;
			}
		};
		ICapabilityProvider provider = new ICapabilityProvider() {
			private final LazyOptional<ICurio> curioOpt = LazyOptional.of(() -> curio);

			@NotNull
			@Override
			public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap,
			                                         @Nullable Direction side) {
				return CuriosCapability.ITEM.orEmpty(cap, curioOpt);
			}
		};
		return provider;
	}
}
