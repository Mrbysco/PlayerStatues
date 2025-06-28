package com.shynieke.playerstatues;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.shynieke.playerstatues.client.ClientHandler;
import com.shynieke.playerstatues.config.PlayerStatuesConfig;
import com.shynieke.playerstatues.item.PlayerStatueBlockItem;
import com.shynieke.playerstatues.network.ModNetworking;
import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import com.shynieke.playerstatues.registry.ModSerializers;
import com.shynieke.playerstatues.util.SkinUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.UUID;

@Mod(PlayerStatuesMod.MOD_ID)
public class PlayerStatuesMod {
	public static final String MOD_ID = "player_statues";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final TagKey<Item> UPGRADE_ITEM = ItemTags.create(new ResourceLocation(MOD_ID, "upgrade_item"));

	public PlayerStatuesMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PlayerStatuesConfig.clientSpec);
		eventBus.register(PlayerStatuesConfig.class);

		ModSerializers.ENTITY_DATA_SERIALIZER.register(eventBus);
		ModEntities.ENTITY_TYPES.register(eventBus);
		ModRegistry.BLOCKS.register(eventBus);
		ModRegistry.ITEMS.register(eventBus);
		ModRegistry.BLOCK_ENTITIES.register(eventBus);

		eventBus.addListener(ModEntities::registerEntityAttributes);
		eventBus.addListener(this::setup);
		eventBus.addListener(this::addTabContents);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::onAnvilRepair);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			eventBus.addListener(ClientHandler::doClientStuff);
			eventBus.addListener(ClientHandler::registerEntityRenders);
			eventBus.addListener(ClientHandler::registerLayerDefinitions);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onLogin);
			MinecraftForge.EVENT_BUS.addListener(ClientHandler::onRespawn);
		});
	}

	public void serverAboutToStart(final ServerAboutToStartEvent event) {
		MinecraftServer server = event.getServer();
		SkinUtil.setup(server.getProfileCache(), server.getSessionService(), server);
		GameProfileCache.setUsesAuthentication(server.usesAuthentication());
	}

	public void onAnvilRepair(final AnvilRepairEvent event) {
		ItemStack result = event.getOutput();
		if (result.getItem() instanceof PlayerStatueBlockItem && result.hasCustomHoverName()) {
			String stackName = result.getHoverName().getString().toLowerCase(Locale.ROOT);
			boolean validFlag = !stackName.isEmpty() && !stackName.contains(" ");
			if (validFlag) {
				CompoundTag stackTag = result.getOrCreateTag();
				GameProfile stackProfile = new GameProfile((UUID) null, stackName);
				SkinUtil.updateGameProfile(stackProfile, (profile) -> {
					if (profile != null) {
						CompoundTag profileTag = new CompoundTag();
						NbtUtils.writeGameProfile(profileTag, profile);
						stackTag.put("PlayerProfile", profileTag);
						result.setTag(stackTag);
					}
				});
			}
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		ModNetworking.init();
	}

	private void addTabContents(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(ModRegistry.PLAYER_STATUE_SPAWN_EGG.get());
		}
		if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
			event.accept(ModRegistry.PLAYER_STATUE.get());
		}
	}
}
