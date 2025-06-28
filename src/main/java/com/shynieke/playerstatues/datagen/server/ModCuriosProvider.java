package com.shynieke.playerstatues.datagen.server;

import com.shynieke.playerstatues.PlayerStatuesMod;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class ModCuriosProvider extends CuriosDataProvider {
	public ModCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<Provider> registries) {
		super(PlayerStatuesMod.MOD_ID, output, fileHelper, registries);
	}

	@Override
	public void generate(Provider registries, ExistingFileHelper fileHelper) {
		createSlot("head").size(1);
		createSlot("body").size(1);
		createSlot("belt").size(1);
		createSlot("feet").size(1).icon(new ResourceLocation(PlayerStatuesMod.MOD_ID, "slot/feet"));

		createEntities("player_statue_entities")
				.addSlots("head", "body", "belt", "feet")
				.addPlayer();
	}
}
