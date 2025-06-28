package com.shynieke.playerstatues.client;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.shynieke.playerstatues.PlayerStatuesMod;
import com.shynieke.playerstatues.client.model.PlayerStatueModel;
import com.shynieke.playerstatues.client.renderer.PlayerBER;
import com.shynieke.playerstatues.client.renderer.PlayerStatueRenderer;
import com.shynieke.playerstatues.registry.ModEntities;
import com.shynieke.playerstatues.registry.ModRegistry;
import com.shynieke.playerstatues.util.SkinUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientHandler {
	public static final ModelLayerLocation PLAYER_STATUE = new ModelLayerLocation(new ResourceLocation(PlayerStatuesMod.MOD_ID, "player_statue"), "main");
	public static final ModelLayerLocation PLAYER_STATUE_SLIM = new ModelLayerLocation(new ResourceLocation(PlayerStatuesMod.MOD_ID, "player_statue"), "slim");

	public static void doClientStuff(final FMLClientSetupEvent event) {
		setPlayerCache(Minecraft.getInstance());


		if (ModList.get().isLoaded("curios")) {
			com.shynieke.playerstatues.compat.curios.client.StatueCurioRenderer.setupRenderer(event);
		}
	}

	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModRegistry.PLAYER.get(), PlayerBER::new);

		event.registerEntityRenderer(ModEntities.PLAYER_STATUE_ENTITY.get(), PlayerStatueRenderer::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(PLAYER_STATUE, () -> LayerDefinition.create(PlayerStatueModel.createStatueMesh(CubeDeformation.NONE, false), 64, 64));
		event.registerLayerDefinition(PLAYER_STATUE_SLIM, () -> LayerDefinition.create(PlayerStatueModel.createStatueMesh(CubeDeformation.NONE, true), 64, 64));
	}

	public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isLocalServer()) {
			setPlayerCache(mc);
		}
	}

	public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isLocalServer()) {
			setPlayerCache(mc);
		}
	}

	private static void setPlayerCache(Minecraft mc) {
		YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(mc.getProxy());
		Services services = Services.create(authenticationService, mc.gameDirectory);
		services.profileCache().setExecutor(mc);
		SkinUtil.setup(services, mc);
		GameProfileCache.setUsesAuthentication(false);
	}
}
