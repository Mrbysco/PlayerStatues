package com.shynieke.playerstatues.datagen;

import com.shynieke.playerstatues.datagen.client.ModBlockstateProvider;
import com.shynieke.playerstatues.datagen.client.ModItemModelProvider;
import com.shynieke.playerstatues.datagen.client.ModLanguageProvider;
import com.shynieke.playerstatues.datagen.server.ModBlockTagProvider;
import com.shynieke.playerstatues.datagen.server.ModItemTagProvider;
import com.shynieke.playerstatues.datagen.server.ModLootProvider;
import com.shynieke.playerstatues.datagen.server.ModRecipeProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
			generator.addProvider(event.includeServer(), new ModLootProvider(packOutput));
			ModBlockTagProvider blockTags = new ModBlockTagProvider(packOutput, lookupProvider, helper);
			generator.addProvider(event.includeServer(), blockTags);
			generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockTags, helper));
			if (ModList.get().isLoaded("curios")) {
				generator.addProvider(event.includeServer(),
						new com.shynieke.playerstatues.datagen.server.ModCuriosProvider(packOutput, helper, lookupProvider));
			}
		}
		if (event.includeClient()) {
			generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput));
			generator.addProvider(event.includeClient(), new ModBlockstateProvider(packOutput, helper));
			generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, helper));
		}
	}

}
