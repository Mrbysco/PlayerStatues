package com.shynieke.playerstatues.datagen.client;

import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.item.PlayerDollItem;
import com.shynieke.playerstatues.registry.ModRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
	public ModItemModelProvider(PackOutput packOutput, ExistingFileHelper helper) {
		super(packOutput, PlayerStatuesMod.MOD_ID, helper);
	}

	@Override
	protected void registerModels() {
		for (RegistryObject<Item> registryObject : ModRegistry.ITEMS.getEntries()) {
			if (registryObject.get() instanceof BlockItem) {
				withBlockParent(registryObject.getId());
			} else if (registryObject.get() instanceof PlayerDollItem) {
				playerDoll(registryObject.getId());
			} else {
				generatedItem(registryObject.getId());
			}
		}
	}

	private void playerDoll(ResourceLocation location) {
		withExistingParent(location.getPath(), modLoc("block/player_statue"));
	}

	private void withBlockParent(ResourceLocation location) {
		withExistingParent(location.getPath(), modLoc("block/" + location.getPath()));
	}

	private void generatedItem(ResourceLocation location) {
		singleTexture(location.getPath(), new ResourceLocation("item/generated"),
				"layer0", new ResourceLocation(PlayerStatuesMod.MOD_ID, "item/" + location.getPath()));
	}
}
